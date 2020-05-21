package com.example.bricklist

import androidx.lifecycle.LiveData
import androidx.room.*
import java.util.*

@Entity(tableName = "Inventories")
data class Inventory(
    @PrimaryKey(autoGenerate = true) var id: Int = 0,
    @ColumnInfo(name = "Name") var name: String = "",
    @ColumnInfo(name = "Active") var active: Boolean = true,
    @ColumnInfo(name = "LastAccessed") var lastAccessed: Date = Date()
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

class DateConverter {
    @TypeConverter
    fun fromTimestamp(timestamp: Long): Date {
        return Date(timestamp * 1000)
    }

    @TypeConverter
    fun toTimestamp(date: Date): Long {
        return date.time / 1000
    }
}

@Dao
interface InventoryDao {
    @Query("SELECT * FROM Inventories")
    fun getInventories(): LiveData<List<Inventory>>

    @Query("SELECT * FROM Inventories WHERE id = :id")
    suspend fun getInventoryById(id: Int): Inventory

    @Update
    suspend fun updateInventory(inventory: Inventory)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertInventory(inventory: Inventory): Long
}
