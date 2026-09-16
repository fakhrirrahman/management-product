package com.example.managementproduct.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "transactions",
    foreignKeys = [ForeignKey(
        entity = ProductEntity::class,
        parentColumns = ["id"],
        childColumns = ["productId"],
        onDelete = ForeignKey.SET_NULL
    )],
    indices = [Index("productId")]
)
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val productId: Int?,
    val type: String,          // "MASUK" atau "KELUAR"
    val quantity: Int,
    val pricePerItem: Long,    // harga jual/beli saat transaksi terjadi
    val costPerItem: Long = 0L,// modal (purchasePrice) saat terjual (hanya dipakai untuk tipe KELUAR)
    val date: Long             // System.currentTimeMillis()
)
