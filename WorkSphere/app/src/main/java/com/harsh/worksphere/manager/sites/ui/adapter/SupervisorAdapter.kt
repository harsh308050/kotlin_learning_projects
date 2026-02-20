// com.harsh.worksphere.manager.sites.ui.adapter/SupervisorAdapter.kt
package com.harsh.worksphere.manager.sites.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.card.MaterialCardView
import com.harsh.worksphere.R
import com.harsh.worksphere.initial.auth.data.model.User

class SupervisorAdapter(
    private val onItemClick: (User) -> Unit,
    private val showAssignmentStatus: Boolean = false,
    private val currentSiteId: String = "",
    private val onReassignRequested: ((User, (Boolean) -> Unit) -> Unit)? = null
) : ListAdapter<User, SupervisorAdapter.ViewHolder>(DiffCallback()) {

    private var selectedPosition = -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.selected_employees_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position), position == selectedPosition)
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val card: MaterialCardView = itemView.findViewById(R.id.employee_card)
        private val nameText: TextView = itemView.findViewById(R.id.selected_supervisor_name)
        private val emailText: TextView = itemView.findViewById(R.id.selected_supervisor_email)
        private val imageView: ImageView = itemView.findViewById(R.id.selected_supervisor_image)
        private val assignedBadge: TextView = itemView.findViewById(R.id.assigned_badge)

        fun bind(supervisor: User, isSelected: Boolean) {
            nameText.text = supervisor.name
            emailText.text = supervisor.email

            // Load profile image with Glide
            Glide.with(itemView.context)
                .load(supervisor.profilePic)
                .placeholder(R.drawable.profile)
                .error(R.drawable.profile)
                .circleCrop()
                .into(imageView)

            val isAssignedElsewhere = showAssignmentStatus &&
                    !supervisor.assignedSite.isNullOrEmpty() &&
                    supervisor.assignedSite != currentSiteId

            // Show assigned badge
            if (isAssignedElsewhere) {
                assignedBadge.isVisible = true
                assignedBadge.text = "Assigned to Site"
                assignedBadge.backgroundTintList =
                    ContextCompat.getColorStateList(itemView.context, R.color.orange)
            } else {
                assignedBadge.isVisible = false
            }

            // Visual selection state
            if (isSelected) {
                card.strokeColor = ContextCompat.getColor(itemView.context, R.color.primary_blue)
                card.strokeWidth = 4
                card.setCardBackgroundColor(ContextCompat.getColor(itemView.context, R.color.primary_blue_light))
            } else {
                card.strokeColor = ContextCompat.getColor(itemView.context, R.color.light_grey)
                card.strokeWidth = 2
                card.cardElevation = 2f
                card.setCardBackgroundColor(ContextCompat.getColor(itemView.context, R.color.white))
            }

            card.setOnClickListener {
                if (isAssignedElsewhere && onReassignRequested != null) {
                    onReassignRequested.invoke(supervisor) { confirmed ->
                        if (confirmed) {
                            val previousPosition = selectedPosition
                            selectedPosition = adapterPosition
                            notifyItemChanged(previousPosition)
                            notifyItemChanged(selectedPosition)
                            onItemClick(supervisor)
                        }
                    }
                } else {
                    val previousPosition = selectedPosition
                    selectedPosition = adapterPosition
                    notifyItemChanged(previousPosition)
                    notifyItemChanged(selectedPosition)
                    onItemClick(supervisor)
                }
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<User>() {
        override fun areItemsTheSame(oldItem: User, newItem: User): Boolean =
            oldItem.email == newItem.email
        override fun areContentsTheSame(oldItem: User, newItem: User): Boolean =
            oldItem == newItem
    }

    fun getSelectedSupervisor(): User? {
        return if (selectedPosition >= 0) getItem(selectedPosition) else null
    }
}