package com.mact.proxyproof.fragments

import android.os.Bundle
import android.view.View
import androidx.fragment.app.DialogFragment
import com.mact.proxyproof.R
import kotlinx.android.synthetic.main.fragment_instruction.*

class InstructionsDialog : DialogFragment(R.layout.fragment_instruction) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        btnCloseFrag.setOnClickListener{
            dismiss()
        }
    }
}