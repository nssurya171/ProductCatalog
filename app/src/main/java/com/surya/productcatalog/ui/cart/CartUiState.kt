package com.surya.productcatalog.ui.cart

import com.surya.productcatalog.data.local.CartItemEntity

/** Everything the cart screen can show. */
sealed interface CartUiState {
    data object Loading : CartUiState
    data object Empty : CartUiState
    data class Content(
        val items: List<CartItemEntity>,
        val totalCount: Int,
        val totalPrice: Double,
    ) : CartUiState
}
