package com.surya.productcatalog.data.remote.dto

import com.google.gson.annotations.SerializedName

/** One entry from /products/categories. */
data class CategoryDto(
    @SerializedName("slug") val slug: String?,
    @SerializedName("name") val name: String?,
    @SerializedName("url") val url: String?,
)
