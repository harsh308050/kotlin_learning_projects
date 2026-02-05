package com.harsh.shopit.main.screens.profile.data.remote

import com.harsh.shopit.main.screens.profile.data.model.UserResponseModel
import com.harsh.shopit.main.utils.ApiUtils
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header

interface UserApiService {
    @GET(ApiUtils.currUser) //endpoint
    suspend fun getUser(@Header("Authorization") token: String): Response<UserResponseModel>
}