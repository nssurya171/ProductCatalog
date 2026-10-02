package com.surya.productcatalog.data.model

import com.surya.productcatalog.data.remote.dto.ProductDto

/**
 * Clean product model used by the rest of the app.
 * Missing API values are replaced with safe defaults here, so screens never
 * deal with nulls - except brand, which is genuinely optional.
 */
data class Product(
    val id: Int,
    val title: String,
    val description: String,
    val category: String,
    val price: Double,
    val rating: Double,
    val stock: Int,
    val brand: String?,
    val thumbnail: String,
    val images: List<String>,
)

fun ProductDto.toProduct(): Product = Product(
    id = id,
    title = title.orEmpty(),
    description = description.orEmpty(),
    category = category.orEmpty(),
    price = price ?: 0.0,
    rating = rating ?: 0.0,
    stock = stock ?: 0,
    brand = brand?.takeIf { it.isNotBlank() },
    thumbnail = thumbnail.orEmpty(),
    images = images.orEmpty(),
)
