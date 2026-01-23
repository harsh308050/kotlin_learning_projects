package com.harsh.mydemo.auth

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.Fragment
import com.harsh.mydemo.home.HomescreenActivity
import com.harsh.mydemo.R

class Login : Fragment(R.layout.fragment_login) {

    fun login(view: View){
        val email = view.findViewById<EditText>(R.id.login_email)
        val pass = view.findViewById<EditText>(R.id.login_password)
        val loginbtn = view.findViewById<Button>(R.id.login_btn)
        loginbtn?.setOnClickListener {
            if(email != null && !Patterns.EMAIL_ADDRESS.matcher(email.text).matches()){
                    email.error = "Please enter Valid email"
                    email.requestFocus()
                    return@setOnClickListener
                }
           if (pass?.length()!! <= 6){
                pass.error = "Please enter password"
                pass.requestFocus()
                return@setOnClickListener}
            val intent = Intent(requireContext(), HomescreenActivity::class.java)
            intent.putExtra("email", email.text.toString())
            intent.putExtra("password", pass.text.toString())
            startActivity(intent)
        }

    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    login(view)
}


}