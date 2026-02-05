package com.harsh.shopit.seller.products.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.harsh.shopit.seller.products.local.entity.SellerAddProductEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SellerAddProductDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: SellerAddProductEntity)

    @Query("SELECT * FROM products")
    fun getAllProducts(): Flow<List<SellerAddProductEntity>>

    @Query("DELETE FROM products WHERE prodId = :productId")
    suspend fun deleteProduct(productId: Int)

    @Query("SELECT prodId FROM products")
    fun geProductId(): Flow<List<Int>>

    @Query("SELECT * FROM products ORDER BY prodId DESC LIMIT 4")
    fun getTopFourProducts(): Flow<List<SellerAddProductEntity>>

    @Update
    suspend fun updateProduct(product: SellerAddProductEntity)

    @Query("SELECT * FROM products WHERE prodId = :id LIMIT 1")
    suspend fun getProductById(id: Int): SellerAddProductEntity?
}