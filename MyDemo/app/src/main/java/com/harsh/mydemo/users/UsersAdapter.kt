package com.harsh.mydemo.users

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.harsh.mydemo.R

class UsersAdapter(private val userList: ArrayList<UsersData>) :
    RecyclerView.Adapter<UsersAdapter.UserViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): UserViewHolder {
        val itemView =
            LayoutInflater.from(parent.context).inflate(R.layout.item_recyclerview, parent, false)
        return UserViewHolder(itemView)
    }

    override fun onBindViewHolder(
        holder: UserViewHolder,
        position: Int
    ) {
        val currItem = userList[position]
        holder.profileimage.setImageResource(currItem.image)
        holder.userEmail.text = currItem.email
        holder.userPass.text = currItem.password
        holder.itemView.setOnClickListener {
            val intent= Intent(holder.itemView.context, UsersDetailsActivity::class.java)
            intent.putExtra("image", currItem.image)
            intent.putExtra("email", currItem.email)
            intent.putExtra("password", currItem.password)
            holder.itemView.context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return userList.size
    }

    class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val profileimage = itemView.findViewById<ImageView>(R.id.profileImg)
        val userEmail = itemView.findViewById<TextView>(R.id.userEmail)
        val userPass = itemView.findViewById<TextView>(R.id.userPass)
    }
}