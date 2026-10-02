package com.surya.productcatalog

import android.content.Context
import com.surya.productcatalog.data.CartRepository
import com.surya.productcatalog.data.ProductRepository
import com.surya.productcatalog.data.local.AppDatabase
import com.surya.productcatalog.data.remote.NetworkModule

/**
 * Manual dependency injection: creates each shared object once and hands it out.
 * `by lazy` means an object is only built the first time someone asks for it.
 */
class AppContainer(context: Context) {

    private val database: AppDatabase by lazy { AppDatabase.create(context) }

    val productRepository: ProductRepository by lazy {
        ProductRepository(NetworkModule.createProductApi())
    }

    val cartRepository: CartRepository by lazy {
        CartRepository(database.cartDao())
    }
}
