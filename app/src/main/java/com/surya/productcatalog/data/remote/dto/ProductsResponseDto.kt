package com.surya.productcatalog.data.remote.dto

import com.google.gson.annotations.SerializedName

/** Wrapper returned by /products and /products/search. */
data class ProductsResponseDto(
    @SerializedName("products") val products: List<ProductDto>?,
    @SerializedName("total") val total: Int?,
    @SerializedName("skip") val skip: Int?,
    @SerializedName("limit") val limit: Int?,
)
