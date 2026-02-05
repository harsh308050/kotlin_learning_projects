package com.harsh.shopit.auth.login.data.remote

import com.harsh.shopit.auth.login.data.model.LoginRequestModel
import com.harsh.shopit.auth.login.data.model.LoginResponseModel
import com.harsh.shopit.main.utils.ApiUtils
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface LoginApiService {
    @POST(ApiUtils.loginEndpoint)
    suspend fun login(@Body request: LoginRequestModel): Response<LoginResponseModel>
}