package com.surya.productcatalog.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * One row in the cart table.
 *
 * We keep a snapshot of the product (title, price, thumbnail) so the cart can
 * be shown with no internet. productId is the primary key, so the same product
 * can never appear twice - adding it again just raises the quantity.
 */
@Entity(tableName = "cart_items")
data class CartItemEntity(
    @PrimaryKey
    @ColumnInfo(name = "product_id") val productId: Int,
    @ColumnInfo(name = "title") val title: String,
    @ColumnInfo(name = "price") val price: Double,
    @ColumnInfo(name = "thumbnail") val thumbnail: String,
    @ColumnInfo(name = "quantity") val quantity: Int,
    // Used only to keep items in the order they were added.
    @ColumnInfo(name = "added_at") val addedAt: Long = System.currentTimeMillis(),
)
