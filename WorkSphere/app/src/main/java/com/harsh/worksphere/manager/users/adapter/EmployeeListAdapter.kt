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

class EmployeeListAdapter(
    private val onItemClick: (User) -> Unit,
    private val supervisorNameResolver: (String) -> String
) : ListAdapter<User, EmployeeListAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.manager_item_employee_card, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val profileImage: ImageView = itemView.findViewById(R.id.employeeProfileImage)
        private val statusDot: View = itemView.findViewById(R.id.employeeStatusDot)
        private val name: TextView = itemView.findViewById(R.id.employeeName)
        private val supervisorText: TextView = itemView.findViewById(R.id.employeeSupervisor)
        private val statusBadge: TextView = itemView.findViewById(R.id.employeeStatusBadge)

        fun bind(user: User) {
            name.text = user.name

            // Show assigned supervisor or "Unassigned"
            if (!user.mySupervisor.isNullOrEmpty()) {
                val supervisorName = supervisorNameResolver(user.mySupervisor)
                supervisorText.text = "Assigned to: $supervisorName"
            } else {
                supervisorText.text = "Unassigned"
            }

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
