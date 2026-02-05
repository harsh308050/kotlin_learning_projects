package com.harsh.shopit.seller.products.repo

import android.content.Context
import com.harsh.shopit.seller.products.local.entity.SellerAddProductEntity
import com.harsh.shopit.seller.database.SellerDatabase
import kotlinx.coroutines.flow.Flow

class SellerProductRepo(context: Context) {
    private val sellerAddProdDao = SellerDatabase.getSellerDb(context).sellerAddProdDao()
    suspend fun addProduct(product: SellerAddProductEntity){
        sellerAddProdDao.insertProduct(product)
    }

    fun getProducts(): Flow<List<SellerAddProductEntity>> {
        return sellerAddProdDao.getAllProducts()
    }
    suspend fun removeProd(productId: Int) {
        sellerAddProdDao.deleteProduct(productId)
    }
    fun getProductsIds(): Flow<List<Int>> {
        return sellerAddProdDao.geProductId()
    }
    fun getTopFourProducts(): Flow<List<SellerAddProductEntity>> {
        return sellerAddProdDao.getTopFourProducts()
    }

    suspend fun updateProduct(product: SellerAddProductEntity) {
        sellerAddProdDao.updateProduct(product)
    }

    suspend fun getProductById(id: Int) =
        sellerAddProdDao.getProductById(id)
}