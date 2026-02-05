package com.harsh.shopit.seller.products.showProds.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import com.harsh.shopit.main.utils.Resource
import com.harsh.shopit.seller.products.local.entity.SellerAddProductEntity
import com.harsh.shopit.seller.products.repo.SellerProductRepo
import kotlinx.coroutines.launch

class ProductViewModel (application: Application): AndroidViewModel(application) {
    private val repo = SellerProductRepo(application)
    val allProds = repo.getProducts().asLiveData()

    fun removeProduct(productId: Int) {
        viewModelScope.launch {
            repo.removeProd(productId)
        }
    }


}