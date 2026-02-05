package com.harsh.shopit.seller.products.addProd.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import com.harsh.shopit.main.utils.Resource
import com.harsh.shopit.seller.products.local.entity.SellerAddProductEntity
import com.harsh.shopit.seller.products.repo.SellerProductRepo
import kotlinx.coroutines.launch

class SellerAddProductViewmodel(application: Application): AndroidViewModel(application) {
    private val sellerAddProdRepo = SellerProductRepo(application)
    private val products = MutableLiveData<Resource<List<SellerAddProductEntity>>>()
    val _products: LiveData<Resource<List<SellerAddProductEntity>>> = products
    fun addProduct(product: SellerAddProductEntity) {
        viewModelScope.launch {
            products.value= Resource.Loading()
            try{
                sellerAddProdRepo.addProduct(product)
                products.value = Resource.Success(listOf(product))
            }catch (e: Exception){
                products.value = Resource.Error(e.message ?: "error")
            }
        }
    }

    fun updateProduct(product: SellerAddProductEntity) {
        viewModelScope.launch {
            products.value = Resource.Loading()
            try {
                sellerAddProdRepo.updateProduct(product)
                products.value = Resource.Success(listOf(product))
            } catch (e: Exception) {
                products.value = Resource.Error(e.message ?: "error")
            }
        }
    }
    fun getProductById(id: Int) = liveData {
        emit(sellerAddProdRepo.getProductById(id))
    }
}