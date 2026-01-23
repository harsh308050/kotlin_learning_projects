package com.harsh.mydemo.home

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.harsh.mydemo.R
import com.harsh.mydemo.users.UsersActivity

class HomescreenActivity : AppCompatActivity() {

    fun navToRecyclerView(){
        val recyclerBtn = findViewById<Button>(R.id.recyclerScreenBtn)
        recyclerBtn.setOnClickListener {
            startActivity(Intent(this, UsersActivity::class.java))
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_homescreen)
        val emailText = findViewById<TextView>(R.id.emailEdit)
        val passText = findViewById<TextView>(R.id.passEdit)
        emailText.text = intent.getStringExtra("email")
        passText.text = intent.getStringExtra("password")
        navToRecyclerView()

    }
}