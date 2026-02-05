package com.harsh.shopit.seller.auth.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.harsh.shopit.seller.auth.local.entity.SellerAuthEntity

@Dao
interface SellerAuthDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSeller(seller: SellerAuthEntity)

    @Query("SELECT * FROM sellers WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): SellerAuthEntity?

    @Query("SELECT COUNT(*) FROM sellers WHERE email = :email")
    suspend fun isEmailExists(email: String): Int
}