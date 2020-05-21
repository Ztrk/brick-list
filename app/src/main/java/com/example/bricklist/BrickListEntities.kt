package com.example.bricklist

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.room.*
import java.io.ByteArrayOutputStream

@Entity(tableName = "Codes")
data class Code(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "ItemID") val itemId: Int = 0,
    @ColumnInfo(name = "ColorID") val colorId: Int? = 0,
    @ColumnInfo(name = "Code") val code: Int? = null,
    @ColumnInfo(name = "Image") val image: Bitmap? = null
)

class BitmapConverter {
    @TypeConverter
    fun fromByteArray(array: ByteArray?): Bitmap? {
        if (array == null)
            return null
        return BitmapFactory.decodeByteArray(array, 0, array.size)
    }

    @TypeConverter
    fun toByteArray(bitmap: Bitmap?): ByteArray? {
        if (bitmap == null)
            return null
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 0, stream)
        return stream.toByteArray()
    }
}

@Entity(tableName = "Colors")
data class Color(
    @PrimaryKey val id: Int = 0,
    @ColumnInfo(name = "Code") var code: Int = 0,
    @ColumnInfo(name = "Name") val name: String = "",
    @ColumnInfo(name = "NamePL") val namePl: String? = ""
)

@Entity(tableName = "ItemTypes")
data class ItemType(
    @PrimaryKey val id: Int = 0,
    @ColumnInfo(name = "Code") var code: String = "",
    @ColumnInfo(name = "Name") val name: String = "",
    @ColumnInfo(name = "NamePL") val namePl: String? = ""
)

@Entity(tableName = "Parts")
data class Item(
    @PrimaryKey val id: Int = 0,
    @ColumnInfo(name = "TypeID") val typeId: Int = 0,
    @ColumnInfo(name = "CategoryID") val categoryId: Int = 0,
    @ColumnInfo(name = "Code") var code: String = "",
    @ColumnInfo(name = "Name") val name: String = "",
    @ColumnInfo(name = "NamePL") val namePl: String? = ""
)

@Dao
interface BrickListDao {
    @Insert
    suspend fun insertCode(code: Code)

    @Update
    suspend fun updateCode(code: Code)

    @Query("SELECT * FROM Codes WHERE ItemID = :itemId AND ColorID = :colorId LIMIT 1")
    suspend fun getCodeByIds(itemId: Int, colorId: Int): Code?

    @Query("SELECT * FROM Colors WHERE Code = :code LIMIT 1")
    suspend fun getColorByCode(code: Int): Color?

    @Query("SELECT * FROM ItemTypes WHERE Code = :code LIMIT 1")
    suspend fun getItemTypeByCode(code: String): ItemType?

    @Query("SELECT * FROM Parts WHERE Code = :code LIMIT 1")
    suspend fun getItemByCode(code: String): Item?
}
