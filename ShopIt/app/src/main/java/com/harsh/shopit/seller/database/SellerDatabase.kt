package com.harsh.shopit.seller.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.harsh.shopit.seller.products.local.dao.SellerAddProductDao
import com.harsh.shopit.seller.products.local.entity.SellerAddProductEntity
import com.harsh.shopit.seller.auth.local.dao.SellerAuthDao
import com.harsh.shopit.seller.auth.local.entity.SellerAuthEntity

@Database(
    entities = [SellerAuthEntity::class, SellerAddProductEntity::class],
    version = 1,
    exportSchema = false
)
abstract class SellerDatabase : RoomDatabase() {
    abstract fun sellerAuthDao(): SellerAuthDao
    abstract fun sellerAddProdDao(): SellerAddProductDao

    companion object {
        private var Instance: SellerDatabase? = null
        fun getSellerDb(context: Context): SellerDatabase {
            return Instance ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SellerDatabase::class.java,
                    "seller_db"
                ).build()
                SellerDatabase.Companion.Instance = instance
                instance
            }
        }
    }
}