package com.harsh.shopit.main.screens.shop.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.harsh.shopit.R
import com.harsh.shopit.main.screens.shop.data.model.Product

class ShopAdapter(
    private val shopProdList: ArrayList<Product>,
    private val listener: OnProductActionListener
) : RecyclerView.Adapter<ShopAdapter.ShopViewHolder>() {
    private val wishlistIds = mutableSetOf<Int>()
    fun updateWishlistIds(ids: Set<Int>) {
        wishlistIds.clear()
        wishlistIds.addAll(ids)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ShopViewHolder {
        val shopItemView =
            LayoutInflater.from(parent.context).inflate(R.layout.customer_item_shop_product, parent, false)
        return ShopViewHolder(shopItemView)
    }

    override fun onBindViewHolder(
        holder: ShopViewHolder,
        position: Int
    ) {
        val currItem = shopProdList[position]
        val isWishlisted = wishlistIds.contains(currItem.id)
        holder.prodTitle.text = currItem.title
        holder.prodCategory.text = currItem.category
        holder.prodPrice.text = "$${currItem.price}"
        holder.prodDiscount.text = "${currItem.discountPercentage} %"
        Glide.with(holder.itemView.context).load(currItem.thumbnail).into(holder.prodImage)
        holder.prodAddFav.setOnClickListener {
            listener.onFavClick(currItem)
        }
        holder.prodAddFav.setImageResource(
            if (isWishlisted) {
                R.drawable.addedfav
            } else R.drawable.fav
        )
        holder.prodAddFav.setColorFilter(
            holder.itemView.context.getColor(
                if (isWishlisted) {
                    R.color.red
                } else {
                    R.color.black
                }

            )
        )
    }

    override fun getItemCount(): Int {
        return shopProdList.size
    }

    class ShopViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val prodImage: ImageView = itemView.findViewById(R.id.prodImage)
        val prodTitle: TextView = itemView.findViewById(R.id.prodName)
        val prodCategory: TextView = itemView.findViewById(R.id.prodCategory)
        val prodPrice: TextView = itemView.findViewById(R.id.prodPrice)
        val prodDiscount: TextView = itemView.findViewById(R.id.prodDiscount)
        val prodAddFav: ImageButton = itemView.findViewById(R.id.addToFav)
    }

    fun updateList(newList: List<Product>) {
        shopProdList.clear()
        shopProdList.addAll(newList)
        notifyDataSetChanged()
    }

}