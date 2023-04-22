package com.mact.proxyproof

import android.app.Activity
import android.app.Dialog
import android.content.ContentValues
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.Window
import android.view.animation.AnimationUtils
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.mact.proxyproof.dataclass.Teachers
import com.mact.proxyproof.fragments.TeacherSignupFragment1
import kotlinx.android.synthetic.main.activity_signup_teacher.*
import kotlinx.android.synthetic.main.fragment_signup_teacher1.*
import kotlinx.android.synthetic.main.fragment_signup_teacher2.*
import java.util.*

class TeacherSignUpActivity : AppCompatActivity() {
    private lateinit var database : DatabaseReference
    private lateinit var user : FirebaseAuth
    private var backPressedTime:Long = 0
    lateinit var backToast: Toast
    private val radioGroup: RadioGroup? = null
    private var radioButton: RadioButton? = null
    override fun onBackPressed() {
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup_teacher)
        val fragment1 = TeacherSignupFragment1()
        replaceFragment(fragment1)
        val slideRightAnimation = AnimationUtils.loadAnimation(this, R.anim.slide_left_to_center_animation)
        val fadeInAnimation = AnimationUtils.loadAnimation(this, R.anim.alphafadein700_animation)
        lavSignUp.startAnimation(slideRightAnimation)
        constraintLayoutSignup.startAnimation(fadeInAnimation)
        user = FirebaseAuth.getInstance()
//        btnToDashboard.setOnClickListener {
//
//            if (validateFName() && validateLName() && validateEmail() && validateAge() && validateWeight() && validateHeight() && validatePassword()) {
//                registerUser()
//
//            }
//        }
        tvbtnToLogin.setOnClickListener {
            val slideRightAnimation = AnimationUtils.loadAnimation(this, R.anim.slide_center_to_right_animation)
            val fadeOutAnimation = AnimationUtils.loadAnimation(this, R.anim.alphafadeout700_animation)
            lavSignUp.startAnimation(slideRightAnimation)
            constraintLayoutSignup.startAnimation(fadeOutAnimation)
            Intent(this, TeacherLoginActivity::class.java).also {
                startActivity(it)
                overridePendingTransition(R.anim.fadein_animation, R.anim.fadeout_animation)
                finish()
            }
        }


//        rbtnMale.setOnClickListener {
//            ivGender.setImageResource(R.drawable.male)
//        }
//        rbtnFemale.setOnClickListener {
//            ivGender.setImageResource(R.drawable.female)
//        }

    }
    fun replaceFragment(fragment : Fragment){
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.fcvSignUp,fragment)
//                addToBackStack(null)
            commit()
        }
    }
    private fun showDialog(activity: Activity?, msg: String?) {
        val dialog = Dialog(activity!!, R.style.AppTheme)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.loadingscreen)
        dialog.show()
