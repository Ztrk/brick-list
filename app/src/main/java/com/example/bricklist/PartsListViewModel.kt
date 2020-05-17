package com.example.bricklist

import android.app.Application
import androidx.lifecycle.*
import com.android.volley.ClientError
import kotlinx.coroutines.launch

class PartsListViewModel(application: Application, inventoryId: Int)
        : AndroidViewModel(application) {

    private val requests = NetworkRequests.getInstance(application)
    private val inventoryPartDao: InventoryPartDao
    private val brickListDao: BrickListDao

    init {
        val database = BrickListDatabase.getDatabase(application)
        inventoryPartDao = database.getInventoryPartDao()
        brickListDao = database.getBrickListDao()
    }

    val inventoryParts = MediatorLiveData<List<InventoryPartWithReferences>>()
    private val _inventoryParts = inventoryPartDao.getInventoryPartsById(inventoryId)
    private val codes = MutableLiveData<HashMap<Pair<Int, Int>, Code>>(hashMapOf())
    private val fetchedIds = hashSetOf<Pair<Int, Int>>()

    init {
        inventoryParts.addSource(_inventoryParts) {
            val codesData = codes.value
            if (codesData != null) {
                inventoryParts.value = combine(it, codesData)
            }
            else {
                inventoryParts.value = it
            }
        }
        inventoryParts.addSource(codes) {
            val parts = _inventoryParts.value
            if (parts != null) {
                inventoryParts.value = combine(parts, it)
            }
        }
    }

    private fun combine(parts: List<InventoryPartWithReferences>,
                        codes: MutableMap<Pair<Int, Int>, Code>)
            : List<InventoryPartWithReferences> {
        val partsToFetch = mutableListOf<InventoryPart>()
        parts.forEach { part ->
            val ids = Pair(part.item.id, part.color.id)
            if (ids !in codes) {
                if (ids !in fetchedIds) {
                    fetchedIds.add(ids)
                    partsToFetch.add(part.inventoryPart)
                }
            }
            else {
                part.code = codes[ids]
            }
        }
        if (partsToFetch.isNotEmpty()) {
            fetchCodes(partsToFetch, codes)
        }
        return parts
    }

    private fun fetchCodes(parts: List<InventoryPart>, codes: MutableMap<Pair<Int, Int>, Code>)
            = viewModelScope.launch {
        for (part in parts) {
            launch {
                fetchCode(part.itemId, part.colorId);
            }
        }
    }

    private suspend fun fetchCode(itemId: Int, colorId: Int) {
        var code = brickListDao.getCodeByIds(itemId, colorId)
        if (code == null) {
            code = Code(itemId = itemId, colorId = colorId)
            brickListDao.insertCode(code)
        }

        if (code.image == null) {
            val url = "https://www.lego.com/service/bricks/5/2/${code.code}"
            try {
                val image = requests.requestImage(url, 400, 400)
                val newCode = code.copy(image = image)
                setCode(newCode)
                brickListDao.updateCode(newCode)
            }
            catch (e: ClientError) {
                println("Image not found at url: $url")
            }
        }
        else {
            setCode(code)
        }
    }

    private fun setCode(code: Code) {
        val codesData = codes.value
        val ids = Pair(code.itemId, code.colorId ?: 0)
        if (codesData != null) {
            codesData[ids] = code
            codes.postValue(codesData)
        }
    }
    class Factory(private val application: Application, private val inventoryId: Int)
            : ViewModelProvider.Factory {

        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(PartsListViewModel::class.java)) {
                return modelClass
                    .getConstructor(Application::class.java, inventoryId::class.java)
                    .newInstance(application, inventoryId)
            }
            else {
                throw IllegalArgumentException("Cannot create instance of $modelClass")
            }
        }
    }
}
