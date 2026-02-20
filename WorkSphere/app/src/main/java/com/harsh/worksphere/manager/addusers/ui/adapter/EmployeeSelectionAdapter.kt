package com.harsh.worksphere.manager.addusers.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
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

class EmployeeSelectionAdapter(
    private val onSelectionChanged: (List<User>) -> Unit,
    private val singleSelection: Boolean = false,
    private val showAssignmentStatus: Boolean = false,
    private val onReassignRequested: ((User, (Boolean) -> Unit) -> Unit)? = null // Callback with result handler
) : ListAdapter<User, EmployeeSelectionAdapter.ViewHolder>(DiffCallback()) {

    private val selectedEmployees = mutableSetOf<User>()
    private val pendingReassignments = mutableSetOf<String>() // Track employees pending reassignment

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.selected_employees_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    fun getSelectedEmployees(): List<User> = selectedEmployees.toList()

    fun getPendingReassignments(): Set<String> = pendingReassignments.toSet()

    fun setSelectedEmployees(employees: List<User>) {
        selectedEmployees.clear()
        selectedEmployees.addAll(employees)
        notifyDataSetChanged()
    }

    fun confirmReassignment(employeeEmail: String) {
        pendingReassignments.add(employeeEmail)
        notifyDataSetChanged()
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val cardView: MaterialCardView = itemView.findViewById(R.id.employee_card)
        private val employeeImage: ImageView = itemView.findViewById(R.id.selected_supervisor_image)
        private val employeeName: TextView = itemView.findViewById(R.id.selected_supervisor_name)
        private val employeeEmail: TextView = itemView.findViewById(R.id.selected_supervisor_email)
        private val assignedBadge: TextView = itemView.findViewById(R.id.assigned_badge)

        fun bind(employee: User) {
            employeeName.text = employee.name
            employeeEmail.text = employee.email

            Glide.with(itemView.context)
                .load(employee.profilePic)
                .placeholder(R.drawable.profile)
                .circleCrop()
                .into(employeeImage)

            val isSelected = selectedEmployees.contains(employee)
            val isPendingReassignment = pendingReassignments.contains(employee.email)
            val isAlreadyAssigned = !employee.mySupervisor.isNullOrEmpty()

            // Show assignment badge if enabled and assigned to someone else
            if (showAssignmentStatus && isAlreadyAssigned && !isPendingReassignment) {
                assignedBadge.isVisible = true
                assignedBadge.text = "Assigned"
                assignedBadge.backgroundTintList = ContextCompat.getColorStateList(itemView.context, R.color.orange)
            } else if (isPendingReassignment) {
                assignedBadge.isVisible = true
                assignedBadge.text = "Reassigning"
                assignedBadge.backgroundTintList = ContextCompat.getColorStateList(itemView.context, R.color.primary_blue_dark)

            } else {
                assignedBadge.isVisible = false
            }

            // Visual indication
            when {
                isPendingReassignment -> {
                    cardView.strokeColor = ContextCompat.getColor(itemView.context, R.color.primary_blue_dark)
                    cardView.strokeWidth = 4
                    cardView.setCardBackgroundColor(ContextCompat.getColor(itemView.context, R.color.primary_blue_light))
                }
                isSelected -> {
                    cardView.strokeColor = ContextCompat.getColor(itemView.context, R.color.primary_blue)
                    cardView.strokeWidth = 4
                    cardView.setCardBackgroundColor(ContextCompat.getColor(itemView.context, R.color.primary_blue_light))
                }
                else -> {
                    cardView.strokeColor = ContextCompat.getColor(itemView.context, R.color.light_grey)
                    cardView.strokeWidth = 2
                    cardView.cardElevation = 2f
                    cardView.setCardBackgroundColor(ContextCompat.getColor(itemView.context, R.color.white))
                }
            }

            itemView.setOnClickListener {
                handleClick(employee, isAlreadyAssigned, isPendingReassignment)
            }
        }

        private fun handleClick(employee: User, isAlreadyAssigned: Boolean, isPendingReassignment: Boolean) {
            if (isPendingReassignment) {
                pendingReassignments.remove(employee.email)
                selectedEmployees.remove(employee)
                notifyItemChanged(adapterPosition)
                onSelectionChanged(selectedEmployees.toList())
                return
            }

            if (isAlreadyAssigned && !selectedEmployees.contains(employee)) {
                // Request reassignment confirmation
                onReassignRequested?.invoke(employee) { confirmed ->
                    if (confirmed) {
                        // Only update UI, don't touch Firestore yet
                        pendingReassignments.add(employee.email)
                        selectedEmployees.add(employee)
                        notifyDataSetChanged()
                        onSelectionChanged(selectedEmployees.toList())
                    }
                }
                return
            }

            // Normal selection toggle
            if (selectedEmployees.contains(employee)) {
                selectedEmployees.remove(employee)
            } else {
                selectedEmployees.add(employee)
            }
            notifyItemChanged(adapterPosition)
            onSelectionChanged(selectedEmployees.toList())
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<User>() {
        override fun areItemsTheSame(oldItem: User, newItem: User): Boolean =
            oldItem.email == newItem.email
        override fun areContentsTheSame(oldItem: User, newItem: User): Boolean =
            oldItem == newItem
    }
}