package com.surya.productcatalog.ui.productlist

import com.surya.productcatalog.data.model.Product

/** Everything the product list can show. Exactly one of these at a time. */
sealed interface ProductListUiState {
    data object Loading : ProductListUiState
    data class Success(val products: List<Product>) : ProductListUiState

    /** [query] is blank when the full list is empty, otherwise the search that found nothing. */
    data class Empty(val query: String) : ProductListUiState
    data class Error(val message: String) : ProductListUiState
}
