package com.example.managementproduct.ui.inventaris

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.items as lazyRowItems
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.managementproduct.data.local.entity.ProductEntity
import com.example.managementproduct.ui.theme.ManagementProductTheme
import com.example.managementproduct.util.formatRupiah

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventarisScreen(
    viewModel: InventarisViewModel
) {
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val products by viewModel.products.collectAsStateWithLifecycle()
    
    var showRestockSheet by remember { mutableStateOf<ProductEntity?>(null) }
    var showAddProductSheet by remember { mutableStateOf(false) }
    var showEditProductSheet by remember { mutableStateOf<ProductEntity?>(null) }

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
                    text = "Inventaris",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Stok & barang masuk",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 14.sp
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
            // Search Bar & Add Button
            Column(modifier = Modifier.padding(16.dp)) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.onSearchQueryChanged(it) },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Cari barang di inventaris...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    )
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                OutlinedButton(
                    onClick = { showAddProductSheet = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Barang baru")
                }
            }

            // Product List
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(products) { product ->
                    InventoryItemCard(
                        product = product,
                        onRestockClick = { showRestockSheet = product },
                        onEditClick = { showEditProductSheet = product }
                    )
                }
            }
        }
    }

    showRestockSheet?.let { restockProduct ->
        ModalBottomSheet(onDismissRequest = { showRestockSheet = null }, containerColor = MaterialTheme.colorScheme.surface) {
            RestockSheet(
                product = restockProduct,
                onConfirm = { qty, price ->
                    viewModel.restock(
                        productId = restockProduct.id,
                        quantity = qty,
                        purchasePricePerItem = price,
                        onSuccess = { showRestockSheet = null },
                        onError = { /* show error */ }
                    )
                },
                onCancel = { showRestockSheet = null }
            )
        }
    }

    if (showAddProductSheet) {
        ModalBottomSheet(onDismissRequest = { showAddProductSheet = false }, containerColor = MaterialTheme.colorScheme.surface) {
            AddProductSheet(
                onConfirm = { name, category, purchasePrice, sellingPrice, stock, barcode ->
                    viewModel.addProduct(name, category, purchasePrice, sellingPrice, stock, barcode) {
                        showAddProductSheet = false
                    }
                },
                onCancel = { showAddProductSheet = false }
            )
        }
    }

    showEditProductSheet?.let { editProduct ->
        ModalBottomSheet(onDismissRequest = { showEditProductSheet = null }, containerColor = MaterialTheme.colorScheme.surface) {
            EditProductSheet(
                product = editProduct,
                onUpdate = { updatedProduct ->
                    viewModel.updateProduct(updatedProduct) {
                        showEditProductSheet = null
                    }
                },
                onDelete = {
                    viewModel.deleteProduct(editProduct) {
                        showEditProductSheet = null
                    }
                },
                onCancel = { showEditProductSheet = null }
            )
        }
    }
}

@Composable
fun InventoryItemCard(
    product: ProductEntity,
    onRestockClick: () -> Unit,
    onEditClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = product.name.take(2).uppercase(),
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(text = product.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(
                    text = "Modal ${product.purchasePrice.formatRupiah()} · Jual ${product.sellingPrice.formatRupiah()}",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                
                val (statusColor, statusText) = when {
                    product.stock > 15 -> MaterialTheme.colorScheme.primary to "Stok aman"
                    product.stock > 5 -> MaterialTheme.colorScheme.secondary to "Stok sedang"
                    else -> MaterialTheme.colorScheme.error to "Stok menipis"
                }
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .background(statusColor.copy(alpha = 0.1f), RoundedCornerShape(16.dp))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Box(modifier = Modifier.size(6.dp).background(statusColor, CircleShape))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "$statusText · ${product.stock} pcs", color = statusColor, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }
            
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onEditClick) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit", tint = MaterialTheme.colorScheme.primary)
                }
                Button(
                    onClick = onRestockClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                ) {
                    Text("+ Stok")
                }
            }
        }
    }
}

