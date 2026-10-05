package com.surya.productcatalog.ui.cart

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.surya.productcatalog.ProductCatalogApplication
import com.surya.productcatalog.data.CartRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CartViewModel(private val cartRepository: CartRepository) : ViewModel() {

    /**
     * Built from three Room Flows. Any change to the cart table makes Room
     * re-emit, so the list and totals update on their own after every tap.
     */
    val uiState: StateFlow<CartUiState> =
        combine(
            cartRepository.items,
            cartRepository.totalCount,
            cartRepository.totalPrice,
        ) { items, totalCount, totalPrice ->
            if (items.isEmpty()) {
                CartUiState.Empty
            } else {
                CartUiState.Content(items, totalCount, totalPrice)
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), CartUiState.Loading)

    fun increaseQuantity(productId: Int) {
        viewModelScope.launch { cartRepository.increaseQuantity(productId) }
    }

    /** At quantity 1 this removes the item. */
    fun decreaseQuantity(productId: Int) {
        viewModelScope.launch { cartRepository.decreaseQuantity(productId) }
    }

    fun remove(productId: Int) {
        viewModelScope.launch { cartRepository.remove(productId) }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as ProductCatalogApplication
                CartViewModel(app.container.cartRepository)
            }
        }
    }
}
