package com.example.bricklist

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import java.lang.IllegalArgumentException

class PartsListViewModel(application: Application, inventoryId: Int)
        : AndroidViewModel(application) {

    private val inventoryPartDao = BrickListDatabase.getDatabase(application).getInventoryPartDao()

    val inventoryParts = inventoryPartDao.getInventoryPartsById(inventoryId)

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
