package com.example.bricklist

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Inventory::class], version = 1, exportSchema = false)
abstract class BrickListDatabase : RoomDatabase() {
    abstract fun getInventoryDAO(): InventoryDao

    companion object {
        private var INSTANCE: BrickListDatabase? = null

        @Synchronized fun getDatabase(context: Context): BrickListDatabase {
            val tmpInstance = INSTANCE
            if (tmpInstance != null) {
                return tmpInstance
            }
            val instance = Room.databaseBuilder(context,
                BrickListDatabase::class.java, "BrickList.db").build()
            INSTANCE = instance
            return instance
        }
    }
}