package com.harsh.learningmvvm.view

import android.os.Bundle
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.button.MaterialButton
import com.harsh.learningmvvm.R
import com.harsh.learningmvvm.viewmodel.CalcViewModel

class MainActivity : AppCompatActivity() {

    lateinit var calcViewModel: CalcViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        calcViewModel = ViewModelProvider(this).get(CalcViewModel::class.java)

        val btn = findViewById<MaterialButton>(R.id.resBtn)
        btn.setOnClickListener {
            val num1field = findViewById<EditText>(R.id.num1)
            val num2field = findViewById<EditText>(R.id.num2)
            val resfield = findViewById<TextView>(R.id.sum)
            val num1 = num1field.text.toString().toIntOrNull() ?: 0
            val num2 = num2field.text.toString().toIntOrNull() ?: 0

            val result = calcViewModel.calc(num1, num2)
            resfield.text = "${result.sum}"
        }
    }
}