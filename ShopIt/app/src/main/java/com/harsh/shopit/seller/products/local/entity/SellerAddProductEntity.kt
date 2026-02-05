package com.harsh.shopit.seller.products.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "products")
data class SellerAddProductEntity(
    @PrimaryKey(autoGenerate = true)
    val prodId: Int = 0,
    val prodName: String,
    val prodPrice: String,
    val prodQuantity: String,
    val prodDescription: String,
    val prodCategory: String,
    val prodStock: Boolean,
    val prodImage: String
)
