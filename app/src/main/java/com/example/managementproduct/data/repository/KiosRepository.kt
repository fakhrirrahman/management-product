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
    suspend fun getProfitReport(range: ReportRange): ProfitReport {
        val (startDate, endDate) = getStartAndEndDate(range)
        
        // Ambil semua transaksi KELUAR dalam rentang tanggal (first() mengambil data satu kali dari Flow)
        val transactionsFlow = transactionDao.getTransactionsBetween(startDate, endDate)
        val allTransactions = transactionsFlow.first()
        val outTransactions = allTransactions.filter { it.transaction.type == "KELUAR" }

        var totalPendapatan = 0L
        var totalModalTerjual = 0L

        val productSalesMap = mutableMapOf<String, TopSellingItemBuilder>()
        val dailyProfitMap = mutableMapOf<String, Long>()
        val dateFormat = SimpleDateFormat("dd MMM", Locale("in", "ID"))

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

        return ProfitReport(
            pendapatan = totalPendapatan,
            modalTerjual = totalModalTerjual,
            labaBersih = labaBersih,
            trendLaba = trendLaba,
            topSelling = topSelling
        )
    }

    private fun getStartAndEndDate(range: ReportRange): Pair<Long, Long> {
        val calendar = Calendar.getInstance()
        val endDate = System.currentTimeMillis()
        
        when (range) {
            ReportRange.HARI_INI -> {
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
            }
            ReportRange.BULAN_INI -> {
                calendar.set(Calendar.DAY_OF_MONTH, 1)
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
            }
            ReportRange.SEMUA -> {
                calendar.timeInMillis = 0L // Sejak awal waktu
            }
        }
        return Pair(calendar.timeInMillis, endDate)
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
