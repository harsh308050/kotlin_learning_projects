package com.harsh.shopit.main.screens.shop.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "wishlisted_products")
data class WishlistedProductsEntity(
    @PrimaryKey(autoGenerate = false)
    val id: Int,
    val brand: String?,
    val category: String?,
    val description: String?,
    val price: Double,
    val thumbnail: String?,
    val title: String?,
    val weight: Int?

)
