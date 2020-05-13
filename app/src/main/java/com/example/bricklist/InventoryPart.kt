package com.example.bricklist

import androidx.lifecycle.LiveData
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
    @Embedded val inventoryPart: InventoryPart = InventoryPart(),

    @Relation(parentColumn = "ItemID", entityColumn = "id")
    val item: Item = Item(),

    @Relation(parentColumn = "ColorID", entityColumn = "id")
    val color: Color = Color(),

    @Relation(parentColumn = "TypeID", entityColumn = "id")
    val itemType: ItemType = ItemType()
    //val code: Code = Code()
)

@Dao
interface InventoryPartDao {
    @Transaction
    @Query("SELECT * from inventoriesParts")
    fun getInventoryParts() : LiveData<List<InventoryPartWithReferences>>
}
