package com.harsh.shopit.initial

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.DecelerateInterpolator
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.harsh.shopit.R

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        val logo = findViewById<ImageView>(R.id.logo)

        logo.alpha = 0f
        logo.scaleX = 1f
        logo.scaleY = 1f

        logo.animate()
            .alpha(1.2f)
            .scaleX(1.2f)
            .scaleY(1.2f)
            .setDuration(2200)
            .setInterpolator(DecelerateInterpolator())
            .start()

        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(Intent(this, GetStartedActivity::class.java))
            finish()
        }, 2500)
    }
}