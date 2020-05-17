package com.example.bricklist

import android.app.Application
import androidx.lifecycle.*
import com.android.volley.ClientError
import kotlinx.coroutines.launch
import kotlin.math.max
import kotlin.math.min

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

    fun incrementQuantity(position: Int) {
        changeQuantity(position) { inStore, inSet ->
            min(inStore + 1, inSet)
        }
    }

    fun decrementQuantity(position: Int) {
        changeQuantity(position) { inStore, _ ->
            max(inStore - 1, 0)
        }
    }

    private fun changeQuantity(position: Int, computeQuantity: (Int, Int) -> Int) {
        val parts = inventoryParts.value
        if (parts != null) {
            val newPart = parts[position].inventoryPart.let {
                val newQuantity = computeQuantity(it.quantityInStore, it.quantityInSet)
                it.copy(quantityInStore = newQuantity)
            }
            viewModelScope.launch {
                inventoryPartDao.updateInventoryPart(newPart)
            }
        }
    }

    private fun combine(parts: List<InventoryPartWithReferences>,
                        codes: MutableMap<Pair<Int, Int>, Code>)
            : List<InventoryPartWithReferences> {
        val partsToFetch = mutableListOf<InventoryPartWithReferences>()
        parts.forEach { part ->
            val ids = Pair(part.item.id, part.color.id)
            if (ids !in codes) {
                if (ids !in fetchedIds) {
                    fetchedIds.add(ids)
                    partsToFetch.add(part)
                }
            }
            else {
                part.code = codes[ids]
            }
        }
        if (partsToFetch.isNotEmpty()) {
            fetchCodes(partsToFetch)
        }
        return parts
    }

    private fun fetchCodes(parts: List<InventoryPartWithReferences>)
            = viewModelScope.launch {
        for (part in parts) {
            launch {
                fetchCode(part.item, part.color)
            }
        }
    }

    private suspend fun fetchCode(item: Item, color: Color) {
        var code = brickListDao.getCodeByIds(item.id, color.id)
        if (code == null) {
            code = Code(itemId = item.id, colorId = color.id)
            brickListDao.insertCode(code)
        }

        if (code.image == null) {
            val urls = getUrls(code.code, item.code, color.code)
            for (url in urls) {
                try {
                    val image = requests.requestImage(url, 400, 400)
                    val newCode = code.copy(image = image)
                    setCode(newCode)
                    brickListDao.updateCode(newCode)
                    break
                }
                catch (e: ClientError) {
                    println("Image not found at url: $url")
                }
            }
        }
        else {
            setCode(code)
        }
    }

    private fun getUrls(code: Int?, itemCode: String, colorCode: Int): List<String> {
        val legoUrl = "https://www.lego.com/service/bricks/5/2/$code"
        val brickLinkUrl = "https://img.bricklink.com/ItemImage/PN/$colorCode/$itemCode.png"
        if (code != null) {
            return listOf(legoUrl, brickLinkUrl)
        }
        return listOf(brickLinkUrl)
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
