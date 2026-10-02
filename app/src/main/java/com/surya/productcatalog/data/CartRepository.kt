package com.surya.productcatalog.data

import com.surya.productcatalog.data.local.CartDao
import com.surya.productcatalog.data.local.CartItemEntity
import com.surya.productcatalog.data.model.Product
import kotlinx.coroutines.flow.Flow

/** Cart operations. Works only with the local database, so it never needs the internet. */
class CartRepository(private val cartDao: CartDao) {

    val items: Flow<List<CartItemEntity>> = cartDao.observeItems()
    val totalCount: Flow<Int> = cartDao.observeTotalCount()
    val totalPrice: Flow<Double> = cartDao.observeTotalPrice()

    suspend fun addToCart(product: Product) {
        cartDao.addOrIncrease(
            CartItemEntity(
                productId = product.id,
                title = product.title,
                price = product.price,
                thumbnail = product.thumbnail,
                quantity = 1,
            )
        )
    }

    suspend fun increaseQuantity(productId: Int) = cartDao.increaseQuantity(productId)

    suspend fun decreaseQuantity(productId: Int) = cartDao.decreaseOrRemove(productId)

    suspend fun remove(productId: Int) = cartDao.delete(productId)
}
