package com.harsh.shopit.seller.products.showProds.ui.adapter

import com.harsh.shopit.seller.products.local.entity.SellerAddProductEntity

interface OnDeleteActionListener {
    fun onRemoveClick(product: SellerAddProductEntity)

}

interface OnEditActionListener {
    fun onEditClick(product: SellerAddProductEntity)

}
