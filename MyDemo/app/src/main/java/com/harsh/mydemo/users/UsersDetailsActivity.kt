package com.harsh.mydemo.users

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.harsh.mydemo.R

class UsersDetailsActivity : AppCompatActivity() {
    fun showDetails(){
        val image = findViewById<ImageView>(R.id.profileImg)
        val emailTxt = findViewById<TextView>(R.id.userMail)
        val passTxt = findViewById<TextView>(R.id.userPassword)

        image.setImageResource(intent.getIntExtra("image", 0))
        emailTxt.text = intent.getStringExtra("email")
        passTxt.text = intent.getStringExtra("password")
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_users_details)
        showDetails()
    }
}