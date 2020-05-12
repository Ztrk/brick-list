package com.example.bricklist

import androidx.lifecycle.LiveData
import androidx.room.*
import java.util.*

@Entity(tableName = "Inventories")
class Inventory() {
    @PrimaryKey(autoGenerate = true) var id = 0
    var name = ""
    var active = true
    @Ignore var lastAccessed = Calendar.getInstance()
}

class InventoryWithParts(val parts: List<InventoryPart>) {
    val inventory = Inventory()
}

@Dao
interface InventoryDao {
    @Query("SELECT * FROM inventories")
    fun getInventories(): LiveData<List<Inventory>>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(inventory: Inventory)
}
