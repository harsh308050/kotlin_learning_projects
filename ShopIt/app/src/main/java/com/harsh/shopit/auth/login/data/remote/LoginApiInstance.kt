package com.harsh.shopit.auth.login.data.remote
import com.harsh.shopit.main.utils.ApiUtils
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


class LoginApiInstance {
    companion object {
        val mainUrl = ApiUtils.mainUrl

        fun loginApiService(): LoginApiService {
            return Retrofit.Builder()
                .baseUrl(mainUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(LoginApiService::class.java)
        }
    }
}
