    package com.harsh.shopit.auth.login.ui

    import android.content.Intent
    import android.os.Bundle
    import android.util.Log
    import android.view.View
    import android.widget.Button
    import android.widget.EditText
    import android.widget.ImageButton
    import android.widget.ProgressBar
    import android.widget.Toast
    import androidx.appcompat.app.AppCompatActivity
    import androidx.lifecycle.ViewModelProvider
    import com.harsh.shopit.R
    import com.harsh.shopit.auth.SignupActivity
    import com.harsh.shopit.auth.login.viewmodel.LoginViewModel
    import com.harsh.shopit.main.MainHomeActivity
    import com.harsh.shopit.main.utils.Prefs
    import com.harsh.shopit.main.utils.Resource
    import com.harsh.shopit.main.utils.SharedPrefKeys

    class LoginActivity : AppCompatActivity() {

        private lateinit var viewModel: LoginViewModel
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

        fun loginFunctionality() {
            val loginBtn = findViewById<Button>(R.id.loginBtn)
            val loader = findViewById<ProgressBar>(R.id.loginLoader)

            viewModel.loginState.observe(this) { result ->
                when (result) {
                    is Resource.Loading -> {
                        loginBtn.text=""
                        loader.visibility  = View.VISIBLE
                    }

                    is Resource.Success -> {
                        loader.visibility = View.GONE
                        loginBtn.text= getString(R.string.login)
                        val user = result.data
                        Prefs.putString(this, SharedPrefKeys.authtoken, user.accessToken)
                        Prefs.putString(this, SharedPrefKeys.authRefreshToken, user.refreshToken)
                        Log.d("USER", "SAVED TOKEN----------------------------------")
                        Log.d("USER", Prefs.getString(this, SharedPrefKeys.authtoken).toString())
                        Log.d("USER", Prefs.getString(this, SharedPrefKeys.authRefreshToken).toString())
                        startActivity(Intent(this, MainHomeActivity::class.java))
                        finishAffinity()
                    }

                    is Resource.Error -> {
                        loader.visibility = View.GONE
                        loginBtn.text=getString(R.string.login)
                        Toast.makeText(this, result.message, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        fun loginBtnHandler() {
            val emailField = findViewById<EditText>(R.id.emailField)
            val passwordField = findViewById<EditText>(R.id.passwordField)
            val loginBtn = findViewById<Button>(R.id.loginBtn)
            loginBtn.setOnClickListener {
                viewModel.login(emailField.text.toString(), passwordField.text.toString())
            }
        }

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setContentView(R.layout.customer_activity_login)
            viewModel = ViewModelProvider(this)[LoginViewModel::class.java]
            btnClickHandler()
            loginBtnHandler()
            loginFunctionality()
        }
    }