package com.mact.proxyproof.fragments

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.mact.proxyproof.R
import com.mact.proxyproof.SignUpActivity
import kotlinx.android.synthetic.main.fragment_signup2.*

class SignUpFragment2 : Fragment(R.layout.fragment_signup2) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        var mainActivityView = (activity as SignUpActivity)
        val fragment3 = SignUpFragment3()

        btnToDashboard.setOnClickListener {
            if (mainActivityView.validateScholar() && mainActivityView.validateSemester()) {
                mainActivityView.replaceFragment(fragment3)

            }
        }
    }
}