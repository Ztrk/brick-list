package com.example.bricklist

import androidx.lifecycle.LiveData
import androidx.room.*
import java.util.*

@Entity(tableName = "Inventories")
data class Inventory(
    @ColumnInfo(name = "Active") var active: Boolean = true,
    @PrimaryKey(autoGenerate = true) var id: Int = 0,
    @ColumnInfo(name = "Name") var name: String = "",
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
    @Query("SELECT * FROM inventories")
    fun getInventories(): LiveData<List<Inventory>>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(inventory: Inventory)
}
