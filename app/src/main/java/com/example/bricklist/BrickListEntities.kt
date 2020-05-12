package com.example.bricklist

import android.graphics.Bitmap
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey

@Entity(tableName = "Codes")
data class Code(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val itemId: Int = 0,
    val colorId: Int = 0,
    val code: Int = 0
) {
    @Ignore val image: Bitmap? = null
}

@Entity(tableName = "Colors")
data class Color(
    @PrimaryKey val id: Int = 0,
    var code: Int = 0,
    val name: String = "",
    val namePl: String = ""
)

@Entity(tableName = "ItemTypes")
data class ItemType(
    @PrimaryKey val id: Int = 0,
    var code: String = "",
    val name: String = "",
    val namePl: String = ""
)

@Entity(tableName = "Parts")
data class Item(
    @PrimaryKey val id: Int = 0,
    val typeId: Int = 0,
    var code: String = "",
    val name: String = "",
    val namePl: String = ""
)
