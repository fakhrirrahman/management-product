package com.example.managementproduct.data.repository

import androidx.room.withTransaction
import com.example.managementproduct.data.local.AppDatabase
import com.example.managementproduct.data.local.entity.ProductEntity
import com.example.managementproduct.data.local.entity.TransactionEntity
import com.example.managementproduct.data.local.entity.TransactionWithProduct
import com.example.managementproduct.model.CartItem
import com.example.managementproduct.model.DailyProfit
import com.example.managementproduct.model.ProfitReport
import com.example.managementproduct.model.ReportRange
import com.example.managementproduct.model.TopSellingItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class KiosRepository(
    private val db: AppDatabase
) {
    private val productDao = db.productDao()
    private val transactionDao = db.transactionDao()

    // 1. Data Products (Master Data)
    fun getAllProducts(): Flow<List<ProductEntity>> = productDao.getAllProducts()

    suspend fun insertProduct(product: ProductEntity) = productDao.insertProduct(product)
    
    suspend fun updateProduct(product: ProductEntity) = productDao.updateProduct(product)
    
    suspend fun deleteProduct(product: ProductEntity) = productDao.deleteProduct(product)

    // 2. Transaksi Barang Masuk (Restock)
    suspend fun getProductByBarcode(barcode: String): ProductEntity? = productDao.getProductByBarcode(barcode)

    suspend fun restock(productId: Int, quantity: Int, purchasePricePerItem: Long) {
        if (quantity <= 0) throw IllegalArgumentException("Quantity must be greater than 0")
        
        db.withTransaction {
            val product = productDao.getProductById(productId) ?: throw Exception("Product not found")
            
            // Catat transaksi masuk
            val transaction = TransactionEntity(
                productId = productId,
                type = "MASUK",
                quantity = quantity,
                pricePerItem = purchasePricePerItem,
                date = System.currentTimeMillis()
            )
            transactionDao.insertTransaction(transaction)

            // Update stok dan harga beli terbaru di master barang
            val newStock = product.stock + quantity
            val updatedProduct = product.copy(
                stock = newStock,
                purchasePrice = purchasePricePerItem
            )
            productDao.updateProduct(updatedProduct)
        }
    }

    // 3. Transaksi Barang Keluar (Checkout Kasir)
    suspend fun checkout(cartItems: List<CartItem>) {
        if (cartItems.isEmpty()) throw IllegalArgumentException("Cart is empty")

        db.withTransaction {
            for (item in cartItems) {
                if (item.quantity <= 0) continue
                
                val product = productDao.getProductById(item.product.id) ?: throw Exception("Product not found")
                
                if (product.stock < item.quantity) {
                    throw Exception("Stok tidak cukup untuk ${product.name}")
                }

                // Catat transaksi keluar
                val transaction = TransactionEntity(
                    productId = product.id,
                    type = "KELUAR",
                    quantity = item.quantity,
                    pricePerItem = product.sellingPrice,
                    costPerItem = product.purchasePrice, // Simpan modal saat ini untuk laporan
                    date = System.currentTimeMillis()
                )
                transactionDao.insertTransaction(transaction)

                // Kurangi stok
                productDao.updateStock(product.id, product.stock - item.quantity)
            }
        }
    }

    // 4. Laporan Keuntungan
    fun getProfitReport(range: ReportRange): Flow<ProfitReport> {
        val (startDate, endDate) = getStartAndEndDate(range)
        
        return transactionDao.getTransactionsBetween(startDate, endDate).map { allTransactions ->
            val outTransactions = allTransactions.filter { it.transaction.type == "KELUAR" }

            var totalPendapatan = 0L
            var totalModalTerjual = 0L

            val productSalesMap = mutableMapOf<String, TopSellingItemBuilder>()
            val dailyProfitMap = mutableMapOf<String, Long>()
            val dateFormat = java.text.SimpleDateFormat("dd MMM", java.util.Locale.forLanguageTag("id-ID"))

            for (t in outTransactions) {
                val trans = t.transaction
                val productName = t.product?.name ?: "Barang Terhapus"
                
                val revenue = trans.quantity * trans.pricePerItem
                val cost = trans.quantity * trans.costPerItem
                val profit = revenue - cost
                
                totalPendapatan += revenue
                totalModalTerjual += cost

                // Hitung untuk top selling
                val builder = productSalesMap.getOrPut(productName) { TopSellingItemBuilder(productName, 0, 0L) }
                builder.quantity += trans.quantity
                builder.profit += profit

                // Hitung untuk daily profit trend
                val dateStr = dateFormat.format(Date(trans.date))
                dailyProfitMap[dateStr] = dailyProfitMap.getOrDefault(dateStr, 0L) + profit
            }

            val labaBersih = totalPendapatan - totalModalTerjual

            val topSelling = productSalesMap.values
                .sortedByDescending { it.quantity }
                .take(3)
                .map { TopSellingItem(it.productName, it.quantity, it.profit) }

            // Generate 7 hari terakhir agar harinya urut, walau tidak ada transaksi isinya 0
            val trendLaba = generateLast7Days(dailyProfitMap, dateFormat)

            ProfitReport(
                pendapatan = totalPendapatan,
                modalTerjual = totalModalTerjual,
                labaBersih = labaBersih,
                trendLaba = trendLaba,
                topSelling = topSelling
            )
        }
    }

    private fun getStartAndEndDate(range: ReportRange): Pair<Long, Long> {
        val startCalendar = Calendar.getInstance()
        val endCalendar = Calendar.getInstance()
        
        when (range) {
            ReportRange.HARI_INI -> {
                startCalendar.set(Calendar.HOUR_OF_DAY, 0)
                startCalendar.set(Calendar.MINUTE, 0)
                startCalendar.set(Calendar.SECOND, 0)
                startCalendar.set(Calendar.MILLISECOND, 0)
                
                endCalendar.set(Calendar.HOUR_OF_DAY, 23)
                endCalendar.set(Calendar.MINUTE, 59)
                endCalendar.set(Calendar.SECOND, 59)
                endCalendar.set(Calendar.MILLISECOND, 999)
            }
            ReportRange.BULAN_INI -> {
                startCalendar.set(Calendar.DAY_OF_MONTH, 1)
                startCalendar.set(Calendar.HOUR_OF_DAY, 0)
                startCalendar.set(Calendar.MINUTE, 0)
                startCalendar.set(Calendar.SECOND, 0)
                startCalendar.set(Calendar.MILLISECOND, 0)
                
                endCalendar.set(Calendar.DAY_OF_MONTH, endCalendar.getActualMaximum(Calendar.DAY_OF_MONTH))
                endCalendar.set(Calendar.HOUR_OF_DAY, 23)
                endCalendar.set(Calendar.MINUTE, 59)
                endCalendar.set(Calendar.SECOND, 59)
                endCalendar.set(Calendar.MILLISECOND, 999)
            }
            ReportRange.SEMUA -> {
                startCalendar.timeInMillis = 0L // Sejak awal waktu
                endCalendar.timeInMillis = Long.MAX_VALUE // Sampai akhir zaman
            }
        }
        return Pair(startCalendar.timeInMillis, endCalendar.timeInMillis)
    }

    private fun generateLast7Days(dailyProfitMap: Map<String, Long>, format: SimpleDateFormat): List<DailyProfit> {
        val list = mutableListOf<DailyProfit>()
        val cal = Calendar.getInstance()
        
        // Mundur 6 hari ke belakang, sampai hari ini
        for (i in 6 downTo 0) {
            val date = cal.clone() as Calendar
            date.add(Calendar.DAY_OF_YEAR, -i)
            val dateStr = format.format(date.time)
            list.add(DailyProfit(dateStr, dailyProfitMap.getOrDefault(dateStr, 0L)))
        }
        return list
    }

    private class TopSellingItemBuilder(
        val productName: String,
        var quantity: Int,
        var profit: Long
    )
}
