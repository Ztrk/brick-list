package com.example.bricklist

import androidx.lifecycle.LiveData
import androidx.room.*

@Entity(tableName = "Inventories")
data class Inventory(
    @PrimaryKey(autoGenerate = true) var id: Int = 0,
    @ColumnInfo(name = "Name") var name: String = "",
    @ColumnInfo(name = "Active") var active: Boolean = true,
    //var lastAccessed: Calendar = Calendar.getInstance()
    @ColumnInfo(name = "LastAccessed") var lastAccessed: Int = 0
)

data class InventoryWithParts(
    val inventory: Inventory = Inventory(),
    @Relation(
        entity = InventoryPart::class,
        parentColumn = "id",
        entityColumn = "InventoryID"
    )
    val parts: List<InventoryPartWithReferences> = listOf()
)

@Dao
interface InventoryDao {
    @Query("SELECT * FROM Inventories")
    fun getInventories(): LiveData<List<Inventory>>

    @Update
    suspend fun updateInventory(inventory: Inventory)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertInventory(inventory: Inventory): Long
}
