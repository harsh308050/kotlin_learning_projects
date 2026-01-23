package com.harsh.shopit.main.screens.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.harsh.shopit.R

class BannerItemAdapter(
    private val bannerList: ArrayList<BannerItemDataModel>
) : RecyclerView.Adapter<BannerItemAdapter.BannerViewHolder>() {

    class BannerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val bannerImage: ImageView = itemView.findViewById(R.id.bannerImage)
        val bannerTag: TextView = itemView.findViewById(R.id.bannerTag)
        val bannerTitle: TextView = itemView.findViewById(R.id.bannerTitle)
        val bannerSubtitle: TextView = itemView.findViewById(R.id.bannerSubtitle)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BannerViewHolder {
        val itemView =
            LayoutInflater.from(parent.context).inflate(R.layout.item_banner_card, parent, false)
        return BannerViewHolder(itemView)
    }

    override fun onBindViewHolder(
        holder: BannerViewHolder,
        position: Int
    ) {
        val currItem = bannerList[position]

        holder.bannerImage.setImageResource(currItem.image)
        holder.bannerTag.text = currItem.tag
        holder.bannerTag.backgroundTintList =
            android.content.res.ColorStateList.valueOf(currItem.tagColor)
        holder.bannerTitle.text = currItem.title
        holder.bannerSubtitle.text = currItem.subtitle
    }

    override fun getItemCount(): Int {
        return bannerList.size
    }


}