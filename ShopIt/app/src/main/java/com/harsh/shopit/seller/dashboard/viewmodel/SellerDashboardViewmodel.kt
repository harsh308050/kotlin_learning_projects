package com.harsh.shopit.seller.dashboard.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.asLiveData
import com.harsh.shopit.seller.products.repo.SellerProductRepo
import kotlinx.coroutines.flow.map

class SellerDashboardViewmodel(application: Application): AndroidViewModel(application) {

    private val repo = SellerProductRepo(application)

    val topProducts = repo.getTopFourProducts().asLiveData()
    val totalProducts = repo.getProducts().map { it.size }.asLiveData()
}