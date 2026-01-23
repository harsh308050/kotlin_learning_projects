package com.harsh.shopit.initial

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.harsh.shopit.R
import com.harsh.shopit.auth.LoginActivity
import com.harsh.shopit.auth.SignupActivity

class GetStartedActivity : AppCompatActivity() {
    fun btnClickHandler(){
        val loginBtn = findViewById<Button>(R.id.login)
        val signupbtn = findViewById<Button>(R.id.signup)

        loginBtn.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
        signupbtn.setOnClickListener {
            val intent = Intent(this, SignupActivity::class.java)
            startActivity(intent)
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        setContentView(R.layout.activity_get_started)

        btnClickHandler()

    }
}