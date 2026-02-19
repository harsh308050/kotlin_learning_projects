package com.harsh.worksphere.manager.users.adapter

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
import com.harsh.worksphere.R
import com.harsh.worksphere.initial.auth.data.model.User
import com.harsh.worksphere.initial.auth.data.model.UserStatus

class SupervisorListAdapter(
    private val onItemClick: (User) -> Unit,

) : ListAdapter<User, SupervisorListAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.manager_item_supervisor_card, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val profileImage: ImageView = itemView.findViewById(R.id.supervisorProfileImage)
        private val statusDot: View = itemView.findViewById(R.id.supervisorStatusDot)
        private val name: TextView = itemView.findViewById(R.id.supervisorName)
        private val subtitle: TextView = itemView.findViewById(R.id.supervisorSubtitle)
        private val employeeCount: TextView = itemView.findViewById(R.id.supervisorEmployeeCount)
        private val statusBadge: TextView = itemView.findViewById(R.id.supervisorStatusBadge)

        fun bind(user: User) {
            name.text = user.name

            // Subtitle: email or site info
            subtitle.text = if (!user.assignedSite.isNullOrEmpty()) {
                "Site: ${user.assignedSite}"
            } else {
                user.email
            }

            // Employee count
            val count = user.assignedEmployees.size
            employeeCount.text = if (count == 1) "1 Employee" else "$count Employees"

            // Status badge & dot
            applyStatus(user.status)

            // Profile image
            Glide.with(itemView.context)
                .load(user.profilePic)
                .placeholder(R.drawable.profile)
                .circleCrop()
                .into(profileImage)

            itemView.setOnClickListener { onItemClick(user) }
        }

        private fun applyStatus(status: UserStatus) {
            val context = itemView.context
            when (status) {
                UserStatus.ON_SITE -> {
                    statusBadge.text = "On-site"
                    statusBadge.setTextColor(ContextCompat.getColor(context, R.color.green))
                    statusDot.backgroundTintList = ContextCompat.getColorStateList(context, R.color.green)
                }
                UserStatus.ON_BREAK -> {
                    statusBadge.text = "On Break"
                    statusBadge.setTextColor(ContextCompat.getColor(context, R.color.orange))
                    statusDot.backgroundTintList = ContextCompat.getColorStateList(context, R.color.orange)
                }
                UserStatus.OFFLINE -> {
                    statusBadge.text = "Offline"
                    statusBadge.setTextColor(ContextCompat.getColor(context, R.color.light_grey))
                    statusDot.backgroundTintList = ContextCompat.getColorStateList(context, R.color.light_grey)
                }
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<User>() {
        override fun areItemsTheSame(oldItem: User, newItem: User) = oldItem.email == newItem.email
        override fun areContentsTheSame(oldItem: User, newItem: User) = oldItem == newItem
    }
}
