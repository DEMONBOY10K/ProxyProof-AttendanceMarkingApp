package com.mact.proxyproof.fragments

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.mact.proxyproof.R
import com.mact.proxyproof.TeacherSignUpActivity
import kotlinx.android.synthetic.main.fragment_signup_teacher2.*

class TeacherSignUpFragment2 : Fragment(R.layout.fragment_signup_teacher2) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mainActivityView = (activity as TeacherSignUpActivity)
        btnSignUp.setOnClickListener{
            if (mainActivityView.validateEmail() && mainActivityView.validatePassword()) {
                mainActivityView.registerUser()
            }
        }
    }
}