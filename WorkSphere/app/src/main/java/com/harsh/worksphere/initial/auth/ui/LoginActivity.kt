package com.harsh.worksphere.initial.auth.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.button.MaterialButton
import com.harsh.worksphere.R
import com.harsh.worksphere.employee.home.EmployeeHomeActivity
import com.harsh.worksphere.initial.auth.data.repo.*
import com.harsh.worksphere.initial.auth.data.model.*
import com.harsh.worksphere.initial.auth.data.remote.*
import com.harsh.worksphere.initial.auth.viewmodel.AuthViewModel
import com.harsh.worksphere.manager.home.ManagerHomeActivity
import com.harsh.worksphere.supervisor.home.SupervisorHomeActivity
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var continueWithGoogleBtn: MaterialButton
    private lateinit var progressBar: ProgressBar
    private lateinit var viewModel: AuthViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login_screen)

        continueWithGoogleBtn = findViewById(R.id.continue_with_google_btn)
        progressBar = findViewById(R.id.login_progress_bar)

        val authRepository = AuthRepository(
            GoogleSignInDataSource(this),
            AuthDataSource()
        )
        val userRepository = UserRepository(FirestoreDataSource())

        viewModel = ViewModelProvider(
            this,
            AuthViewModel.Factory(authRepository, userRepository)
        )[AuthViewModel::class.java]

        setupUI()
        observeViewModel()
    }

    private fun setupUI() {
        continueWithGoogleBtn.setOnClickListener {
            viewModel.signInWithGoogle()
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    handleState(state)
                }
            }
        }
    }

    private fun handleState(state: AuthViewModel.UiState) {
        when (state) {
            is AuthViewModel.UiState.Idle -> {
                showLoading(false)
            }
            is AuthViewModel.UiState.Loading -> {
                showLoading(true)
            }
            is AuthViewModel.UiState.Success -> {
                showLoading(false)
                navigateToHome(state.authResult)
            }
            is AuthViewModel.UiState.Error -> {
                showLoading(false)
                showError(state.message)
                viewModel.resetState()
            }
        }
    }

    private fun showLoading(isLoading: Boolean) {

        if(isLoading){
            continueWithGoogleBtn.text= ""
            continueWithGoogleBtn.icon = null
            progressBar.visibility = View.VISIBLE
            continueWithGoogleBtn.isEnabled = false
        }else{
            continueWithGoogleBtn.text = getText(R.string.continue_with_google)
            continueWithGoogleBtn.icon = ContextCompat.getDrawable(this,R.drawable.google)
            progressBar.visibility = View.GONE
            continueWithGoogleBtn.isEnabled = true
        }
    }

    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    private fun navigateToHome(authResult: AuthResult) {
        val targetClass = when (authResult.role) {
            is UserRole.Manager -> ManagerHomeActivity::class.java
            is UserRole.Supervisor -> SupervisorHomeActivity::class.java
            is UserRole.Employee -> EmployeeHomeActivity::class.java
        }

        val intent = Intent(this, targetClass).apply {
            putExtra("USER_MAIL", authResult.user?.email)
            putExtra("IS_NEW_USER", authResult.isNewUser)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        finish()
    }
}