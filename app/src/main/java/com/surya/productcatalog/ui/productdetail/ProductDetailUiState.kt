package com.surya.productcatalog.ui.productdetail

import com.surya.productcatalog.data.model.Product

/** Everything the details screen can show. */
sealed interface ProductDetailUiState {
    data object Loading : ProductDetailUiState
    data class Success(val product: Product) : ProductDetailUiState
    data class Error(val message: String) : ProductDetailUiState
}
