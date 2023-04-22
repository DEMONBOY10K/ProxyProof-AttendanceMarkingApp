package com.mact.proxyproof.fragments

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase
import com.mact.proxyproof.R
import com.mact.proxyproof.TeacherLoginActivity
import kotlinx.android.synthetic.main.fragment_resetpassword.*



class TeacherResetPasswordDialog : DialogFragment(R.layout.fragment_resetpassword) {
//  private lateinit var userReset : FirebaseAuth
    private lateinit var database : DatabaseReference
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
//        userReset = FirebaseAuth.getInstance()
        val mainActivityView = (activity as TeacherLoginActivity)
        btnResetPass.setOnClickListener{
            val dialog = EmailSentDialog()
            if(validateEmail()){
                val email = etResetEmail.text.toString().trim()
                val userName = mainActivityView.emailToUserName(email)
                val url = getString(R.string.firebase_db_location)
                database = FirebaseDatabase.getInstance(url).getReference("teachers")
                database.child(userName!!).get().addOnSuccessListener {
                    if(it.exists()){
                        Firebase.auth.sendPasswordResetEmail(email).addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                Toast.makeText(mainActivityView,"Email Sent Successfully",Toast.LENGTH_LONG).show()
                                val alert = "Password reset Mail has been sent to your \\nEmail Address!!"
                                tvResetAlert.text = alert
                                dialog.show(mainActivityView.supportFragmentManager,"customDialogEmailSent")
                                dialog.isCancelable = true
                                dismiss()
                            }
                        }.addOnFailureListener {
                            Log.d("ResetPassword","Failed to Send reset password email $email")

                        }
                    }else{
                        Toast.makeText(mainActivityView, "User Doesn't Exist", Toast.LENGTH_SHORT).show()
                    }
                }.addOnFailureListener {
                    Toast.makeText(mainActivityView, "Failed", Toast.LENGTH_SHORT).show()
                }
//            btnBackToLogin.visibility = View.VISIBLE
//            btnResetPass.visibility = View.INVISIBLE
//            mainActivityView.replaceFragment(fragment)
            }
        }
        btnBackToLogin.setOnClickListener{
            dismiss()
        }
    }
    private fun validateEmail() : Boolean{
        val email  = etResetEmail.text.toString().trim()
        val emailRegex : Regex = "^[a-zA-Z0-9+_.-]+@[a-zA-Z0-9.-]+$".toRegex()
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
//class ResetPasswordFragment:DialogFragment(){
//    override fun onCreateView(
//        inflater: LayoutInflater,
//        container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View? {
//        val rootView : View = inflater.inflate(R.layout.fragment_resetpassword,container,false)
//        return rootView
//    }
//}