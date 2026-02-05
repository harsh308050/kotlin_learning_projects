package com.harsh.shopit.main.screens.shop.data.remote
import com.harsh.shopit.main.screens.shop.data.model.ProductsResponseModel
import com.harsh.shopit.main.utils.ApiUtils
import retrofit2.Response
import retrofit2.http.GET

interface ApiService {
    @GET(ApiUtils.productsEndpoint) //endpoint
    suspend fun getProducts(): Response<ProductsResponseModel>

}