package com.surya.productcatalog

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.surya.productcatalog.ui.cart.CartScreen
import com.surya.productcatalog.ui.productdetail.ProductDetailScreen
import com.surya.productcatalog.ui.productlist.ProductListScreen
import com.surya.productcatalog.ui.theme.ProductCatalogTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ProductCatalogTheme {
                // Temporary screen switching: Phase 7 replaces this with Navigation Compose.
                var selectedProductId by rememberSaveable { mutableStateOf<Int?>(null) }
                var showCart by rememberSaveable { mutableStateOf(false) }
                val productId = selectedProductId
                when {
                    showCart -> {
                        BackHandler { showCart = false }
                        CartScreen(onBack = { showCart = false })
                    }
                    productId != null -> {
                        BackHandler { selectedProductId = null }
                        ProductDetailScreen(
                            productId = productId,
                            onBack = { selectedProductId = null },
                        )
                    }
                    else -> ProductListScreen(
                        onProductClick = { selectedProductId = it },
                        onCartClick = { showCart = true },
                    )
                }
            }
        }
    }
}
