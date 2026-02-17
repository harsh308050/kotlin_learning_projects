// com.harsh.worksphere.manager.sites.ui.adapter/SupervisorAdapter.kt
package com.harsh.worksphere.manager.sites.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.card.MaterialCardView
import com.harsh.worksphere.R
import com.harsh.worksphere.initial.auth.data.model.User

class SupervisorAdapter(
    private val onItemClick: (User) -> Unit
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


            card.setOnClickListener {
                val previousPosition = selectedPosition
                selectedPosition = adapterPosition
                notifyItemChanged(previousPosition)
                notifyItemChanged(selectedPosition)
                onItemClick(supervisor)
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