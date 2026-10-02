package com.surya.productcatalog.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

/**
 * Database operations for the cart.
 * The Flow queries re-emit automatically whenever the cart table changes.
 */
@Dao
abstract class CartDao {

    @Query("SELECT * FROM cart_items ORDER BY added_at ASC")
    abstract fun observeItems(): Flow<List<CartItemEntity>>

    @Query("SELECT COALESCE(SUM(quantity), 0) FROM cart_items")
    abstract fun observeTotalCount(): Flow<Int>

    @Query("SELECT COALESCE(SUM(price * quantity), 0.0) FROM cart_items")
    abstract fun observeTotalPrice(): Flow<Double>

    @Query("SELECT * FROM cart_items WHERE product_id = :productId")
    abstract suspend fun getItem(productId: Int): CartItemEntity?

    @Insert
    abstract suspend fun insert(item: CartItemEntity)

    @Query("UPDATE cart_items SET quantity = quantity + 1 WHERE product_id = :productId")
    abstract suspend fun increaseQuantity(productId: Int)

    @Query("UPDATE cart_items SET quantity = quantity - 1 WHERE product_id = :productId")
    abstract suspend fun decrementQuantity(productId: Int)

    @Query("DELETE FROM cart_items WHERE product_id = :productId")
    abstract suspend fun delete(productId: Int)

    /** Adds a new row, or raises the quantity if the product is already in the cart. */
    @Transaction
    open suspend fun addOrIncrease(item: CartItemEntity) {
        if (getItem(item.productId) == null) {
            insert(item.copy(quantity = 1))
        } else {
            increaseQuantity(item.productId)
        }
    }

    /** Lowers the quantity by one; at quantity 1 the item is removed. */
    @Transaction
    open suspend fun decreaseOrRemove(productId: Int) {
        val item = getItem(productId) ?: return
        if (item.quantity <= 1) {
            delete(productId)
        } else {
            decrementQuantity(productId)
        }
    }
}
