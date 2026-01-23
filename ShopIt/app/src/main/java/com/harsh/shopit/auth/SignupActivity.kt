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

class SignupActivity : AppCompatActivity() {
    fun btnClickHandler() {
        val loginNowBtn = findViewById<Button>(R.id.loginNow)
        val navigateBack = findViewById<ImageButton>(R.id.navback)
        navigateBack.setOnClickListener {
            finish()
        }
        loginNowBtn.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    fun signupBtnHandler() {
        val nameField = findViewById<EditText>(R.id.nameField)
        val confPasswordField = findViewById<EditText>(R.id.confPasswordField)
        val emailField = findViewById<EditText>(R.id.emailFieldSignup)
        val passwordField = findViewById<EditText>(R.id.passwordFieldSignup)
        val signupBtn = findViewById<Button>(R.id.signupBtn)
        signupBtn.setOnClickListener {
            if (nameField != null && nameField.text.toString().isEmpty()) {
                nameField.error = "Please enter name"
                nameField.requestFocus()
                return@setOnClickListener
            }
            if (emailField != null && !Patterns.EMAIL_ADDRESS.matcher(emailField.text).matches()) {
                emailField.error = "Please enter Valid email"
                emailField.requestFocus()
                return@setOnClickListener
            }
            if (passwordField.length() < 6) {
                passwordField.error = "Please enter password"
                passwordField.requestFocus()
                return@setOnClickListener
            }
            if (confPasswordField != null && confPasswordField.text.toString() != passwordField.text.toString()) {
                confPasswordField.error = "Password does not match"
                confPasswordField.requestFocus()
                return@setOnClickListener
            }
            val intent = Intent(this, MainHomeActivity:: class.java)
            startActivity(intent)
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)
        btnClickHandler()
        signupBtnHandler()
    }
}