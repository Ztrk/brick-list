package com.example.bricklist

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class NewInventoryViewModel(application: Application) : AndroidViewModel(application) {
    private val queue = Volley.newRequestQueue(application)
    private val database = BrickListDatabase.getDatabase(application)
    private val brickListDao = database.getBrickListDao()
    private val inventoryDao = database.getInventoryDao()
    private val inventoryPartDao = database.getInventoryPartDao()

    private val _result = MutableLiveData("")
    val result: LiveData<String>
        get() = _result

    fun addInventory(url: String, name: String) = viewModelScope.launch {
        val response = try {
            requestXml(url)
        }
        catch (error: VolleyError) {
            _result.value = error.toString()
            return@launch
        }

        val parser = InventoryXmlParser()
        val inventory = parser.parse(response)
        inventory.inventory.name = name

        try {
            insert(inventory)
        }
        catch (error: Throwable) {
            error.printStackTrace()
            throw error
        }

        _result.value = "New project was created"
    }

    private suspend fun requestXml(url: String): String = suspendCancellableCoroutine {
        val stringRequest = StringRequest(
            Request.Method.GET, url,
            Response.Listener { response ->
                it.resume(response)
            },
            Response.ErrorListener { error ->
                it.resumeWithException(error)
            }
        )
        it.invokeOnCancellation {
            stringRequest.cancel()
        }
        queue.add(stringRequest)
    }

    private suspend fun insert(inventory: InventoryWithParts) = withContext(Dispatchers.IO) {
        val inventoryId = inventoryDao.insertInventory(inventory.inventory).toInt()

        _result.postValue(inventoryId.toString())

        val inventoryParts = inventory.parts.mapNotNull { part ->
            val color = brickListDao.getColorByCode(part.color.code)
            val item = brickListDao.getItemByCode(part.item.code)
            if (item == null || color == null) {
                return@mapNotNull null
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

            return@mapNotNull part.inventoryPart.copy(
                inventoryId = inventoryId, colorId = color.id,
                itemId =  item.id, typeId = itemType.id
            )
        }
        inventoryPartDao.insertInventoryParts(inventoryParts)
    }
}
