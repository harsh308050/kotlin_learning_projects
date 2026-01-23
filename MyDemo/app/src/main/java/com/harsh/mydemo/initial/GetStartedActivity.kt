package com.harsh.mydemo.initial

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.harsh.mydemo.R
import com.harsh.mydemo.auth.AuthActivity

class GetStartedActivity : AppCompatActivity() {
    fun getStart(){
        val getstartbtn = findViewById<Button>(R.id.getstart_btn)
        getstartbtn.setOnClickListener {
            startActivity(Intent(this, AuthActivity::class.java))
        }
    }
    fun openGoogle(){
        val url = "https://www.google.com"
        val openGoogleBtn = findViewById<Button>(R.id.google_btn)
        openGoogleBtn.setOnClickListener {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_get_started)
        getStart()
        openGoogle()
    }
}