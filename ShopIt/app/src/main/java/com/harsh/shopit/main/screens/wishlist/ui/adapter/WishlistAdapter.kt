package com.harsh.shopit.main.screens.wishlist.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.harsh.shopit.R
import com.harsh.shopit.main.screens.shop.data.local.entity.WishlistedProductsEntity

class WishlistAdapter(private val listener: OnWishlistActionListener): RecyclerView.Adapter<WishlistAdapter.WishlistViewHolder>() {
    private val items = mutableListOf<WishlistedProductsEntity>()
    fun submitList(newList: List<WishlistedProductsEntity>){
        items.clear()
        items.addAll(newList)
        notifyDataSetChanged()
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WishlistViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.customer_item_fav_product, parent, false)
        return WishlistViewHolder(view)
    }
    override fun onBindViewHolder(holder: WishlistViewHolder, position: Int) {
        val product = items[position]

        holder.title.text = product.title
        holder.price.text = "$${product.price}"
        holder.category.text = product.category

        Glide.with(holder.itemView.context)
            .load(product.thumbnail)
            .into(holder.image)
        holder.removeBtn.setOnClickListener {
            listener.onRemoveClick(product)
        }
    }
    override fun getItemCount(): Int = items.size

    class WishlistViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val image: ImageView = itemView.findViewById(R.id.favProdImage)
        val title: TextView = itemView.findViewById(R.id.favProdName)
        val price: TextView = itemView.findViewById(R.id.favProdPrice)
        val category: TextView = itemView.findViewById(R.id.favProdCategory)
        val removeBtn: ImageButton = itemView.findViewById(R.id.addedToFav)
    }
}