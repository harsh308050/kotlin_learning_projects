package com.harsh.shopit.seller.auth.local.entity
import androidx.room.Entity
import androidx.room.PrimaryKey
@Entity(tableName = "sellers")
data class SellerAuthEntity(
    @PrimaryKey
    val email: String,
    val password: String,
    val name: String,
)
