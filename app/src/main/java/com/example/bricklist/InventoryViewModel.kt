package com.example.bricklist

import android.app.Application
import androidx.lifecycle.*
import androidx.preference.PreferenceManager
import kotlinx.coroutines.launch

class InventoryViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = BrickListDatabase.getDatabase(application).getInventoryDao()

    private val _showArchived = MutableLiveData(false)
    val showArchived: LiveData<Boolean>
        get() = _showArchived

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
            newInventories.sortedByDescending { inventory -> inventory.lastAccessed }
        }
    }

    fun archive(position: Int) {
        inventories.value?.let {
            val inventory = it[position]
            viewModelScope.launch {
                dao.updateInventory(inventory.copy(active=false))
            }
        }
    }
}
