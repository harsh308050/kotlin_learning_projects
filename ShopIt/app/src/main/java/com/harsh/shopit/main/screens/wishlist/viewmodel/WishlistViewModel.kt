package com.harsh.shopit.main.screens.wishlist.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.harsh.shopit.main.screens.shop.data.repo.WishlistedRepo
import kotlinx.coroutines.launch

class WishlistViewModel(application: Application): AndroidViewModel(application) {
    private val repo = WishlistedRepo(application)
    val wishlistedProds = repo.getWishlist().asLiveData()

    fun removeFromWishlist(productId: Int) {
        viewModelScope.launch {
            repo.removeFromWishlist(productId)
        }
    }
}