package com.example.managementproduct.model

import com.example.managementproduct.data.local.entity.ProductEntity

data class CartItem(
    val product: ProductEntity,
    val quantity: Int
)

enum class ReportRange {
    HARI_INI,
    BULAN_INI,
    SEMUA
}

data class ProfitReport(
    val pendapatan: Long,
    val modalTerjual: Long,
    val labaBersih: Long,
    val trendLaba: List<DailyProfit>, // 7 hari terakhir
    val topSelling: List<TopSellingItem> // 3 terlaris
)

data class DailyProfit(
    val dateString: String,
    val profit: Long
)

data class TopSellingItem(
    val productName: String,
    val totalQuantity: Int,
    val totalProfitContribution: Long
)
