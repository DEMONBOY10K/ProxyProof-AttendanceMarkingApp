package com.mact.proxyproof.fragments

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.mact.proxyproof.R
import com.mact.proxyproof.TeacherSignUpActivity
import kotlinx.android.synthetic.main.fragment_signup_teacher1.*


class TeacherSignupFragment1: Fragment(R.layout.fragment_signup_teacher1) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mainActivityView = (activity as TeacherSignUpActivity)
        val fragment2 = TeacherSignUpFragment2()
        btnNext.setOnClickListener{
            if (mainActivityView.validateFName() && mainActivityView.validateLName()
                && mainActivityView.validateIDNum()) {
                mainActivityView.replaceFragment(fragment2)
            }
        }
    }
}