@Composable
fun RestockSheet(
    product: ProductEntity,
    onConfirm: (Int, Long) -> Unit,
    onCancel: () -> Unit
) {
    var quantity by remember { mutableStateOf(1) }
    var price by remember { mutableStateOf(product.purchasePrice.toString()) }
    
    Column(modifier = Modifier.padding(16.dp)) {
        Text("Tambah Stok: ${product.name}", fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Spacer(modifier = Modifier.height(16.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Jumlah Stok Masuk")
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { if (quantity > 1) quantity-- }, modifier = Modifier.background(MaterialTheme.colorScheme.outline, CircleShape).size(32.dp)) {
                    Icon(Icons.Default.Remove, contentDescription = "-")
                }
                Text("$quantity", modifier = Modifier.padding(horizontal = 16.dp), fontWeight = FontWeight.Bold)
                IconButton(onClick = { quantity++ }, modifier = Modifier.background(MaterialTheme.colorScheme.primaryContainer, CircleShape).size(32.dp)) {
                    Icon(Icons.Default.Add, contentDescription = "+", tint = MaterialTheme.colorScheme.onPrimaryContainer)
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        OutlinedTextField(
            value = price,
            onValueChange = { if (it.isEmpty() || it.all { char -> char.isDigit() }) price = it },
            label = { Text("Harga Modal (Per Pcs)") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        val total = quantity * (price.toLongOrNull() ?: 0L)
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Total Pembelian", fontWeight = FontWeight.Bold)
            Text(total.formatRupiah(), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Button(
            onClick = { onConfirm(quantity, price.toLongOrNull() ?: 0L) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Simpan Stok")
        }
        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun AddProductSheet(
    onConfirm: (String, String, Long, Long, Int, String?) -> Unit,
    onCancel: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Makanan") }
    var purchasePrice by remember { mutableStateOf("") }
    var sellingPrice by remember { mutableStateOf("") }
    var stock by remember { mutableStateOf("") }
    var barcode by remember { mutableStateOf<String?>(null) }
    
    val context = androidx.compose.ui.platform.LocalContext.current

    val categories = listOf("Makanan", "Minuman", "Sembako", "Rokok", "Lainnya")
    
    val scanLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = com.journeyapps.barcodescanner.ScanContract(),
        onResult = { result ->
            if (result.contents != null) {
                barcode = result.contents
            }
        }
    )

    Column(modifier = Modifier.padding(16.dp)) {
        Text("Barang Baru", fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Spacer(modifier = Modifier.height(16.dp))
        
        OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nama Barang") }, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(8.dp))
        
        // Simple Dropdown substitute using segmented buttons or just lazy row for category selection
        Text("Kategori", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            lazyRowItems(categories) { cat ->
                FilterChip(
                    selected = category == cat,
                    onClick = { category = cat },
                    label = { Text(cat) },
                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = MaterialTheme.colorScheme.primary, selectedLabelColor = Color.White)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(value = purchasePrice, onValueChange = { purchasePrice = it }, label = { Text("Harga Modal") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.weight(1f))
            OutlinedTextField(value = sellingPrice, onValueChange = { sellingPrice = it }, label = { Text("Harga Jual") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.weight(1f))
        }
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(value = stock, onValueChange = { stock = it }, label = { Text("Stok Awal") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
        
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = barcode ?: "",
                onValueChange = { barcode = it },
                label = { Text("Barcode (Opsional)") },
                modifier = Modifier.weight(1f)
            )
            IconButton(
                onClick = {
                    val options = com.journeyapps.barcodescanner.ScanOptions()
                    options.setDesiredBarcodeFormats(com.journeyapps.barcodescanner.ScanOptions.ALL_CODE_TYPES)
                    options.setPrompt("Arahkan ke Barcode")
                    options.setBeepEnabled(true)
                    options.setOrientationLocked(false)
                    scanLauncher.launch(options)
                },
                modifier = Modifier.background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(12.dp))
            ) {
                Icon(androidx.compose.material.icons.Icons.Default.CameraAlt, contentDescription = "Scan", tint = MaterialTheme.colorScheme.onPrimaryContainer)
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = { onConfirm(name, category, purchasePrice.toLongOrNull() ?: 0, sellingPrice.toLongOrNull() ?: 0, stock.toIntOrNull() ?: 0, barcode) },
            modifier = Modifier.fillMaxWidth(),
            enabled = name.isNotBlank() && purchasePrice.isNotBlank() && sellingPrice.isNotBlank()
        ) {
            Text("Simpan Barang")
        }
        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Preview(showBackground = true)
@Composable
fun InventoryItemCardPreview() {
    ManagementProductTheme {
        InventoryItemCard(
            product = ProductEntity(
                id = 1,
                name = "Kopi Kapal Api",
                category = "Minuman",
                purchasePrice = 1200,
                sellingPrice = 1500,
                stock = 20
            ),
            onRestockClick = {},
            onEditClick = TODO()
        )
    }
}

@Preview(showBackground = true)
@Composable
fun RestockSheetPreview() {
    ManagementProductTheme {
        RestockSheet(
            product = ProductEntity(id = 1, name = "Kopi Kapal Api", category = "Minuman", purchasePrice = 1200, sellingPrice = 1500, stock = 20),
            onConfirm = { _, _ -> },
            onCancel = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun AddProductSheetPreview() {
    ManagementProductTheme {
        AddProductSheet(
            onConfirm = { _, _, _, _, _, _ -> },
            onCancel = {}
        )
    }
}

@Composable
fun EditProductSheet(
    product: ProductEntity,
    onUpdate: (ProductEntity) -> Unit,
    onDelete: () -> Unit,
    onCancel: () -> Unit
) {
    var name by remember { mutableStateOf(product.name) }
    var category by remember { mutableStateOf(product.category) }
    var purchasePrice by remember { mutableStateOf(product.purchasePrice.toString()) }
    var sellingPrice by remember { mutableStateOf(product.sellingPrice.toString()) }
    var barcode by remember { mutableStateOf(product.barcode) }
    
    var showDeleteConfirm by remember { mutableStateOf(false) }

    val categories = listOf("Makanan", "Minuman", "Sembako", "Rokok", "Lainnya")
    val context = androidx.compose.ui.platform.LocalContext.current
    
    val scanLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = com.journeyapps.barcodescanner.ScanContract(),
        onResult = { result ->
            if (result.contents != null) {
                barcode = result.contents
            }
        }
    )

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Hapus Barang?") },
            text = { Text("Apakah Anda yakin ingin menghapus '${product.name}'? Data barang ini akan hilang.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirm = false
                        onDelete()
                    }
                ) {
                    Text("Hapus", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Batal")
                }
            }
        )
    }

    Column(modifier = Modifier.padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Edit Barang", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            IconButton(onClick = { showDeleteConfirm = true }) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        
        OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nama Barang") }, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(8.dp))
        
        Text("Kategori", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            lazyRowItems(categories) { cat ->
                FilterChip(
                    selected = category == cat,
                    onClick = { category = cat },
                    label = { Text(cat) },
                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = MaterialTheme.colorScheme.primary, selectedLabelColor = Color.White)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(value = purchasePrice, onValueChange = { if (it.isEmpty() || it.all { char -> char.isDigit() }) purchasePrice = it }, label = { Text("Harga Modal") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.weight(1f))
            OutlinedTextField(value = sellingPrice, onValueChange = { if (it.isEmpty() || it.all { char -> char.isDigit() }) sellingPrice = it }, label = { Text("Harga Jual") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.weight(1f))
        }
        
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = barcode ?: "",
                onValueChange = { barcode = it },
                label = { Text("Barcode (Opsional)") },
                modifier = Modifier.weight(1f)
            )
            IconButton(
                onClick = {
                    val options = com.journeyapps.barcodescanner.ScanOptions()
                    options.setDesiredBarcodeFormats(com.journeyapps.barcodescanner.ScanOptions.ALL_CODE_TYPES)
                    options.setPrompt("Arahkan ke Barcode")
                    options.setBeepEnabled(true)
                    options.setOrientationLocked(false)
                    scanLauncher.launch(options)
                },
                modifier = Modifier.background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(12.dp))
            ) {
                Icon(androidx.compose.material.icons.Icons.Default.CameraAlt, contentDescription = "Scan", tint = MaterialTheme.colorScheme.onPrimaryContainer)
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = { 
                val updatedProduct = product.copy(
                    name = name,
                    category = category,
                    purchasePrice = purchasePrice.toLongOrNull() ?: 0,
                    sellingPrice = sellingPrice.toLongOrNull() ?: 0,
                    barcode = barcode
                )
                onUpdate(updatedProduct) 
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = name.isNotBlank() && purchasePrice.isNotBlank() && sellingPrice.isNotBlank()
        ) {
            Text("Simpan Perubahan")
        }
        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Preview(showBackground = true)
@Composable
fun EditProductSheetPreview() {
    ManagementProductTheme {
        EditProductSheet(
            product = ProductEntity(id = 1, name = "Kopi Kapal Api", category = "Minuman", purchasePrice = 1200, sellingPrice = 1500, stock = 20),
            onUpdate = {},
            onDelete = {},
            onCancel = {}
        )
    }
}
