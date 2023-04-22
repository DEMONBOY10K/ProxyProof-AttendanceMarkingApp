package com.mact.proxyproof

import android.Manifest
import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.view.Window
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.mact.proxyproof.fragments.TeacherResetPasswordDialog
import com.mact.proxyproof.receiver.FileReceiverActivity
//import kotlinx.android.synthetic.main.activity_login2.*
import kotlinx.android.synthetic.main.activity_login_teacher.*
import kotlinx.android.synthetic.main.activity_login_teacher.btnLogIn
import kotlinx.android.synthetic.main.activity_login_teacher.constraintLayoutLogin
import kotlinx.android.synthetic.main.activity_login_teacher.etEmail
import kotlinx.android.synthetic.main.activity_login_teacher.etPassword
import kotlinx.android.synthetic.main.activity_login_teacher.lavLogin
import kotlinx.android.synthetic.main.activity_login_teacher.pbLoading
import kotlinx.android.synthetic.main.activity_login_teacher.tiEmail
import kotlinx.android.synthetic.main.activity_login_teacher.tiPassword
import kotlinx.android.synthetic.main.activity_login_teacher.tvAlert
import kotlinx.android.synthetic.main.activity_login_teacher.tvForgotPassword
import kotlinx.android.synthetic.main.activity_login_teacher.tvbtnToSignUp

import java.io.OutputStreamWriter

