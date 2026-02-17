// com.harsh.worksphere.manager.sites.ui.adapter/SitesAdapter.kt
package com.harsh.worksphere.manager.sites.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.harsh.worksphere.R
import com.harsh.worksphere.manager.sites.data.model.SiteModel
import com.harsh.worksphere.manager.sites.data.model.SiteStatus
import java.io.File

class SitesAdapter(
    private val onItemClick: (SiteModel) -> Unit,
    private val onDeleteClick: (SiteModel) -> Unit
) : ListAdapter<SiteModel, SitesAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.manager_sites_fragment_item_card, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val siteImage: ImageView = itemView.findViewById(R.id.siteImage)
        private val siteName: TextView = itemView.findViewById(R.id.siteName)
        private val siteAddress: TextView = itemView.findViewById(R.id.siteCategory)
        private val statusBadge: TextView = itemView.findViewById(R.id.siteStatusBadge)
        private val deleteBtn: ImageButton = itemView.findViewById(R.id.site_dlt_btn)

        fun bind(site: SiteModel) {
            siteName.text = site.siteName
            siteAddress.text = site.location.address

            // Show site status badge
            statusBadge.text = site.status.displayName
            val statusColorRes = when (site.status) {
                SiteStatus.PENDING_ASSIGNMENT -> R.color.orange
                SiteStatus.ASSIGNED -> R.color.primary_blue
                SiteStatus.IN_PROGRESS -> R.color.yellow
                SiteStatus.COMPLETED -> R.color.green
                SiteStatus.ON_HOLD -> R.color.dark_grey
                SiteStatus.CANCELLED -> R.color.red
            }
            statusBadge.backgroundTintList = ContextCompat.getColorStateList(itemView.context, statusColorRes)

            // Load site image from local path - SAME AS ACTIVITY
            when {
                site.siteImageUrl.isNotEmpty() && File(site.siteImageUrl).exists() -> {
                    // Load from local file path
                    Glide.with(itemView.context)
                        .load(File(site.siteImageUrl))
                        .placeholder(R.drawable.siteeee)
                        .error(R.drawable.siteeee)
                        .into(siteImage)
                }
                site.siteImageUrl.isNotEmpty() -> {
                    // Try loading as URI if file doesn't exist
                    Glide.with(itemView.context)
                        .load(site.siteImageUrl)
                        .placeholder(R.drawable.siteeee)
                        .error(R.drawable.siteeee)
                        .into(siteImage)
                }
                else -> {
                    // Show default image
                    siteImage.setImageResource(R.drawable.siteeee)
                }
            }

            itemView.setOnClickListener { onItemClick(site) }
            deleteBtn.setOnClickListener { onDeleteClick(site) }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<SiteModel>() {
        override fun areItemsTheSame(oldItem: SiteModel, newItem: SiteModel): Boolean =
            oldItem.siteId == newItem.siteId
        override fun areContentsTheSame(oldItem: SiteModel, newItem: SiteModel): Boolean =
            oldItem == newItem
    }
}