package com.harsh.shopit.main.screens.home.saleprod

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.harsh.shopit.R

class SaleProductsAdapter(
    private val saleProductsList: ArrayList<SaleProductsDataModel>
) : RecyclerView.Adapter<SaleProductsAdapter.SaleProductsViewHolder>() {

    class SaleProductsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val saleProductsImage: ImageView = itemView.findViewById(R.id.sale_prod_image)
        val saleProductsDiscount: TextView = itemView.findViewById(R.id.sale_prod_discount)
        val saleProductsName: TextView = itemView.findViewById(R.id.sale_prod_name)
        val saleProductsPrice: TextView = itemView.findViewById(R.id.sale_prod_price)
        val saleProductsOriginalPrice: TextView = itemView.findViewById(R.id.sale_prod_original_price)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SaleProductsViewHolder {
        val itemView =
            LayoutInflater.from(parent.context).inflate(R.layout.customer_item_sale_products, parent, false)
        return SaleProductsViewHolder(itemView)
    }

    override fun onBindViewHolder(
        holder: SaleProductsViewHolder,
        position: Int
    ) {
        val currItem = saleProductsList[position]

        holder.saleProductsImage.setImageResource(currItem.image)
        holder.saleProductsDiscount.text = currItem.discount
        holder.saleProductsName.text = currItem.productName
        holder.saleProductsPrice.text = currItem.price
        holder.saleProductsOriginalPrice.text = currItem.originalPrice
    }

    override fun getItemCount(): Int {
        return saleProductsList.size
    }


}