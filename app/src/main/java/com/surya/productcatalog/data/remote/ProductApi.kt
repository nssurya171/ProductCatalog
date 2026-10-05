package com.surya.productcatalog.data.remote

import com.surya.productcatalog.data.remote.dto.ProductDto
import com.surya.productcatalog.data.remote.dto.ProductsResponseDto
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/** DummyJSON product endpoints. Retrofit generates the implementation. */
interface ProductApi {

    /** limit = 0 asks DummyJSON for every product. */
    @GET("products")
    suspend fun getProducts(
        @Query("limit") limit: Int = 0,
        @Query("skip") skip: Int = 0,
    ): ProductsResponseDto

    @GET("products/search")
    suspend fun searchProducts(
        @Query("q") query: String,
        @Query("limit") limit: Int = 0,
    ): ProductsResponseDto

    @GET("products/{id}")
    suspend fun getProduct(@Path("id") id: Int): ProductDto
}
