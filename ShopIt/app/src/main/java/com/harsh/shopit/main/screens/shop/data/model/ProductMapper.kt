package com.harsh.shopit.main.screens.shop.data.model

import com.harsh.shopit.main.screens.shop.data.local.entity.WishlistedProductsEntity

fun Product.toWishlistEntity(): WishlistedProductsEntity {
    return WishlistedProductsEntity(
        id = id,
        brand = brand,
        category = category,
        description = description,
        price = price,
        thumbnail = thumbnail,
        title = title,
        weight = weight
    )
}