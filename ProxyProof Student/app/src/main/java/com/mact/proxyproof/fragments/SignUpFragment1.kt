package com.mact.proxyproof.fragments

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.mact.proxyproof.R
import com.mact.proxyproof.SignUpActivity

import kotlinx.android.synthetic.main.fragment_signup1.*

class SignUpFragment1 : Fragment(R.layout.fragment_signup1) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mainActivityView = (activity as SignUpActivity)
        val fragment2 = SignUpFragment2()
        btnNext2.setOnClickListener{
            if (mainActivityView.validateFName() && mainActivityView.validateLName()
                && mainActivityView.validateAge()) {
                mainActivityView.replaceFragment(fragment2)
            }
        }
    }
}