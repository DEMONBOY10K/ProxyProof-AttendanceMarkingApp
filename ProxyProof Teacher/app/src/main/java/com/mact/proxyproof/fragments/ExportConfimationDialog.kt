package com.mact.proxyproof.fragments

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.DialogFragment
import com.mact.proxyproof.R
import kotlinx.android.synthetic.main.fragment_export_confirm_dialog.*


class ExportConfirmationDialog : DialogFragment(R.layout.fragment_export_confirm_dialog) {

    // declare variables to store data
    private var depart: String? = null
    private var sem: String? = null
    private var sec: String? = null
    private var sub: String? = null
    private var timeslot: String? = null

    // companion object to define a newInstance() method for the dialog
    companion object {
        fun newInstance(depart: String, sem: String, sec: String, sub: String, timeslot: String): ExportConfirmationDialog {
            val args = Bundle().apply {
                putString("depart", depart)
                putString("sem", sem)
                putString("sec", sec)
                putString("sub", sub)
                putString("timeslot", timeslot)
            }
            return ExportConfirmationDialog().apply { arguments = args }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // retrieve the data from the arguments
        arguments?.let {
            depart = it.getString("depart")
            sem = it.getString("sem")
            sec = it.getString("sec")
            sub = it.getString("sub")
            timeslot = it.getString("timeslot")
        }

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d("","Department : $depart\n Semester : $sem\n Section: $sec \n Subject: $sub \n TimeSlot: $timeslot ")
        tvConfirmData.text = " Department : $depart\n Semester : $sem\n Section: $sec \n Subject: $sub \n TimeSlot: $timeslot "
        btnCloseFrag.setOnClickListener {
            dismiss()
        }
        btnConfirmExport.setOnClickListener {

        }
    }

    // rest of the class implementation...
}
