package com.example.bricklist

import androidx.lifecycle.LiveData
import androidx.room.*

@Entity(tableName = "InventoriesParts")
data class InventoryPart(
    @PrimaryKey(autoGenerate = true) var id: Int = 0,
    var inventoryId: Int = 0,
    var itemId: Int = 0,
    var colorId: Int = 0,
    var typeId: Int = 0,
    var extra: String = "",
    var quantityInSet: Int = 0,
    var quantityInStore: Int = 0,
    @Ignore var alternate: String = ""
)

data class InventoryPartWithReferences(
    @Embedded val inventoryPart: InventoryPart = InventoryPart(),

    @Relation(parentColumn = "itemId", entityColumn = "id")
    val item: Item = Item(),

    @Relation(parentColumn = "colorId", entityColumn = "id")
    val color: Color = Color(),

    @Relation(parentColumn = "typeId", entityColumn = "id")
    val itemType: ItemType = ItemType(),
    val code: Code = Code()
)

@Dao
interface InventoryPartDao {
    @Query("SELECT * from inventoriesParts")
    fun getInventoryParts() : LiveData<List<InventoryPartWithReferences>>
}
