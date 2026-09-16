package com.example.managementproduct.ui.inventaris

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.managementproduct.ManagementProductApplication
import com.example.managementproduct.data.local.entity.ProductEntity
import com.example.managementproduct.data.repository.KiosRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class InventarisViewModel(
    private val repository: KiosRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val allProducts = repository.getAllProducts()

    val products = combine(allProducts, _searchQuery) { products, query ->
        if (query.isBlank()) {
            products
        } else {
            products.filter { it.name.contains(query, ignoreCase = true) }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList()
    )

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun addProduct(name: String, category: String, purchasePrice: Long, sellingPrice: Long, initialStock: Int, onSuccess: () -> Unit) {
        viewModelScope.launch {
            repository.insertProduct(
                ProductEntity(
                    name = name,
                    category = category,
                    purchasePrice = purchasePrice,
                    sellingPrice = sellingPrice,
                    stock = initialStock
                )
            )
            onSuccess()
        }
    }

    fun restock(productId: Int, quantity: Int, purchasePricePerItem: Long, onSuccess: () -> Unit, onError: (String) -> Unit) {
        if (quantity <= 0) {
            onError("Jumlah restok harus lebih dari 0")
            return
        }
        viewModelScope.launch {
            try {
                repository.restock(productId, quantity, purchasePricePerItem)
                onSuccess()
            } catch (e: Exception) {
                onError(e.message ?: "Gagal melakukan restok")
            }
        }
    }
    fun updateProduct(product: ProductEntity, onSuccess: () -> Unit) {
        viewModelScope.launch {
            repository.updateProduct(product)
            onSuccess()
        }
    }

    fun deleteProduct(product: ProductEntity, onSuccess: () -> Unit) {
        viewModelScope.launch {
            repository.deleteProduct(product)
            onSuccess()
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as ManagementProductApplication)
                InventarisViewModel(application.container.kiosRepository)
            }
        }
    }
}
