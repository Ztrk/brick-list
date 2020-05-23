package com.example.bricklist

import androidx.room.*

@Entity(tableName = "InventoriesParts")
data class InventoryPart(
    @PrimaryKey(autoGenerate = true) var id: Int = 0,
    @ColumnInfo(name = "InventoryID") var inventoryId: Int = 0,
    @ColumnInfo(name = "ItemID") var itemId: Int = 0,
    @ColumnInfo(name = "ColorID") var colorId: Int = 0,
    @ColumnInfo(name = "TypeID") var typeId: Int = 0,
    @ColumnInfo(name = "Extra") var extra: Int = 0,
    @ColumnInfo(name = "QuantityInSet") var quantityInSet: Int = 0,
    @ColumnInfo(name = "QuantityInStore") var quantityInStore: Int = 0,
    @Ignore var alternate: String = ""
)

data class InventoryPartWithReferences(
    @Embedded var inventoryPart: InventoryPart = InventoryPart(),

    @Relation(parentColumn = "ItemID", entityColumn = "id")
    var item: Item = Item(),

    @Relation(parentColumn = "ColorID", entityColumn = "id")
    var color: Color = Color(),

    @Relation(parentColumn = "TypeID", entityColumn = "id")
    var itemType: ItemType = ItemType(),
    @Ignore var code: Code? = null
)

@Dao
interface InventoryPartDao {
    @Transaction
    @Query("SELECT * FROM InventoriesParts WHERE InventoryID = :id")
    suspend fun getInventoryPartsById(id: Int) : List<InventoryPartWithReferences>

    @Update
    suspend fun updateInventoryPart(inventoryPart: InventoryPart)

    @Insert
    suspend fun insertInventoryParts(inventoryPart: List<InventoryPart>)
}
