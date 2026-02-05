package com.harsh.shopit.main.screens.shop.data.repo

import com.harsh.shopit.main.screens.shop.data.remote.RetrofitInstance

class ProductsRepo {
    suspend fun getproduct() = RetrofitInstance.Companion.getApiService().getProducts()
}