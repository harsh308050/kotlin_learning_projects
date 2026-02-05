package com.harsh.shopit.main.screens.shop.ui.adapter

import com.harsh.shopit.main.screens.shop.data.model.Product

interface OnProductActionListener {
    fun onFavClick(product: Product)
}