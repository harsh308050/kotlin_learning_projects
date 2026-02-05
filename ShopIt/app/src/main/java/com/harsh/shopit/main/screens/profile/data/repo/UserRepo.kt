package com.harsh.shopit.main.screens.profile.data.repo

import com.harsh.shopit.main.screens.profile.data.remote.UserInstance


class UserRepo {
    suspend fun getUser(token : String) = UserInstance.Companion.getUserApiService().getUser(token)
}