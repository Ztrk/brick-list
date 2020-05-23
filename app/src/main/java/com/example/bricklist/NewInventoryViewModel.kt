package com.example.bricklist

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.preference.PreferenceManager
import com.android.volley.ClientError
import com.android.volley.VolleyError
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

class NewInventoryViewModel(application: Application) : AndroidViewModel(application) {
    private val requests = NetworkRequests.getInstance(application)
    private val database = BrickListDatabase.getDatabase(application)
    private val brickListDao = database.getBrickListDao()
    private val inventoryDao = database.getInventoryDao()
    private val inventoryPartDao = database.getInventoryPartDao()

    private val urlPrefix = PreferenceManager.getDefaultSharedPreferences(application)
        .getString("url_prefix", "http://fcds.cs.put.poznan.pl/MyWeb/BL/")

    private val _result = MutableLiveData(State.NONE)
    val result: LiveData<State>
        get() = _result

    fun resetResult() {
        if (_result.value != State.IN_PROGRESS) {
            _result.value = State.NONE
        }
    }

    private val _bricksNotFound = MutableLiveData(listOf<String>())
    val bricksNotFound: LiveData<List<String>>
        get() = _bricksNotFound

    fun resetBricksNotFound() {
        _bricksNotFound.value = listOf()
    }

    private val _addWhenInProgress = MutableLiveData(false)
    val addWhenInProgress: LiveData<Boolean>
        get() = _addWhenInProgress

    fun handledAddWhenInProgress() {
        _addWhenInProgress.value = false
    }

    enum class State {
        NONE, IN_PROGRESS, WRONG_URL, NETWORK_ERROR, SUCCESS
    }


    fun addInventory(setNumber: String, name: String) = viewModelScope.launch {
        if (_result.value == State.IN_PROGRESS) {
            _addWhenInProgress.value = true
            return@launch
        }
        _result.value = State.IN_PROGRESS

        val url = "$urlPrefix$setNumber.xml"
        val response = try {
            requests.requestString(url)
        }
        catch (error: ClientError) {
            _result.value = State.WRONG_URL
            return@launch
        } catch (error: VolleyError) {
            _result.value = State.NETWORK_ERROR
            return@launch
        }

        val parser = InventoryXmlParser()
        val inventory = parser.parse(response)
        inventory.inventory.name = name

        _result.value = State.SUCCESS

        _bricksNotFound.value = insert(inventory)
    }

    private suspend fun insert(inventory: InventoryWithParts): List<String> = withContext(Dispatchers.IO) {
        val inventoryId = inventoryDao.insertInventory(inventory.inventory).toInt()
        val bricksNotFound = mutableListOf<String>()
        val bricksNotFoundMutex = Mutex(false)

        val inventoryDeferred = inventory.parts.map { part ->
            async {
                val color = brickListDao.getColorByCode(part.color.code)
                val item = brickListDao.getItemByCode(part.item.code)
                if (item == null || color == null) {
                    bricksNotFoundMutex.withLock {
                        bricksNotFound.add(part.item.code)
                    }
                    return@async null
                }

                var itemType = brickListDao.getItemTypeByCode(part.itemType.code)
                if (itemType == null) {
                    itemType = part.itemType
                }

                var code = brickListDao.getCodeByIds(item.id, color.id)
                if (code == null) {
                    code = Code(itemId = item.id, colorId = color.id)
                    brickListDao.insertCode(code)
                }

                return@async part.inventoryPart.copy(
                    inventoryId = inventoryId, colorId = color.id,
                    itemId =  item.id, typeId = itemType.id
                )
            }
        }
        val inventoryParts = inventoryDeferred.mapNotNull { deferred -> deferred.await() }
        inventoryPartDao.insertInventoryParts(inventoryParts)
        bricksNotFound
    }
}
