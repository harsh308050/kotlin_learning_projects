package com.harsh.shopit.initial

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Button
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.harsh.shopit.R
import com.harsh.shopit.auth.login.ui.LoginActivity
import com.harsh.shopit.seller.auth.ui.SellerAuthActivity

class GetStartedActivity : AppCompatActivity() {
    private val requestPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {}

    fun btnClickHandler(){
        val customerBtn = findViewById<Button>(R.id.customer)
        val sellerBtn = findViewById<Button>(R.id.seller)

        customerBtn.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
        sellerBtn.setOnClickListener {
            val intent = Intent(this, SellerAuthActivity::class.java)
            startActivity(intent)
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.customer_activity_get_started)
       if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
           requestPermission.launch(android.Manifest.permission.POST_NOTIFICATIONS)
       }
        btnClickHandler()
    }
}