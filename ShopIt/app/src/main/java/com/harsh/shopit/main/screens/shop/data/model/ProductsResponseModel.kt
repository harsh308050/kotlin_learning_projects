package com.harsh.shopit.main.screens.shop.data.model

data class ProductsResponseModel(
    val limit: Int,
    val products: List<Product>,
    val skip: Int,
    val total: Int
)