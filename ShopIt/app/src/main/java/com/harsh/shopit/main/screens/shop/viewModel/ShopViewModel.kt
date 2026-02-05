package com.harsh.shopit.main.screens.shop.viewModel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.harsh.shopit.main.screens.shop.data.repo.ProductsRepo
import com.harsh.shopit.main.screens.shop.data.model.Product
import com.harsh.shopit.main.screens.shop.data.model.toWishlistEntity
import com.harsh.shopit.main.screens.shop.data.repo.WishlistedRepo
import com.harsh.shopit.main.utils.Resource
import kotlinx.coroutines.launch

class ShopViewModel(application: Application) : AndroidViewModel(application) {
    private val productsRepo = ProductsRepo()

    private val wishlistedRepo = WishlistedRepo(application)
    val wishlistedIds = wishlistedRepo.getWishlistIds().asLiveData()
    private val products = MutableLiveData<Resource<List<Product>>>()
    val _products: LiveData<Resource<List<Product>>> = products
    fun toggleWishlist(product: Product) {
        viewModelScope.launch {
            val isWishlisted = wishlistedRepo.isWishlisted(product.id)
            if (isWishlisted) {
                wishlistedRepo.removeFromWishlist(product.id)
            } else {
                wishlistedRepo.addToWishlist(product.toWishlistEntity())
            }
        }
    }

    fun fetchProducts() {
        viewModelScope.launch {
            products.value = Resource.Loading()

            try {
                val response = productsRepo.getproduct()
                    if (response.isSuccessful) {
                    products.value = Resource.Success(response.body()?.products ?: emptyList())
                    Log.d("Products", response.body()?.products.toString())
                } else {
                    products.value = Resource.Error("error")
                }
            } catch (e: Exception) {
                products.value = Resource.Error(e.message ?: "error")
            }
        }
    }
}