package com.example.managementproduct.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val category: String,
    val purchasePrice: Long,
    val sellingPrice: Long,
    val stock: Int = 0
)