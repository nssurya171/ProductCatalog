package com.surya.productcatalog.ui.productdetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.surya.productcatalog.ProductCatalogApplication
import com.surya.productcatalog.data.AppResult
import com.surya.productcatalog.data.CartRepository
import com.surya.productcatalog.data.ProductRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ProductDetailViewModel(
    private val productId: Int,
    private val productRepository: ProductRepository,
    private val cartRepository: CartRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow<ProductDetailUiState>(ProductDetailUiState.Loading)
    val uiState: StateFlow<ProductDetailUiState> = _uiState.asStateFlow()

    init {
        loadProduct()
    }

    fun loadProduct() {
        _uiState.value = ProductDetailUiState.Loading
        viewModelScope.launch {
            _uiState.value = when (val result = productRepository.getProduct(productId)) {
                is AppResult.Success -> ProductDetailUiState.Success(result.data)
                is AppResult.Error -> ProductDetailUiState.Error(result.message)
            }
        }
    }

    /** Adds the product, or raises its quantity if it's already in the cart. */
    fun addToCart() {
        val state = _uiState.value as? ProductDetailUiState.Success ?: return
        viewModelScope.launch {
            cartRepository.addToCart(state.product)
        }
    }

    companion object {
        /** Builds the ViewModel for one product, using our AppContainer. */
        fun factory(productId: Int): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val container = (this[APPLICATION_KEY] as ProductCatalogApplication).container
                ProductDetailViewModel(
                    productId = productId,
                    productRepository = container.productRepository,
                    cartRepository = container.cartRepository,
                )
            }
        }
    }
}
