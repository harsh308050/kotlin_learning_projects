package com.harsh.shopit.main.screens.wishlist.ui.adapter

import com.harsh.shopit.main.screens.shop.data.local.entity.WishlistedProductsEntity

interface OnWishlistActionListener {
    fun onRemoveClick(product: WishlistedProductsEntity)
}