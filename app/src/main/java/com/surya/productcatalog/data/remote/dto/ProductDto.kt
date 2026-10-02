package com.surya.productcatalog.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * One product exactly as DummyJSON sends it.
 *
 * Every field except id is nullable on purpose: Gson creates objects without
 * calling the Kotlin constructor, so a missing field becomes null even when the
 * type says it can't be. Declaring them nullable makes the compiler force us to
 * handle that case instead of crashing later.
 */
data class ProductDto(
    @SerializedName("id") val id: Int,
    @SerializedName("title") val title: String?,
    @SerializedName("description") val description: String?,
    @SerializedName("category") val category: String?,
    @SerializedName("price") val price: Double?,
    @SerializedName("rating") val rating: Double?,
    @SerializedName("stock") val stock: Int?,
    @SerializedName("brand") val brand: String?,
    @SerializedName("thumbnail") val thumbnail: String?,
    @SerializedName("images") val images: List<String>?,
)
