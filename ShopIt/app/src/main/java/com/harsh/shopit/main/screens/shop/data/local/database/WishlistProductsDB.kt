package com.harsh.shopit.main.screens.shop.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

import com.harsh.shopit.main.screens.shop.data.local.dao.WishlistProductsDAO
import com.harsh.shopit.main.screens.shop.data.local.entity.WishlistedProductsEntity

@Database(
    entities = [WishlistedProductsEntity::class],
    version = 1,
    exportSchema = false
)
abstract class WishlistProductsDB: RoomDatabase() {
    abstract fun wishlistProductsDao(): WishlistProductsDAO
    companion object{
        private var Instance: WishlistProductsDB? = null
        fun getDb(context: Context): WishlistProductsDB{
            return Instance?: synchronized(this){
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    WishlistProductsDB::class.java,
                    "wishlist_products_db").build()
                Instance = instance
                instance
            }
        }

    }
}