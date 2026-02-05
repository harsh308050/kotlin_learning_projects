package com.harsh.shopit.main.screens.profile.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.harsh.shopit.main.screens.profile.data.model.UserResponseModel
import com.harsh.shopit.main.screens.profile.data.repo.UserRepo
import com.harsh.shopit.main.utils.Resource
import kotlinx.coroutines.launch

class ProfileViewModel : ViewModel() {
    private val userRepo = UserRepo()
    private val user = MutableLiveData<Resource<UserResponseModel>>()
    val _user: LiveData<Resource<UserResponseModel>> = user

    fun getUserDetails(token: String){
        viewModelScope.launch {
            user.value = Resource.Loading()
            try{
                val response = userRepo.getUser(token)
                if (response.isSuccessful) {
                    user.value = Resource.Success(response.body()!!)
                } else {
                    user.value = Resource.Error("Failed to load user")
                }
            }
            catch (e: Exception) {
                user.value = Resource.Error(e.message ?: "error")
            }
        }
    }

}