package com.harsh.shopit.auth.login.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.harsh.shopit.auth.login.data.model.LoginResponseModel
import com.harsh.shopit.auth.login.data.repo.LoginRepo
import com.harsh.shopit.main.utils.Prefs
import com.harsh.shopit.main.utils.Resource
import com.harsh.shopit.main.utils.SharedPrefKeys
import kotlinx.coroutines.launch

class LoginViewModel : ViewModel() {
    private val loginRepo = LoginRepo()
    private val _loginState = MutableLiveData<Resource<LoginResponseModel>>()
    val loginState: LiveData<Resource<LoginResponseModel>> = _loginState


    fun login(username: String, password: String) {
        viewModelScope.launch {
            _loginState.value = Resource.Loading()
            try {
                val response = loginRepo.login(username, password)
                Log.d("Login", response.body().toString())
                if (response.isSuccessful) {
                    _loginState.value = Resource.Success(response.body()!!)
                } else {
                    _loginState.value = Resource.Error("Invalid Creds")
                }
            } catch (e: Exception) {
                _loginState.value = Resource.Error(e.message ?: "Something Went Wrong")
            }
        }
    }
}
