package com.harsh.shopit.auth

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import com.harsh.shopit.R
import com.harsh.shopit.main.MainHomeActivity

class LoginActivity : AppCompatActivity() {
    fun btnClickHandler() {
        val signupNowbtn = findViewById<Button>(R.id.signupNow)
        val navigateBack = findViewById<ImageButton>(R.id.navback)
        navigateBack.setOnClickListener {
            finish()
        }
        signupNowbtn.setOnClickListener {
            val intent = Intent(this, SignupActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    fun loginBtnHandler() {
        val emailField = findViewById<EditText>(R.id.emailField)
        val passwordField = findViewById<EditText>(R.id.passwordField)
        val loginBtn = findViewById<Button>(R.id.loginBtn)
        loginBtn.setOnClickListener {
            if (emailField != null && !Patterns.EMAIL_ADDRESS.matcher(emailField.text).matches()) {
                emailField.error = "Please enter Valid email"
                emailField.requestFocus()
                return@setOnClickListener
            }

            if (passwordField.length() < 6) {
                passwordField.error = "Password length must be at least 6 characters"
                passwordField.requestFocus()
                return@setOnClickListener
            }
            val intent = Intent(this, MainHomeActivity:: class.java)
            startActivity(intent)
            finishAffinity() //clears entire background stack //finish - replaces //finishAffinity - clears everything in stack
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        btnClickHandler()
        loginBtnHandler()
    }
}