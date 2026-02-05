package com.harsh.shopit.main.screens.shop.data.local.dao
import androidx.room.*
import com.harsh.shopit.main.screens.shop.data.local.entity.WishlistedProductsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WishlistProductsDAO {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: WishlistedProductsEntity)
    @Query("SELECT * FROM wishlisted_products")
    fun getAllProducts(): Flow<List<WishlistedProductsEntity>>
    @Query("DELETE FROM wishlisted_products WHERE id = :productId")
   suspend fun deleteProduct(productId: Int)
    @Query("SELECT EXISTS(SELECT 1 FROM wishlisted_products WHERE id = :productId)")
    suspend fun isWishlisted(productId: Int): Boolean

    @Query("SELECT id FROM wishlisted_products")
    fun getWishlistIds(): Flow<List<Int>>
}
