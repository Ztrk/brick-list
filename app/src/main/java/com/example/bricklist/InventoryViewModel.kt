package com.example.bricklist

import android.app.Application
import androidx.lifecycle.*
import kotlinx.coroutines.launch

class InventoryViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = BrickListDatabase.getDatabase(application).getInventoryDao()

    private val _showArchived = MutableLiveData(false)
    val showArchived: LiveData<Boolean>
        get() = _showArchived

    private var lastArchived: Inventory? = null

    fun setArchive(value: Boolean) {
        _showArchived.value = value
    }

    val inventories: LiveData<List<Inventory>> = Transformations.switchMap(_showArchived)
    { showArchived ->
        Transformations.map(dao.getInventories()) {
            val newInventories =
                if (!showArchived)
                    it.filter { inventory -> inventory.active }
                else
                    it
            newInventories.sortedByDescending { inventory -> inventory.lastAccessed.time }
        }
    }

    fun archive(position: Int): Inventory? {
        inventories.value?.let {
            val inventory = it[position]
            lastArchived = inventory
            viewModelScope.launch {
                dao.updateInventory(inventory.copy(active = false))
            }
            return inventory
        }
        return null
    }

    fun dearchive() {
        val newInventory = lastArchived?.copy(active = true)
        newInventory?.let {
            viewModelScope.launch {
                dao.updateInventory(newInventory)
            }
        }
    }
}
