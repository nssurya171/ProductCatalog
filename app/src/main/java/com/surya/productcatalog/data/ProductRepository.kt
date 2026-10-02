package com.surya.productcatalog.data

import com.surya.productcatalog.data.model.Product
import com.surya.productcatalog.data.model.toProduct
import com.surya.productcatalog.data.remote.ProductApi

/** Single place the app asks for product data. Hides Retrofit and DTOs from the UI. */
class ProductRepository(private val api: ProductApi) {

    suspend fun getProducts(): AppResult<List<Product>> = safeApiCall {
        api.getProducts().products.orEmpty().map { it.toProduct() }
    }

    suspend fun searchProducts(query: String): AppResult<List<Product>> = safeApiCall {
        api.searchProducts(query).products.orEmpty().map { it.toProduct() }
    }

    suspend fun getProduct(id: Int): AppResult<Product> = safeApiCall {
        api.getProduct(id).toProduct()
    }
}
