package com.example.managementproduct.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.managementproduct.data.local.dao.ProductDao
import com.example.managementproduct.data.local.entity.ProductEntity

@Database(
    entities = [ProductEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun productDao(): ProductDao
}