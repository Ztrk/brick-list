package com.example.bricklist

import android.app.Application
import androidx.lifecycle.*
import androidx.preference.PreferenceManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.util.*
import kotlin.math.max
import kotlin.math.min

const val EXPORT_PATH = "exports/export.xml"

class PartsListViewModel(application: Application, inventoryId: Int)
        : AndroidViewModel(application) {

    private val repository = InventoryRepository.getInstance(
        BrickListDatabase.getDatabase(application),
        NetworkRequests.getInstance(application)
    )

    enum class SortBy { NONE, ITEM, COLOR }

    private val _sortBy = MutableLiveData(SortBy.NONE)

    val inventoryParts: LiveData<List<InventoryPartWithReferences>> =
        Transformations.switchMap(_sortBy) { sortBy ->
            Transformations.map(repository.getInventoryParts(inventoryId)) {
                when (sortBy) {
                    SortBy.ITEM -> it.sortedBy { part -> part.item.code }
                    SortBy.COLOR -> it.sortedBy { part -> part.color.code }
                    else -> it
                }.sortedBy { part ->
                    part.inventoryPart.quantityInSet == part.inventoryPart.quantityInStore
                }
            }
        }

    fun setSortBy(value: SortBy) {
        _sortBy.value = value
    }

    private var inventory: Inventory? = null

    init {
        viewModelScope.launch {
            val tmpInventory = repository.getInventoryById(inventoryId)
            inventory = tmpInventory
            repository.updateInventory(tmpInventory.copy(lastAccessed = Date()))
        }
    }

    private val condition = PreferenceManager.getDefaultSharedPreferences(application)
        .getString("condition", "not_important")

    private val _exportReady = MutableLiveData(false)
    val exportReady: LiveData<Boolean>
        get() = _exportReady

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
                repository.updateInventoryPart(newPart)
            }
        }
    }

    fun handledExport() {
        _exportReady.value = false
    }

    fun exportToXml() {
        inventoryParts.value?.let {
            viewModelScope.launch(Dispatchers.IO) {
                val condition = when (condition) {
                    "new" -> InventoryPartsXmlSerializer.Condition.NEW
                    "used" -> InventoryPartsXmlSerializer.Condition.USED
                    else -> InventoryPartsXmlSerializer.Condition.NOT_IMPORTANT
                }
                val serializer = InventoryPartsXmlSerializer()
                val file = File(getApplication<Application>().filesDir, EXPORT_PATH)
                file.parentFile?.mkdirs()
                file.createNewFile()
                serializer.serialize(it, condition, file)
                _exportReady.postValue(true)
            }
        }
    }

    fun updateDate() {
        inventory?.let {
            viewModelScope.launch {
                repository.updateInventory(it.copy(lastAccessed = Date()))
            }
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
