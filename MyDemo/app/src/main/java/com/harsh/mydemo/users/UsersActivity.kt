package com.harsh.mydemo.users

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.harsh.mydemo.R

class UsersActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var userList: ArrayList<UsersData>

    fun userdata() {
        userList = arrayListOf(
            UsersData(R.drawable.applogo, "harsh1@gmail.com", "harsh1"),
            UsersData(R.drawable.applogo, "harsh2@gmail.com", "harsh2"),
            UsersData(R.drawable.applogo, "harsh3@gmail.com", "harsh3"),
            UsersData(R.drawable.applogo, "harsh4@gmail.com", "harsh4"),
            UsersData(R.drawable.applogo, "harsh5@gmail.com", "harsh5")
        )
        recyclerView = findViewById(R.id.recyclerActivityView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.setHasFixedSize(true)
        recyclerView.adapter = UsersAdapter(userList)

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_users_recycler_view)
        userdata()

    }

}