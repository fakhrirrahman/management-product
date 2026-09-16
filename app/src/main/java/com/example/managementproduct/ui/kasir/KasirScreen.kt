package com.example.managementproduct.ui.kasir

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items as lazyRowItems
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import kotlinx.coroutines.launch
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.CameraAlt
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
import com.example.managementproduct.data.local.entity.ProductEntity
import com.example.managementproduct.util.formatRupiah

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KasirScreen(
    viewModel: KasirViewModel
) {
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
    val products by viewModel.products.collectAsStateWithLifecycle()
    val cartItems by viewModel.cartItems.collectAsStateWithLifecycle()
    
    val categories = listOf("Semua", "Makanan", "Minuman", "Sembako", "Rokok")
    
    var showBottomSheet by remember { mutableStateOf(false) }
    
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val context = androidx.compose.ui.platform.LocalContext.current
    
    val scanLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = com.journeyapps.barcodescanner.ScanContract(),
        onResult = { result ->
            if (result.contents != null) {
                viewModel.scanToCart(result.contents) {
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar("Barang tidak ditemukan di Inventaris")
                    }
                }
            }
        }
    )
    
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.primary)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "KiosKu",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Transaksi penjualan",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 14.sp
                )
            }
        },
        bottomBar = {
            if (cartItems.isNotEmpty()) {
                val totalPrice = cartItems.sumOf { it.product.sellingPrice * it.quantity }
                val totalItems = cartItems.sumOf { it.quantity }
                
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 8.dp,
                    shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = "$totalItems Barang", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                            Text(text = totalPrice.formatRupiah(), fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MaterialTheme.colorScheme.primary)
                        }
                        Button(
                            onClick = { showBottomSheet = true },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Text("Bayar")
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Search Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.onSearchQueryChanged(it) },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Cari barang...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    )
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
                    modifier = Modifier
                        .size(56.dp)
                        .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(12.dp))
                ) {
                    Icon(Icons.Default.CameraAlt, contentDescription = "Scan Barcode", tint = MaterialTheme.colorScheme.onPrimaryContainer)
                }
            }

            // Categories
            
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                lazyRowItems(categories) { category ->
                    val isSelected = category == selectedCategory
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.onCategorySelected(category) },
                        label = { Text(category) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Product Grid
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(products) { product ->
                    val cartItem = cartItems.find { it.product.id == product.id }
                    val qtyInCart = cartItem?.quantity ?: 0
                    
                    ProductCard(
                        product = product,
                        qtyInCart = qtyInCart,
                        onAddClick = { viewModel.addToCart(product) },
                        onRemoveClick = { viewModel.removeFromCart(product) }
                    )
                }
            }
        }
    }

    if (showBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = { showBottomSheet = false },
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            CheckoutSheet(
                cartItems = cartItems,
                onConfirm = {
                    viewModel.checkout(
                        onSuccess = { showBottomSheet = false },
                        onError = { /* Show error toast */ }
                    )
                },
                onCancel = { showBottomSheet = false }
            )
        }
    }
}

@Composable
fun ProductCard(
    product: ProductEntity,
    qtyInCart: Int,
    onAddClick: () -> Unit,
    onRemoveClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            // Avatar Placeholder
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
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = product.name,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                maxLines = 2
            )
            Text(
                text = product.sellingPrice.formatRupiah(),
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
            Text(
                text = "Stok ${product.stock}",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 12.sp
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            if (qtyInCart == 0) {
                Button(
                    onClick = onAddClick,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer, contentColor = MaterialTheme.colorScheme.onPrimaryContainer),
                    enabled = product.stock > 0
                ) {
                    Text("+ Tambah")
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onRemoveClick, modifier = Modifier
                        .size(32.dp)
                        .background(MaterialTheme.colorScheme.primaryContainer, CircleShape)) {
                        Icon(Icons.Default.Remove, contentDescription = "-", tint = MaterialTheme.colorScheme.onPrimaryContainer)
                    }
                    Text(text = "$qtyInCart", fontWeight = FontWeight.Bold)
                    IconButton(
                        onClick = onAddClick,
                        enabled = qtyInCart < product.stock,
                        modifier = Modifier
                            .size(32.dp)
                            .background(if (qtyInCart < product.stock) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline, CircleShape)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "+", tint = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
fun CheckoutSheet(
    cartItems: List<com.example.managementproduct.model.CartItem>,
    onConfirm: () -> Unit,
    onCancel: () -> Unit
) {
    val totalPrice = cartItems.sumOf { it.product.sellingPrice * it.quantity }
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text("Rincian Pembayaran", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))
        
        // Let's just list the items
        cartItems.forEach { item ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("${item.product.name} x${item.quantity}")
                Text((item.product.sellingPrice * item.quantity).formatRupiah(), fontWeight = FontWeight.Bold)
            }
        }
        
        Divider(modifier = Modifier.padding(vertical = 12.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Total", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Text(totalPrice.formatRupiah(), fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Button(
            onClick = onConfirm,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Text("Konfirmasi Pembayaran")
        }
        Spacer(modifier = Modifier.height(32.dp)) // Padding for bottom nav overlap avoidance in some devices
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
fun ProductCardPreview() {
    com.example.managementproduct.ui.theme.ManagementProductTheme {
        ProductCard(
            product = ProductEntity(id = 1, name = "Indomie Goreng", category = "Makanan", purchasePrice = 2500, sellingPrice = 3000, stock = 10),
            qtyInCart = 0,
            onAddClick = {},
            onRemoveClick = {}
        )
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
fun CheckoutSheetPreview() {
    com.example.managementproduct.ui.theme.ManagementProductTheme {
        CheckoutSheet(
            cartItems = listOf(
                com.example.managementproduct.model.CartItem(
                    product = ProductEntity(id = 1, name = "Indomie Goreng", category = "Makanan", purchasePrice = 2500, sellingPrice = 3000, stock = 10),
                    quantity = 2
                )
            ),
            onConfirm = {},
            onCancel = {}
        )
    }
}
