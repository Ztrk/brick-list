package com.example.bricklist

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Inventory::class, InventoryPart::class, Code::class,
    Color::class, ItemType::class, Item::class], version = 1, exportSchema = false)
abstract class BrickListDatabase : RoomDatabase() {

    abstract fun getInventoryDao(): InventoryDao

    abstract fun getInventoryPartDao(): InventoryPartDao

    abstract fun getBrickListDao(): BrickListDao

    companion object {
        private var INSTANCE: BrickListDatabase? = null

        @Synchronized fun getDatabase(context: Context): BrickListDatabase {
            val tmpInstance = INSTANCE
            if (tmpInstance != null) {
                return tmpInstance
            }
            val instance = Room.databaseBuilder(context,
                BrickListDatabase::class.java, "BrickList.db")
                .createFromAsset("BrickList.db")
                .build()
            INSTANCE = instance
            return instance
        }
    }
}