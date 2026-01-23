package com.harsh.shopit.main.screens.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.harsh.shopit.R

class TrendProductsAdapter(
    private val trendProductsList: ArrayList<TrendProductsDataModel>
) : RecyclerView.Adapter<TrendProductsAdapter.trendProductsViewHolder>() {

    class trendProductsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val trendProductsImage: ImageView = itemView.findViewById(R.id.trendProdImage)
        val trendProductsBgCard: MaterialCardView = itemView.findViewById(R.id.trendProductsBgCard)
        val trendProductsName: TextView = itemView.findViewById(R.id.trendProdName)
        val trendProductsPrice: TextView = itemView.findViewById(R.id.tredProdPrice)
        val trendProductsSubtitle: TextView = itemView.findViewById(R.id.trendProdSubtitle)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): trendProductsViewHolder {
        val itemView =
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_trending_product, parent, false)
        return trendProductsViewHolder(itemView)
    }

    override fun onBindViewHolder(
        holder: trendProductsViewHolder,
        position: Int
    ) {
        val currItem = trendProductsList[position]

        holder.trendProductsImage.setImageResource(currItem.trendProductImage)
        holder.trendProductsName.text = currItem.trendProductName
        holder.trendProductsBgCard.backgroundTintList =
            android.content.res.ColorStateList.valueOf(currItem.trendProductBgCardColor)
        holder.trendProductsSubtitle.text = currItem.trendProductSubtitle
        holder.trendProductsPrice.text = currItem.trendProductPrice

    }

    override fun getItemCount(): Int {
        return trendProductsList.size
    }


}