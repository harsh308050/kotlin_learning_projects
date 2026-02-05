package com.harsh.shopit.main.screens.profile.data.remote

import com.harsh.shopit.main.utils.ApiUtils
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class UserInstance {

        companion object {
            val mainUrl = ApiUtils.mainUrl

            fun getUserApiService(): UserApiService {
                return Retrofit.Builder()
                    .baseUrl(mainUrl)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
                    .create(UserApiService::class.java)
            }
        }
}