class TeacherLoginActivity: AppCompatActivity() {
    private lateinit var database : DatabaseReference
    private lateinit var user : FirebaseAuth
    private var backPressedTime:Long = 0
    lateinit var backToast: Toast
//    private var scholar: String? = null
//    private var imei: String? = null
    private val PHONE_STATE_PERMISSION_CODE = 102
    private var userName : String? =null;
    override fun onBackPressed(){
        backToast = Toast.makeText(this, "Press back again to Exit.", Toast.LENGTH_LONG)
        if (backPressedTime + 2000 > System.currentTimeMillis()) {
            backToast.cancel()
            super.onBackPressed()
            return
        } else {
            backToast.show()
        }
        backPressedTime = System.currentTimeMillis()
    }
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login_teacher)
        val slideRightAnimation = AnimationUtils.loadAnimation(this, R.anim.slide_left_to_center_animation)
        val fadeInAnimation = AnimationUtils.loadAnimation(this, R.anim.alphafadein700_animation)
        lavLogin.startAnimation(slideRightAnimation)
        constraintLayoutLogin.startAnimation(fadeInAnimation)
        user = FirebaseAuth.getInstance()
        if(user.currentUser!= null){
            user.currentUser?.let {
                if(user.currentUser?.isEmailVerified == true){
                    Log.d("currentUserAtLogin",it.email.toString()+" has logged in")
                    Intent(this,FileReceiverActivity::class.java).also { newIt->
                        startActivity(newIt)
                        overridePendingTransition(R.anim.fadein_animation, R.anim.fadeout_animation)
                        finish()
                    }
                }else{

                }
            }
        }

        tvbtnToSignUp.setOnClickListener{
            val slideRightAnimation = AnimationUtils.loadAnimation(this, R.anim.slide_center_to_right_animation)
            val fadeOutAnimation = AnimationUtils.loadAnimation(this, R.anim.alphafadeout700_animation)
            lavLogin.startAnimation(slideRightAnimation)
            constraintLayoutLogin.startAnimation(fadeOutAnimation)
            Intent(this, TeacherSignUpActivity::class.java).also {
//                val pair1 = UtilPair.create<View,String>(tiEmail,"tiFirst")
//                var option = ActivityOptions.makeSceneTransitionAnimation(this,pair1)
//                startActivity(it,option.toBundle())
                startActivity(it)
                overridePendingTransition(R.anim.fadein_animation, R.anim.fadeout_animation)
                finish()
            }
        }
        btnLogIn.setOnClickListener {
            if(validateEmail()&&validatePassword()){
                loginUser()
            }
        }
        tvForgotPassword.setOnClickListener{

            val resetDialog = TeacherResetPasswordDialog()
            resetDialog.show(supportFragmentManager,"customDialog")
        }
    }

    private fun showDialog(activity: Activity?, msg: String?) {
        val dialog = Dialog(activity!!, R.style.AppTheme)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.loadingscreen)
        dialog.show()
    }
    @RequiresApi(Build.VERSION_CODES.O)
    private fun loginUser () {
        btnLogIn.visibility=View.INVISIBLE
        pbLoading.visibility=View.VISIBLE
        val email = etEmail.text.toString()
        val password = etPassword.text.toString()
        user.signInWithEmailAndPassword(email,password)
            .addOnCompleteListener { task ->
                if(task.isSuccessful){
                    beginLogIn()
                } else{
                    btnLogIn.visibility=View.VISIBLE
                    pbLoading.visibility=View.INVISIBLE
                    Toast.makeText(
                        this,
                        task.exception!!.message,
                        Toast.LENGTH_LONG
                    ).show()
                }
            }

    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun beginLogIn() {
        Handler().postDelayed({
            if(user.currentUser?.isEmailVerified == true){

                tvAlert.text = null
                Log.d("currentUserAtLogin", user.currentUser?.email.toString()+" has logged in")
                val url = getString(R.string.firebase_db_location)
                val email = etEmail.text.toString()
                val password = etPassword.text.toString()
                userName = emailToUserName(email)
                database = FirebaseDatabase.getInstance(url).getReference("teachers")
                database.child(userName!!).get().addOnSuccessListener {
                    if(it.exists()){
                        showDialog(this,"Login Successful")
                        val userEmail = it.child("email").value
                        val userPass = it.child("password").value
                        val userFName = it.child("fName").value.toString()
                        Log.d("user",user.currentUser.toString())
                        if(userEmail==email
//                            &&userPass==password
                        ){
//                            scholar = it.child("schNum").value.toString()
//                            imei = getIMEIDeviceId(this)
                            createTxt()
                            Intent(this,FileReceiverActivity::class.java).also { newIt->
                                startActivity(newIt)
                                overridePendingTransition(R.anim.fadein_animation, R.anim.fadeout_animation)
                                finish()
                            }
                        }else{
                            btnLogIn.visibility=View.VISIBLE
                            pbLoading.visibility=View.INVISIBLE
                            Toast.makeText(applicationContext, "Wrong Credentials", Toast.LENGTH_SHORT).show()
                            user.signOut()
                        }
                    }else{
                        btnLogIn.visibility=View.VISIBLE
                        pbLoading.visibility=View.INVISIBLE
                        Toast.makeText(applicationContext, "User Doesn't Exist", Toast.LENGTH_SHORT).show()
                        user.signOut()
                    }
                }.addOnFailureListener {
                    btnLogIn.visibility=View.VISIBLE
                    pbLoading.visibility=View.INVISIBLE
                    Toast.makeText(applicationContext, "Failed", Toast.LENGTH_SHORT).show()
                }
            }
            else{
                btnLogIn.visibility= View.VISIBLE
                pbLoading.visibility= View.INVISIBLE
                val alert = "Email Verification Pending!"
                tvAlert.text = alert
                Log.d("currentUserAtLogin", user.currentUser?.email.toString()+" has not verified his email address")
            }
        }, 1500) // 1500 is the delayed time in milliseconds.
    }

    fun createTxt() {
        // add-write text into file
        try {
            val fileIdentity = openFileOutput("identity.txt", MODE_PRIVATE)
            ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.READ_PHONE_STATE),
                PHONE_STATE_PERMISSION_CODE
            )
//            val telephonyManager = getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
//            imei = telephonyManager.deviceId
            val outputWriterIdentity = OutputStreamWriter(fileIdentity)
            outputWriterIdentity.write("Teacher")
            outputWriterIdentity.close()
            //display file saved message
            Toast.makeText(
                baseContext, "File saved successfully!",
                Toast.LENGTH_SHORT
            ).show()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    private fun validateEmail() : Boolean{
        val email  = etEmail.text.toString().trim()
        val emailRegex : Regex = "^[a-zA-Z0-9+_.-]+@[a-zA-Z0-9.-]+$".toRegex()
        if (email.isEmpty()){
            tiEmail.error = "Enter Your Email"
            return false
        }
        else if(!email.matches(emailRegex)){
            tiEmail.error = "Invalid Email Address"
            return false
        }
        else
        {
            tiEmail.isErrorEnabled = false
            tiEmail.error = null
            return true
        }
    }
    private fun validatePassword() : Boolean{
        val password = etPassword.text.toString().trim()

        if (password.isEmpty()){
            tiPassword.error = "Enter Your Password"
            return false
        }
        else{
            tiPassword.isErrorEnabled = false
            tiPassword.error = null
            return true
        }
    }
    fun emailToUserName(email : String ): String{
        var userName= email
        val regex = Regex("[^A-Za-z0-9]")
        userName = regex.replace(userName, "")
        return userName
    }

//    @RequiresApi(Build.VERSION_CODES.O)
//    fun createTxt() {
//        // add-write text into file
//        try {
//            val fileout = openFileOutput("$userName.txt", MODE_PRIVATE)
//            ActivityCompat.requestPermissions(this,
//                arrayOf(Manifest.permission.READ_PHONE_STATE),
//                PHONE_STATE_PERMISSION_CODE
//            )
////            val telephonyManager = getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
////            imei = telephonyManager.deviceId
//            Log.d("current1", "$imei")
//            val outputWriter = OutputStreamWriter(fileout)
//            outputWriter.write(scholar)
//            outputWriter.close()
//
//            //display file saved message
//            Toast.makeText(
//                baseContext, "File saved successfully!",
//                Toast.LENGTH_SHORT
//            ).show()
//        } catch (e: Exception) {
//            e.printStackTrace()
//        }
//    }
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String?>,
                                            grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            PHONE_STATE_PERMISSION_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] ==
                    PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Permission granted.", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Permission denied.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

//    fun getIMEIDeviceId(context: Context): String? {
//        val deviceId: String
//        deviceId = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
//            Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
//        } else {
//            val mTelephony = context.getSystemService(TELEPHONY_SERVICE) as TelephonyManager
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//                if (context.checkSelfPermission(Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
//                    return ""
//                }
//            }
//            assert(mTelephony != null)
//            if (mTelephony.deviceId != null) {
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                    mTelephony.imei
//                } else {
//                    mTelephony.deviceId
//                }
//            } else {
//                Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
//            }
//        }
//        Log.d("deviceId", deviceId)
//        return deviceId
//    }

}