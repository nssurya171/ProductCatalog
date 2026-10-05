package com.surya.productcatalog.ui.productlist

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.surya.productcatalog.ProductCatalogApplication
import com.surya.productcatalog.data.AppResult
import com.surya.productcatalog.data.CartRepository
import com.surya.productcatalog.data.ProductRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

private const val SEARCH_DEBOUNCE_MS = 400L

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
class ProductListViewModel(
    private val repository: ProductRepository,
    cartRepository: CartRepository,
) : ViewModel() {

    /**
     * The text in the search bar. Kept as Compose state (not a StateFlow) because
     * a TextField must be updated synchronously, otherwise the cursor can jump.
     */
    var query by mutableStateOf("")
        private set

    /** Bumped by Retry so the same query is loaded again. */
    private val retryCount = MutableStateFlow(0)

    val uiState: StateFlow<ProductListUiState> =
        combine(
            snapshotFlow { query }
                .map { it.trim() }
                // Wait until the user stops typing. An empty query (first load or
                // cleared search) shows the full list immediately.
                .debounce { if (it.isEmpty()) 0L else SEARCH_DEBOUNCE_MS }
                .distinctUntilChanged(),
            retryCount,
        ) { searchText, _ -> searchText }
            // flatMapLatest cancels the previous request when a new query arrives,
            // so an old, slow response can never overwrite newer results.
            .flatMapLatest { searchText -> loadProducts(searchText) }
            // Eagerly: keep the result while the user is on another screen,
            // so coming back doesn't trigger a reload.
            .stateIn(viewModelScope, SharingStarted.Eagerly, ProductListUiState.Loading)

    /** Total quantity in the cart, for the badge on the cart icon. Comes from Room. */
    val cartItemCount: StateFlow<Int> = cartRepository.totalCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    fun onQueryChange(newQuery: String) {
        query = newQuery
    }

    fun retry() {
        retryCount.value++
    }

    private fun loadProducts(searchText: String): Flow<ProductListUiState> = flow {
        emit(ProductListUiState.Loading)
        val result = if (searchText.isEmpty()) {
            repository.getProducts()
        } else {
            repository.searchProducts(searchText)
        }
        val state = when (result) {
            is AppResult.Success ->
                if (result.data.isEmpty()) {
                    ProductListUiState.Empty(searchText)
                } else {
                    ProductListUiState.Success(result.data)
                }
            is AppResult.Error -> ProductListUiState.Error(result.message)
        }
        emit(state)
    }

    companion object {
        /** Tells Compose how to build this ViewModel using our AppContainer. */
        val Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as ProductCatalogApplication
                ProductListViewModel(
                    repository = app.container.productRepository,
                    cartRepository = app.container.cartRepository,
                )
            }
        }
    }
}
