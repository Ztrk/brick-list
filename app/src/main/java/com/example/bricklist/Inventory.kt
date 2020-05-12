package com.example.bricklist

import androidx.lifecycle.LiveData
import androidx.room.*
import java.util.*

@Entity(tableName = "Inventories")
data class Inventory(
    @PrimaryKey(autoGenerate = true) var id: Int = 0,
    var name: String = "",
    var active: Boolean = true,
    @Ignore var lastAccessed: Calendar = Calendar.getInstance()
)

data class InventoryWithParts(
    val inventory: Inventory = Inventory(),
    val parts: List<InventoryPartWithReferences> = listOf()
)

@Dao
interface InventoryDao {
    @Query("SELECT * FROM inventories")
    fun getInventories(): LiveData<List<Inventory>>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(inventory: Inventory)
}
