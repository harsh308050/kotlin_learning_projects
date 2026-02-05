package com.harsh.shopit.seller.products.showProds.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.harsh.shopit.R
import com.harsh.shopit.seller.products.local.entity.SellerAddProductEntity

class ProductAdapter(private val listener: OnDeleteActionListener, private val editListener: OnEditActionListener): RecyclerView.Adapter<ProductAdapter.SellerProductsViewHolder>() {
    private val items = mutableListOf<SellerAddProductEntity>()
    fun submitList(newList: List<SellerAddProductEntity>){
        items.clear()
        items.addAll(newList)
        notifyDataSetChanged()
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SellerProductsViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.seller_item_prod, parent, false)
        return SellerProductsViewHolder(view)
    }
    override fun onBindViewHolder(holder: SellerProductsViewHolder, position: Int) {
        val product = items[position]

        holder.title.text = product.prodName
        holder.price.text = "$${product.prodPrice}"
        holder.category.text = product.prodCategory
        holder.desc.text = product.prodDescription
        Glide.with(holder.itemView.context)
            .load(product.prodImage)
            .into(holder.image)
        holder.removeBtn.setOnClickListener {
            listener.onRemoveClick(product)
        }
        holder.editBtn.setOnClickListener {
            editListener.onEditClick(product)
        }
    }
    override fun getItemCount(): Int = items.size
    class SellerProductsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val editBtn: ImageButton = itemView.findViewById(R.id.sellerProdEdit)
        val image: ImageView = itemView.findViewById(R.id.sellerProdImage)
        val title: TextView = itemView.findViewById(R.id.sellerProdName)
        val price: TextView = itemView.findViewById(R.id.sellerProdPrice)
        val category: TextView = itemView.findViewById(R.id.sellerProdCategory)
        val desc: TextView = itemView.findViewById(R.id.sellerProdDesc)
        val removeBtn: ImageButton = itemView.findViewById(R.id.sellerProdDelete)
    }
}