package com.harsh.mydemo.initial

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.harsh.mydemo.R

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        Handler(Looper.getMainLooper()).postDelayed({
            val splashintent = Intent(this, GetStartedActivity::class.java)
            startActivity(splashintent)
            finish()
        }, 2000) // 2 seconds
    }
}