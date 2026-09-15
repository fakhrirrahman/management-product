package com.example.managementproduct.data.local.entity

import androidx.room.Embedded
import androidx.room.Relation

data class TransactionWithProduct(
    @Embedded val transaction: TransactionEntity,
    @Relation(
        parentColumn = "productId",
        entityColumn = "id"
    )
    val product: ProductEntity
)
