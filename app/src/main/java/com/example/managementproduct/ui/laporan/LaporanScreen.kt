package com.example.managementproduct.ui.laporan

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.managementproduct.model.ProfitReport
import com.example.managementproduct.model.ReportRange
import com.example.managementproduct.util.formatRupiah

@Composable
fun LaporanScreen(
    viewModel: LaporanViewModel
) {
    val selectedRange by viewModel.selectedRange.collectAsStateWithLifecycle()
    val profitReport by viewModel.profitReport.collectAsStateWithLifecycle()

    LaporanScreenContent(
        selectedRange = selectedRange,
        profitReport = profitReport,
        onRangeSelected = { viewModel.setReportRange(it) }
    )
}

@Composable
fun LaporanScreenContent(
    selectedRange: ReportRange,
    profitReport: ProfitReport?,
    onRangeSelected: (ReportRange) -> Unit
) {
    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.primary)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Laporan Keuntungan",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Segmented Control
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(24.dp))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                listOf(ReportRange.HARI_INI to "Hari Ini", ReportRange.BULAN_INI to "Bulan Ini", ReportRange.SEMUA to "Sejak Awal").forEach { (range, label) ->
                    val isSelected = range == selectedRange
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent)
                            .clickable { onRangeSelected(range) }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            if (profitReport == null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        // Profit Card
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Column(modifier = Modifier.padding(24.dp).fillMaxWidth()) {
                                Text("Laba Bersih", color = Color.White.copy(alpha = 0.8f))
                                Text(
                                    text = profitReport.labaBersih.formatRupiah(),
                                    color = Color.White,
                                    fontSize = 32.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    item {
                        // Revenue & Cost Cards
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Card(
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text("Pendapatan", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(profitReport.pendapatan.formatRupiah(), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                }
                            }
                            Card(
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text("Modal Terjual", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(profitReport.modalTerjual.formatRupiah(), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                    }

                    item {
                        // Chart
                        Text("Tren Laba (7 Hari)", fontWeight = FontWeight.Bold, fontSize = 16.sp, modifier = Modifier.padding(top = 8.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().height(150.dp).padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Bottom
                            ) {
                                val maxProfit = profitReport.trendLaba.maxOfOrNull { it.profit }?.coerceAtLeast(1L) ?: 1L
                                profitReport.trendLaba.forEach { daily ->
                                    val heightFraction = (daily.profit.toFloat() / maxProfit.toFloat()).coerceIn(0f, 1f)
                                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Bottom, modifier = Modifier.fillMaxHeight()) {
                                        Box(
                                            modifier = Modifier
                                                .width(24.dp)
                                                .fillMaxHeight(heightFraction)
                                                .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(daily.dateString.substringBefore(" "), fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant) // Just date number
                                    }
                                }
                            }
                        }
                    }

                    item {
                        Text("Barang Terlaris", fontWeight = FontWeight.Bold, fontSize = 16.sp, modifier = Modifier.padding(top = 8.dp))
                    }

                    if (profitReport.topSelling.isEmpty()) {
                        item {
                            Text("Belum ada data penjualan.", color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(16.dp))
                        }
                    } else {
                        items(profitReport.topSelling) { item ->
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier.size(40.dp).background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(item.productName.take(1).uppercase(), color = MaterialTheme.colorScheme.onPrimaryContainer, fontWeight = FontWeight.Bold)
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(item.productName, fontWeight = FontWeight.Bold)
                                        Text("${item.totalQuantity} terjual", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                                    }
                                }
                                Text("+${item.totalProfitContribution.formatRupiah()}", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
fun LaporanScreenPreview() {
    com.example.managementproduct.ui.theme.ManagementProductTheme {
        LaporanScreenContent(
            selectedRange = ReportRange.HARI_INI,
            profitReport = ProfitReport(
                pendapatan = 150000,
                modalTerjual = 100000,
                labaBersih = 50000,
                trendLaba = listOf(
                    com.example.managementproduct.model.DailyProfit("12 Sep", 10000),
                    com.example.managementproduct.model.DailyProfit("13 Sep", 15000),
                    com.example.managementproduct.model.DailyProfit("14 Sep", 25000)
                ),
                topSelling = listOf(
                    com.example.managementproduct.model.TopSellingItem("Indomie Goreng", 10, 15000),
                    com.example.managementproduct.model.TopSellingItem("Kopi Kapal Api", 5, 5000)
                )
            ),
            onRangeSelected = {}
        )
    }
}
