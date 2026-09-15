package com.example.managementproduct.ui.product

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.managementproduct.data.local.entity.ProductEntity
import com.example.managementproduct.data.repository.ProductRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ProductViewModel(
    private val repository: ProductRepository
) : ViewModel() {

    val products: StateFlow<List<ProductEntity>> =
        repository.getAllProducts()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = emptyList()
            )

    fun addProduct(
        name: String,
        purchasePrice: Long,
        sellingPrice: Long,
        stock: Int
    ) {
        viewModelScope.launch {
            repository.insertProduct(
                ProductEntity(
                    name = name,
                    purchasePrice = purchasePrice,
                    sellingPrice = sellingPrice,
                    stock = stock
                )
            )
        }
    }

    fun updateProduct(product: ProductEntity) {
        viewModelScope.launch {
            repository.updateProduct(product)
        }
    }

    fun deleteProduct(product: ProductEntity) {
        viewModelScope.launch {
            repository.deleteProduct(product)
        }
    }
}