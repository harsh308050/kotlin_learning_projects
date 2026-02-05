package com.harsh.shopit.seller.auth.repo

import android.content.Context
import com.harsh.shopit.seller.auth.local.entity.SellerAuthEntity
import com.harsh.shopit.seller.database.SellerDatabase

class SellerAuthRepo(context: Context) {
    private val sellerAuthDao = SellerDatabase.getSellerDb(context).sellerAuthDao()
    suspend fun addSeller(name: String, email: String, password: String){
        return try {
            val exist = sellerAuthDao.isEmailExists(email)
            if(exist>0){
                throw Exception("Email Already Exists")
            }else{
                val seller = SellerAuthEntity(name = name, email = email, password = password,)
                sellerAuthDao.insertSeller(seller)
            }
        }catch (e: Exception){
            throw Exception(e.message)
        }
    }
    suspend fun getSeller(email: String, password: String) {
        val seller =
            sellerAuthDao.getUserByEmail(email) ?: throw Exception("Account Does not Exist")
        if (seller.password != password) {
            throw Exception("Invalid Password")
        }
    }
}