//        val timer = Timer()
//        timer.schedule(object : TimerTask() {
//            override fun run() {
//                dialog.dismiss()
//                timer.cancel()
//            }
//        }, 4000)
    }
    fun registerUser (){
        val email = etEmail.text.toString()
        val password = etPassword.text.toString()

        user.createUserWithEmailAndPassword(email,password)
            .addOnCompleteListener(this){ task->
                if (task.isSuccessful){
                    showDialog(this,"SignUp Successful")
                    beginRegistration()
                }
                else{
                    Toast.makeText(
                        this,
                        task.exception!!.message,
                        Toast.LENGTH_LONG
                    ).show()
                }
            }.addOnFailureListener (this){
                Toast.makeText(
                    this,
                    it.message,
                    Toast.LENGTH_LONG
                ).show()
            }
    }
    private fun beginRegistration(){
        val url = getString(R.string.firebase_db_location)
        val firstName = etFName.text.toString().replaceFirstChar {
            if (it.isLowerCase()) it.titlecase(Locale.getDefault())
            else it.toString()
        }
        val lastName =etLName.text.toString().replaceFirstChar {
            if (it.isLowerCase()) it.titlecase(Locale.getDefault())
            else it.toString()
        }
        val name = "$firstName $lastName"
        val email = etEmail.text.toString()
        val IDNum = etIDNum.text.toString().toInt()
        val userName = emailToUserName(email)
        val password = etPassword.text.toString()

//        val checkedGenderRadioButtonId = rgGender.checkedRadioButtonId
//        val gender = findViewById<RadioButton>(checkedGenderRadioButtonId).text.toString()

//        val selectedId = radioGroup!!.checkedRadioButtonId
//        radioButton = (radioButton)?.findViewById((selectedId));
//        val gender = radioButton?.text.toString();
        val currentDate = getDate()
        user.currentUser?.sendEmailVerification()
            ?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d(ContentValues.TAG, "Email sent Successfully to "+ user.currentUser?.email.toString())
                    database = FirebaseDatabase.getInstance(url).getReference("teachers")
                    val teachers = Teachers(firstName, lastName, email, IDNum, password)
                    database.child(userName).setValue(teachers).addOnSuccessListener {
                        etFName.text?.clear()
                        etLName.text?.clear()
                        etEmail.text?.clear()
                        etIDNum.text?.clear()
                        etPassword.text?.clear()
//                        Log.d(
//                            "MyActivity",
//                            "$firstName $lastName @($email) , $gender" + " born on $dob has height ${height}cm & Weight ${weight}kg , Registered as an User"
//                        )
                        Log.d("userCurrent", "${user.currentUser}")
                        Toast.makeText(
                            this,
                            "$firstName, Verify your Email to Continue",
                            Toast.LENGTH_LONG
                        ).show()
                        Handler().postDelayed({
//                            user.signOut()
                            Intent(this, TeacherLoginActivity::class.java).also {
                                startActivity(it)
                                overridePendingTransition(R.anim.fadein_animation, R.anim.fadeout_animation)
                                finish()
                            }
                        }, 1500) // 1500 is the delayed time in milliseconds.
                    }.addOnFailureListener {
                        Log.d("SignUp", "Failed TO Signup")
                        Toast.makeText(applicationContext, "Failed", Toast.LENGTH_SHORT).show()
                    }
                }
            }

    }
    private fun getDate() : String{
        val c = Calendar.getInstance()
        var day = c.get(Calendar.DAY_OF_MONTH).toString()
        var month =  (c.get(Calendar.MONTH) + 1).toString()
        if(day.length<2){
            day = "0$day"
        }
        if(month.length<2){
            month = "0$month"
        }
        val year = c.get(Calendar.YEAR).toString()
        val date = "$day$month$year"
        return date
    }
    fun validateFName() : Boolean{
        Log.d(ContentValues.TAG, "validateFname")
        val first = etFName.text.toString().trim()
        if (first.isEmpty()){
            tiFName.error = "Enter Your First Name"
            return false
        }
        else{
            tiFName.isErrorEnabled = false
            tiFName.error=null
            return true
        }

    }
    fun validateLName() : Boolean{
        val last = etLName.text.toString().trim()
        if (last.isEmpty()){
            tiLName.error = "Enter Your Last Name"
            return false
        }
        else{
            tiLName.isErrorEnabled = false
            tiLName.error = null
            return true
        }
    }
    fun validateIDNum() : Boolean{
        val idNum = etIDNum.text.toString().trim()
//        val dobRegex = "^(3[01]|[12][0-9]|0[1-9])/(1[0-2]|0[1-9])/[0-9]{4}\$".toRegex()

        if (idNum.isEmpty()){
            tiIDNum.error = "Enter Your ID Number"
            return false
        }
//        else if(idNum.toInt()>0){
//                tiIDNum.isErrorEnabled = false
//                tiIDNum.error=null
//                return true
//        }
//        tiIDNum.error = "Enter your ID Number correctly"
        return true
    }
    fun validateEmail() : Boolean{
        val email = etEmail.text.toString().trim()
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
            tiEmail.error=null
            return true
        }
    }
    fun validatePassword() : Boolean{
        val password = etPassword.text.toString().trim()
        val passwordRegex : Regex = "^(?=.*?[A-Z])(?=.*?[a-z])(?=.*?[0-9]).{8,16}\$".toRegex()
//        Regex Conditions:
//        Min 1 uppercase letter.
//        Min 1 lowercase letter.
//        Min 1 special character.
//        Min 1 number.
//        Min 8 characters.
//        Max 30 characters.

        if (password.isEmpty()){
            tiPassword.error = "Enter Your Password"
            return false
        }
        else if(!password.matches(passwordRegex)) {
            tiPassword.error = "Must be 8-16 characters Long, Containing an UpperCase, Lowercase and Number"
            return false
        }
        else{
            tiPassword.isErrorEnabled = false
            tiPassword.error = null
            return true
        }
    }

    private fun getEmojiByUnicode(reactionCode: String): String {
        val code = reactionCode.toInt(16)
        return String(Character.toChars(code))
    }
    private fun emailToUserName(email : String ): String{
        var userName = email
        val regex = Regex("[^A-Za-z0-9]")
        userName = regex.replace(userName, "")
        return userName
    }
}