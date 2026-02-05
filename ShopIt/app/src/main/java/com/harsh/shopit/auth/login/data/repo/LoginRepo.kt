package com.harsh.shopit.auth.login.data.repo

import com.harsh.shopit.auth.login.data.model.LoginRequestModel
import com.harsh.shopit.auth.login.data.remote.LoginApiInstance


class LoginRepo {
    suspend fun login(username: String, password: String) =
        LoginApiInstance.loginApiService().login(
            LoginRequestModel(username, password)
        )

}
