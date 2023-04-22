package com.mact.proxyproof.fragments

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.mact.proxyproof.R
import com.mact.proxyproof.SignUpActivity
import kotlinx.android.synthetic.main.fragment_signup3.*

class SignUpFragment3 : Fragment(R.layout.fragment_signup3) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        var mainActivityView = (activity as SignUpActivity)
        val fragment4 = SignUpFragment4()
        btnNext1.setOnClickListener{
            if (mainActivityView.validateEmail() && mainActivityView.validatePassword()) {
//                mainActivityView.registerUser()
                mainActivityView.replaceFragment(fragment4)

            }
        }
    }
}
