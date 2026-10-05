package com.surya.productcatalog

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.surya.productcatalog.ui.productlist.ProductListScreen
import com.surya.productcatalog.ui.theme.ProductCatalogTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ProductCatalogTheme {
                // Temporary: Phase 7 replaces this with navigation between screens.
                ProductListScreen(onProductClick = {})
            }
        }
    }
}
