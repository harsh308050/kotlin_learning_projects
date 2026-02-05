package com.harsh.shopit.main.screens.shop.data.repo

import android.content.Context
import com.harsh.shopit.main.screens.shop.data.local.database.WishlistProductsDB
import com.harsh.shopit.main.screens.shop.data.local.entity.WishlistedProductsEntity
import kotlinx.coroutines.flow.Flow

class WishlistedRepo(context: Context) {
    private val wishlistProductsDAO = WishlistProductsDB.getDb(context).wishlistProductsDao()
    suspend fun addToWishlist(product: WishlistedProductsEntity) {
        wishlistProductsDAO.insertProduct(product)
    }
    suspend fun removeFromWishlist(productId: Int) {
        wishlistProductsDAO.deleteProduct(productId)
    }
    fun getWishlist(): Flow<List<WishlistedProductsEntity>> {
        return wishlistProductsDAO.getAllProducts()
    }
    suspend fun isWishlisted(productId: Int): Boolean {
        return wishlistProductsDAO.isWishlisted(productId)
    }

    fun getWishlistIds(): Flow<List<Int>> {
        return wishlistProductsDAO.getWishlistIds()
    }
}