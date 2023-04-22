package com.mact.proxyproof

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_resetpassword.*


class ResetPasswordActivity: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_resetpassword)

        btnResetPass.setOnClickListener{
            val email = etResetEmail.text.toString().trim()
            Log.d("ResetPassword", email)
            if(validateEmail()){
                Firebase.auth.sendPasswordResetEmail(email).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this,"Email Sent Successfully", Toast.LENGTH_LONG).show()
                        val alert = "Password reset Mail has been sent to your \\nEmail Address!!"
                        tvResetAlert.text = alert

                    }
                }.addOnFailureListener {
                    Log.d("ResetPassword","Failed to Send reset password email $email")

                }
//            btnBackToLogin.visibility = View.VISIBLE
//            btnResetPass.visibility = View.INVISIBLE
//            mainActivityView.replaceFragment(fragment)
            }
        }
        btnBackToLogin.setOnClickListener{

        }
    }
    private fun validateEmail() : Boolean{
        val email  = etResetEmail.text.toString().trim()
        val emailRegex : Regex = "^[a-zA-Z0-9+_.-]+@[a-zA-Z0-9.-]+$".toRegex()
        Log.d("ResetPassword", email)
        if (email.isEmpty()){
            tiResetEmail.error = "Enter Your Email"
            return false
        }
        else if(!email.matches(emailRegex)){
            tiResetEmail.error = "Invalid Email Address"
            return false
        }
        else
        {
            tiResetEmail.isErrorEnabled = false
            tiResetEmail.error = null
            return true
        }
    }
}