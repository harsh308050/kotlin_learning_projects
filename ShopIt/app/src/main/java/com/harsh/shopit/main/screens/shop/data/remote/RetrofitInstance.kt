package com.harsh.shopit.main.screens.shop.data.remote

import com.harsh.shopit.main.utils.ApiUtils
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class RetrofitInstance {
    companion object {
        val mainUrl = ApiUtils.mainUrl

        fun getApiService(): ApiService {
            return Retrofit.Builder()
                .baseUrl(mainUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(ApiService::class.java)
        }
    }
}
