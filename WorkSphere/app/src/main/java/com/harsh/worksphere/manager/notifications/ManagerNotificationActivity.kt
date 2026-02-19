package com.harsh.worksphere.manager.notifications

import android.os.Bundle
import android.widget.ImageButton
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.harsh.worksphere.R

class ManagerNotificationActivity : AppCompatActivity() {
    private lateinit var navback: ImageButton
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.manager_notification_activity)
        initView()
        navback()
    }
    private fun initView(){
        navback = findViewById(R.id.navback)

    }
    private fun navback(){
        navback.setOnClickListener {
            finish()
        }
    }

}
