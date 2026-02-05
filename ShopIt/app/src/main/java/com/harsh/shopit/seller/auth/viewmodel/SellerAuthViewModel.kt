package com.harsh.shopit.seller.auth.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.harsh.shopit.main.utils.Resource
import com.harsh.shopit.seller.auth.local.entity.SellerAuthEntity
import com.harsh.shopit.seller.auth.repo.SellerAuthRepo
import kotlinx.coroutines.launch

class SellerAuthViewModel(application: Application) : AndroidViewModel(application) {
    private val sellerAuthRepo = SellerAuthRepo(application)
    private val sellers = MutableLiveData<Resource<List<SellerAuthEntity>>>()
    val _sellers: LiveData<Resource<List<SellerAuthEntity>>> = sellers
    fun addSeller(name: String, email: String, password: String) {
        viewModelScope.launch {
            sellers.value = Resource.Loading()

            try {
                sellerAuthRepo.addSeller(name, email, password)
                sellers.value = Resource.Success(emptyList())
            } catch (e: Exception) {
                sellers.value = Resource.Error(e.message ?: "error")
            }
        }
    }
    fun getSeller(email: String,password: String){
        viewModelScope.launch {
            sellers.value = Resource.Loading()
            try {
                sellerAuthRepo.getSeller(email,password)
                sellers.value = Resource.Success(emptyList())
            }catch (e: Exception){
                sellers.value = Resource.Error(e.message ?: "error")
            }
        }
    }
}