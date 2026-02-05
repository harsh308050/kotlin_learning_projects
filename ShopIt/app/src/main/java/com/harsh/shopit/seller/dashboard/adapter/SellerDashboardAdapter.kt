package com.harsh.shopit.seller.dashboard.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.harsh.shopit.R
import com.harsh.shopit.seller.products.local.entity.SellerAddProductEntity

class SellerDashboardAdapter:RecyclerView.Adapter<SellerDashboardAdapter.DashboardViewHolder>() {

    private val items = mutableListOf<SellerAddProductEntity>()

    fun submitList(newList: List<SellerAddProductEntity>) {
        items.clear()
        items.addAll(newList)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DashboardViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.seller_item_prod_dashboard, parent, false)
        return DashboardViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: DashboardViewHolder,
        position: Int
    ) {
        val product = items[position]

        holder.name.text = product.prodName
        holder.category.text = product.prodCategory
        holder.price.text = "$${product.prodPrice}"

        Glide.with(holder.itemView.context)
            .load(product.prodImage)
            .into(holder.image)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    class  DashboardViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val image: ImageView = itemView.findViewById(R.id.sellerProdImage)
        val name: TextView = itemView.findViewById(R.id.sellerProdName)
        val category: TextView = itemView.findViewById(R.id.sellerProdCategory)
        val price: TextView = itemView.findViewById(R.id.sellerProdPrice)
    }
}