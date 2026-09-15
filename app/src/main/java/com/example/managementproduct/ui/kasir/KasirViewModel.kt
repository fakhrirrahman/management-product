package com.example.managementproduct.ui.kasir

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.managementproduct.ManagementProductApplication
import com.example.managementproduct.data.local.entity.ProductEntity
import com.example.managementproduct.data.repository.KiosRepository
import com.example.managementproduct.model.CartItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class KasirViewModel(
    private val repository: KiosRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow("Semua")
    val selectedCategory = _selectedCategory.asStateFlow()

    // Map dari productId ke quantity
    private val _cart = MutableStateFlow<Map<Int, Int>>(emptyMap())

    // All products from DB
    private val allProducts = repository.getAllProducts()

    // Filtered products for UI
    val products = combine(allProducts, _searchQuery, _selectedCategory) { products, query, category ->
        products.filter { product ->
            val matchesSearch = product.name.contains(query, ignoreCase = true)
            val matchesCategory = if (category == "Semua") true else product.category == category
            matchesSearch && matchesCategory
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList()
    )

    // Derived cart items for UI (combining cart map with actual product data)
    val cartItems: StateFlow<List<CartItem>> = combine(allProducts, _cart) { products, cartMap ->
        cartMap.mapNotNull { (productId, qty) ->
            val product = products.find { it.id == productId }
            if (product != null) CartItem(product, qty) else null
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList()
    )

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun onCategorySelected(category: String) {
        _selectedCategory.value = category
    }

    fun addToCart(product: ProductEntity) {
        val currentCart = _cart.value.toMutableMap()
        val currentQty = currentCart.getOrDefault(product.id, 0)
        if (currentQty < product.stock) {
            currentCart[product.id] = currentQty + 1
            _cart.value = currentCart
        }
    }

    fun removeFromCart(product: ProductEntity) {
        val currentCart = _cart.value.toMutableMap()
        val currentQty = currentCart.getOrDefault(product.id, 0)
        if (currentQty > 1) {
            currentCart[product.id] = currentQty - 1
        } else {
            currentCart.remove(product.id)
        }
        _cart.value = currentCart
    }
    
    fun clearCart() {
        _cart.value = emptyMap()
    }

    fun checkout(onSuccess: () -> Unit, onError: (String) -> Unit) {
        val items = cartItems.value
        if (items.isEmpty()) {
            onError("Keranjang kosong")
            return
        }

        viewModelScope.launch {
            try {
                repository.checkout(items)
                _cart.value = emptyMap()
                onSuccess()
            } catch (e: Exception) {
                onError(e.message ?: "Terjadi kesalahan")
            }
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as ManagementProductApplication)
                KasirViewModel(application.container.kiosRepository)
            }
        }
    }
}
