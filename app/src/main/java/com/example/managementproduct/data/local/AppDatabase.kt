package com.example.managementproduct.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.managementproduct.data.local.dao.ProductDao
import com.example.managementproduct.data.local.entity.ProductEntity
import com.example.managementproduct.data.local.entity.TransactionEntity

@Database(
    entities = [ProductEntity::class, TransactionEntity::class],
    version = 5,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun productDao(): ProductDao
    abstract fun transactionDao(): com.example.managementproduct.data.local.dao.TransactionDao
}