package com.example.bricklist

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.android.volley.ClientError
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class InventoryRepository(database: BrickListDatabase, private val requests: NetworkRequests) {
    private val inventoryPartDao = database.getInventoryPartDao()
    private val brickListDao = database.getBrickListDao()
    private val inventoryDao = database.getInventoryDao()

    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.IO + job)

    private var inventoryId: Int? = null
    private val _inventoryParts = MutableLiveData(listOf<InventoryPartWithReferences>())

    suspend fun getInventoryById(inventoryId: Int) = inventoryDao.getInventoryById(inventoryId)
    suspend fun updateInventory(inventory: Inventory) = inventoryDao.updateInventory(inventory)

    suspend fun updateInventoryPart(inventoryPart: InventoryPart) {
        val parts = _inventoryParts.value
        if (parts != null) {
            for (part in parts) {
                if (part.inventoryPart.id == inventoryPart.id) {
                    part.inventoryPart = inventoryPart
                }
            }
            _inventoryParts.postValue(parts)
        }
        inventoryPartDao.updateInventoryPart(inventoryPart)
    }

    fun getInventoryParts(newInventoryId: Int): LiveData<List<InventoryPartWithReferences>> {
        if (inventoryId != newInventoryId) {
            job.cancelChildren()
            inventoryId = newInventoryId
            _inventoryParts.value = listOf()
            scope.launch {
                println("Getting parts for id: $newInventoryId")
                val parts = inventoryPartDao.getInventoryPartsById(newInventoryId)
                _inventoryParts.postValue(parts)
                fetchCodes(parts)
            }
        }
        return _inventoryParts
    }

    private fun fetchCodes(parts: List<InventoryPartWithReferences>) {
        val fetchedIds = hashSetOf<Pair<Int, Int>>()
        for (part in parts) {
            val ids = Pair(part.item.id, part.color.id)
            if (ids !in fetchedIds) {
                fetchedIds.add(ids)
                scope.launch {
                    val code = fetchCode(part.item, part.color)
                    setCode(code, parts)
                }
            }
        }
    }

    private fun setCode(code: Code, parts: List<InventoryPartWithReferences>) {
        for (part in parts) {
            if (part.item.id == code.itemId && part.color.id == code.colorId) {
                part.code = code
            }
        }
        _inventoryParts.postValue(parts)
    }

    private suspend fun fetchCode(item: Item, color: Color): Code {
        val code = getCodeByIds(item.id, color.id)

        if (code.image == null) {
            val urls = getUrls(code.code, item.code, color.code)
            for (url in urls) {
                try {
                    val image = requests.requestImage(url, 400, 400)
                    val newCode = code.copy(image = image)
                    brickListDao.updateCode(newCode)
                    return code
                }
                catch (e: ClientError) {
                    println("Image not found at url: $url")
                }
            }
        }
        return code
    }

    private fun getUrls(code: Int?, itemCode: String, colorCode: Int): List<String> {
        val legoUrl = "https://www.lego.com/service/bricks/5/2/$code"
        val brickLinkUrl = "https://img.bricklink.com/ItemImage/PN/$colorCode/$itemCode.png"
        if (code != null) {
            return listOf(legoUrl, brickLinkUrl)
        }
        return listOf(brickLinkUrl)
    }

    suspend fun insertByCodes(inventory: InventoryWithParts): List<String>
            = withContext(Dispatchers.IO) {
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

                getCodeByIds(item.id, color.id)

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

    private suspend fun getCodeByIds(itemId: Int, colorId: Int): Code {
        var code = brickListDao.getCodeByIds(itemId, colorId)
        if (code == null) {
            code = Code(itemId = itemId, colorId = colorId)
            brickListDao.insertCode(code)
        }
        return code
    }

    companion object {
        private var instance: InventoryRepository? = null

        @Synchronized fun getInstance(database: BrickListDatabase,
                                      requests: NetworkRequests): InventoryRepository {
            val tmpInstance = instance
            if (tmpInstance != null) {
                return tmpInstance
            }
            val newInstance = InventoryRepository(database, requests)
            instance = newInstance
            return newInstance
        }
    }

}
