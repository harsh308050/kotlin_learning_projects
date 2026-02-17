package com.harsh.worksphere.manager.addusers.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.card.MaterialCardView
import com.harsh.worksphere.R
import com.harsh.worksphere.manager.sites.data.model.SiteModel
import java.io.File

class SiteSelectionAdapter(
    private val onSiteSelected: (SiteModel) -> Unit
) : ListAdapter<SiteModel, SiteSelectionAdapter.ViewHolder>(DiffCallback()) {

    private var selectedPosition = -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.manager_site_item_card, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position), position == selectedPosition)
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val cardView: MaterialCardView = itemView.findViewById(R.id.selected_site_card) // Add this ID to your layout
        private val siteImage: ImageView = itemView.findViewById(R.id.selected_site_image)
        private val siteName: TextView = itemView.findViewById(R.id.selected_site_name)
        private val siteAddress: TextView = itemView.findViewById(R.id.selected_site_address)
        private val siteDescription: TextView = itemView.findViewById(R.id.selected_site_description)

        fun bind(site: SiteModel, isSelected: Boolean) {
            siteName.text = site.siteName
            siteAddress.text = site.location.address
            siteDescription.text = site.workDetails

            // Load site image
            if (site.siteImageUrl.isNotEmpty() && File(site.siteImageUrl).exists()) {
                Glide.with(itemView.context)
                    .load(File(site.siteImageUrl))
                    .placeholder(R.drawable.siteeee)
                    .into(siteImage)
            } else {
                siteImage.setImageResource(R.drawable.siteeee)
            }

            // Fix: Use card stroke instead of itemView background and remove alpha fade
            if (isSelected) {
                cardView.strokeColor = ContextCompat.getColor(itemView.context, R.color.primary_blue)
                cardView.strokeWidth = 4 // Thicker border
                cardView.cardElevation = 8f
                cardView.setCardBackgroundColor(ContextCompat.getColor(itemView.context, R.color.primary_blue_light))
            } else {
                cardView.strokeColor = ContextCompat.getColor(itemView.context, R.color.light_grey)
                cardView.strokeWidth = 1 // Normal border
                cardView.cardElevation = 2f
                cardView.setCardBackgroundColor(ContextCompat.getColor(itemView.context, R.color.white))
            }

            // Remove the alpha fade that causes white overlay
            // itemView.alpha = if (isSelected) 1.0f else 0.7f // REMOVED THIS LINE

            itemView.setOnClickListener {
                val previousPosition = selectedPosition
                selectedPosition = adapterPosition
                notifyItemChanged(previousPosition)
                notifyItemChanged(selectedPosition)
                onSiteSelected(site)
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<SiteModel>() {
        override fun areItemsTheSame(oldItem: SiteModel, newItem: SiteModel): Boolean =
            oldItem.siteId == newItem.siteId
        override fun areContentsTheSame(oldItem: SiteModel, newItem: SiteModel): Boolean =
            oldItem == newItem
    }
}