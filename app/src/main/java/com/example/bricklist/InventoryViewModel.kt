package com.example.bricklist

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class InventoryViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = BrickListDatabase.getDatabase(application).getInventoryDao()
    val inventories = dao.getInventories()

    fun insert(inventory: Inventory) = viewModelScope.launch {
        dao.insert(inventory)
    }
}
