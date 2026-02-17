package com.harsh.worksphere.initial.splash

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.lifecycle.lifecycleScope
import com.google.firebase.auth.FirebaseAuth
import com.harsh.worksphere.R
import com.harsh.worksphere.initial.auth.data.model.UserRole
import com.harsh.worksphere.initial.auth.data.remote.FirestoreDataSource
import com.harsh.worksphere.initial.auth.data.repo.UserRepository
import com.harsh.worksphere.initial.auth.ui.LoginActivity
import com.harsh.worksphere.manager.home.ManagerHomeActivity
import kotlinx.coroutines.launch
import com.harsh.worksphere.core.utils.Result

class SplashScreen : AppCompatActivity() {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private lateinit var userRepository: UserRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        userRepository = UserRepository(FirestoreDataSource())

        val logo = findViewById<CardView>(R.id.splash_logo)
        logo.startAnimation(AnimationUtils.loadAnimation(this, R.anim.splash_fade_scale))

        Handler(Looper.getMainLooper()).postDelayed({
            checkAuthAndNavigate()
        }, 2500)
    }

    private fun checkAuthAndNavigate() {
        val currentUser = auth.currentUser

        if (currentUser != null) {
            fetchUserAndNavigate(currentUser.email.toString())
        } else {
            navigateToLogin()
        }
    }

    private fun fetchUserAndNavigate(useremail: String) {
        lifecycleScope.launch {
            try {
                when (val result = userRepository.getUser(useremail)) {
                    is Result.Success -> {
                        val user = result.data
                        if (user != null) {
                            navigateToHome(user.role, useremail)
                        } else {
                            navigateToLogin()
                        }
                    }
                    is Result.Error -> {
                        showError(result.message)
                        navigateToLogin()
                    }
                    is Result.Loading -> {
                    }
                }
            } catch (e: Exception) {
                showError("Error: ${e.message}")
                navigateToLogin()
            }
        }
    }

    private fun navigateToHome(role: UserRole, usermail: String) {
        val intent = when (role) {
            is UserRole.Manager -> Intent(this, ManagerHomeActivity::class.java)
            is UserRole.Supervisor -> Intent(this, ManagerHomeActivity::class.java)
            is UserRole.Employee -> Intent(this, ManagerHomeActivity::class.java)
        }.apply {
            putExtra("USER_MAIL", usermail)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        startActivity(intent)
        finish()
    }

    private fun navigateToLogin() {
        // Sign out just in case
        auth.signOut()

        val intent = Intent(this, LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        finish()
    }

    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}