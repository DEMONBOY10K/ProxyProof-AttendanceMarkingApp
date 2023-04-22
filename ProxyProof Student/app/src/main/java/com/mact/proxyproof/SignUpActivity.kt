package com.mact.proxyproof

import android.app.Activity
import android.app.Dialog
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.view.Window
import android.view.animation.AnimationUtils
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import com.airbnb.lottie.LottieAnimationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.mact.proxyproof.dataclass.Students
import com.mact.proxyproof.fragments.SignUpFragment1
import kotlinx.android.synthetic.main.fragment_signup1.*
import kotlinx.android.synthetic.main.fragment_signup2.*
import kotlinx.android.synthetic.main.fragment_signup3.*
import kotlinx.android.synthetic.main.fragment_signup4.*
import java.io.ByteArrayOutputStream
import java.util.*
import com.regula.facesdk.model.results.LivenessResponse

class SignUpActivity : AppCompatActivity() {
    private lateinit var database : DatabaseReference
    private lateinit var userDataDatabase : DatabaseReference
    private lateinit var statsDatabase : DatabaseReference
    private lateinit var datesDatabase : DatabaseReference
    private var storageReference: StorageReference? = null
    private lateinit var user : FirebaseAuth
    private var backPressedTime:Long = 0
    lateinit var backToast:Toast
    private val radioGroup: RadioGroup? = null
    private var radioButton: RadioButton? = null
    var lavSignUp : LottieAnimationView? = null
    var tvbtnToLogin : TextView? = null
    var constraintLayoutSignup : ConstraintLayout? = null
    var liveResponse: LivenessResponse? = null
//    var etEmail : EditText? = null
//    var etPassword : EditText? = null
//    var etFName : EditText? = null
//    var etLName : EditText? = null
//    var etAge : EditText? = null
//    var etScholar : EditText? = null
//    var etSemester : EditText? = null
//    var tiFName : TextInputLayout? = null
//    var tiLName : TextInputLayout? = null
//    var tiEmail : TextInputLayout? = null
//    var tiPassword : TextInputLayout? = null
//    var tiAge : TextInputLayout? = null
//    var tiScholar : TextInputLayout? = null
//    var tiSemester : TextInputLayout? = null
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
        setContentView(R.layout.activity_signup2)
        val fragment1 = SignUpFragment1()
        replaceFragment(fragment1)
        lavSignUp = findViewById(R.id.lavSignUp)
        tvbtnToLogin = findViewById(R.id.tvbtnToLogin)
        constraintLayoutSignup = findViewById(R.id.constraintLayoutSignup)

//        etEmail = findViewById(R.id.etEmail)
//        etPassword = findViewById(R.id.etPassword)
//        etFName = findViewById(R.id.etFName)
//        etLName = findViewById(R.id.etLName)
//        etAge = findViewById(R.id.etAge)
//        etScholar = findViewById(R.id.etScholar)
//        tiFName = findViewById(R.id.tiFName)
//        tiLName = findViewById(R.id.tiLName)
//        tiEmail = findViewById(R.id.tiEmail)
//        tiPassword = findViewById(R.id.tiPassword)
//        tiAge = findViewById(R.id.tiAge)
//        tiScholar = findViewById(R.id.tiScholar)
//        tiSemester = findViewById(R.id.tiSemester)

        val slideRightAnimation = AnimationUtils.loadAnimation(this, R.anim.slide_left_to_center_animation)
        val fadeInAnimation = AnimationUtils.loadAnimation(this, R.anim.alphafadein700_animation)
        lavSignUp?.startAnimation(slideRightAnimation)
        constraintLayoutSignup?.startAnimation(fadeInAnimation)
        user = FirebaseAuth.getInstance()
//        btnToDashboard.setOnClickListener {
//
//            if (validateFName() && validateLName() && validateEmail() && validateAge() && validateWeight() && validateHeight() && validatePassword()) {
//                registerUser()
//
//            }
//        }
        tvbtnToLogin?.setOnClickListener {
            val slideRightAnimation = AnimationUtils.loadAnimation(this, R.anim.slide_center_to_right_animation)
            val fadeOutAnimation = AnimationUtils.loadAnimation(this, R.anim.alphafadeout700_animation)
            lavSignUp?.startAnimation(slideRightAnimation)
            constraintLayoutSignup?.startAnimation(fadeOutAnimation)
            Intent(this, LoginActivity::class.java).also {
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
        val email = etEmail?.text.toString()
        val password = etPassword?.text.toString()

        user.createUserWithEmailAndPassword(email,password)
            .addOnCompleteListener(SignUpActivity()){ task->
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
            }.addOnFailureListener (SignUpActivity() ){
                Toast.makeText(
                    this,
                    it.message,
                    Toast.LENGTH_LONG
                ).show()
                btnFinish.isActivated = true
                tvProcessing.visibility = View.INVISIBLE
                pbProcessing.visibility = View.INVISIBLE
            }
    }
    private fun beginRegistration(){
        val url = getString(R.string.firebase_db_location)
        val firstName = etFName?.text.toString().replaceFirstChar {
            if (it.isLowerCase()) it.titlecase(Locale.getDefault())
            else it.toString()
        }
        val lastName =etLName?.text.toString().replaceFirstChar {
            if (it.isLowerCase()) it.titlecase(Locale.getDefault())
            else it.toString()
        }
        val name = "$firstName $lastName"
        val email = etEmail?.text.toString()
        val age = etAge?.text.toString()
        val scholar = etScholar?.text.toString()
        val semester = etSemester?.text.toString().toInt()
        val userName = emailToUserName(email)
        val password = etPassword?.text.toString()
        storageReference = FirebaseStorage.getInstance().getReference("StudentFaceID/$userName$scholar")
//        val checkedGenderRadioButtonId = rgGender.checkedRadioButtonId
//        val gender = findViewById<RadioButton>(checkedGenderRadioButtonId).text.toString()

//        val selectedId = radioGroup!!.checkedRadioButtonId
//        radioButton = (radioButton)?.findViewById((selectedId));
//        val gender = radioButton?.text.toString();
        val currentDate = getDate()
        user.currentUser?.sendEmailVerification()
            ?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "Email sent Successfully to "+ user.currentUser?.email.toString())
                    database = FirebaseDatabase.getInstance(url).getReference("students")
                    val students = Students(firstName, lastName, email, scholar,age,semester, password)
                    database.child(userName).setValue(students).addOnSuccessListener {

                        uploadFace()
                        etFName?.text?.clear()
                        etLName?.text?.clear()
                        etEmail?.text?.clear()
                        etAge?.text?.clear()
                        etSemester?.text?.clear()
                        etScholar?.text?.clear()
                        etPassword?.text?.clear()
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
                            Intent(this, LoginActivity::class.java).also {
                                startActivity(it)
                                overridePendingTransition(R.anim.fadein_animation, R.anim.fadeout_animation)
                                finish()
                            }
                        }, 1500) // 1500 is the delayed time in milliseconds.
                    }.addOnFailureListener {
                        Log.d("SignUp", "Failed TO Signup")
                        btnFinish.isActivated = true
                        Toast.makeText(applicationContext, "Failed", Toast.LENGTH_SHORT).show()
                    }
                }
            }

    }
    fun getLiveResponse(liveResponse1 : LivenessResponse){
        liveResponse = liveResponse1
    }
    fun uploadFace(){
        val uri: Uri = getImageUri(this, liveResponse?.bitmap!!)
        storageReference?.putFile(uri)?.addOnSuccessListener {
            Log.d("Upload", "Successful")
//                        tvMessage.visibility = View.VISIBLE
            tvProcessing?.visibility = View.INVISIBLE
            pbProcessing?.visibility = View.INVISIBLE
        }?.addOnFailureListener{
            Log.d("Upload", "Failed")
        }
    }
    private fun getImageUri(inContext: Context, inImage: Bitmap): Uri {
        val bytes = ByteArrayOutputStream()
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
        val path = MediaStore.Images.Media.insertImage(
            inContext.getContentResolver(),
            inImage,
            "Title",
            null
        )
        return Uri.parse(path)
    }
    private fun getDate() : String{
        val c =Calendar.getInstance()
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
        Log.d(TAG, "validateFname")
        val first = etFName?.text.toString().trim()
        if (first.isEmpty()){
            tiFName?.error = "Enter Your First Name"
           return false
        }
        else{
            tiFName?.isErrorEnabled = false
            tiFName?.error=null
            return true
        }

    }
    fun validateLName() : Boolean{
        val last = etLName?.text.toString().trim()
        if (last.isEmpty()){
            tiLName?.error = "Enter Your Last Name"
            return false
        }
        else{
            tiLName?.isErrorEnabled = false
            tiLName?.error = null
            return true
        }
    }
    fun validateAge() : Boolean{
        val age = etAge?.text.toString().trim()
//        val dobRegex = "^(3[01]|[12][0-9]|0[1-9])/(1[0-2]|0[1-9])/[0-9]{4}\$".toRegex()

        if (age.isEmpty()){
            tiAge?.error = "Enter Your Age"
            return false
        }else if(age.toInt()>0){
            if(age.toInt()>60 || age.toInt()<15){
                tiAge?.error = "You aren't eligible to use this app"
                return false
            }else{
                tiAge?.isErrorEnabled = false
                tiAge?.error=null
                return true
            }
        }
        tiAge?.error = "Enter your Age correctly"
       return false
    }
    fun validateEmail() : Boolean{
        val email = etEmail?.text.toString().trim()
        val emailRegex : Regex = "^[a-zA-Z0-9+_.-]+@[a-zA-Z0-9.-]+$".toRegex()
        if (email.isEmpty()){
            tiEmail?.error = "Enter Your Email"
            return false
        }
        else if(!email.matches(emailRegex)){
            tiEmail?.error = "Invalid Email Address"
            return false
        }
        else
        {
            tiEmail?.isErrorEnabled = false
            tiEmail?.error=null
            return true
        }
    }
    fun validatePassword() : Boolean{
        val password = etPassword?.text.toString().trim()
        val passwordRegex : Regex = "^(?=.*?[A-Z])(?=.*?[a-z])(?=.*?[0-9]).{8,16}\$".toRegex()
//        Regex Conditions:
//        Min 1 uppercase letter.
//        Min 1 lowercase letter.
//        Min 1 special character.
//        Min 1 number.
//        Min 8 characters.
//        Max 30 characters.

        if (password.isEmpty()){
            tiPassword?.error = "Enter Your Password"
            return false
        }
        else if(!password.matches(passwordRegex)) {
            tiPassword?.error = "Must be 8-16 characters Long, Containing an UpperCase, Lowercase and Number"
            return false
        }
        else{
            tiPassword?.isErrorEnabled = false
            tiPassword?.error = null
            return true
        }
    }
    fun validateScholar() : Boolean{
        val emojiCode = "1F605" //Enter Code without u prefix
        val scholar = etScholar?.text.toString().trim()
        val regex  = Regex("^[0-9]{9}$")
        val regex2 = Regex("^[0-9]{2}[A-Z][0-9]{3}$")
        val regex3 = Regex("^[0-9]{5}[A-Z][0-9]{3}$")
        if (scholar.isEmpty()){
            tiScholar?.error = "Enter Your Scholar Number"
            return false
        }
        else if(!regex.matches(scholar)) {
            if(regex2.matches(scholar)){
                etScholar.setText("000$scholar")
                tiScholar.hint = "Scholar Number (0's added for Roll.No)"
                validateScholar()
            }
            else if(regex3.matches(scholar)){
                tiScholar?.isErrorEnabled = false
                tiScholar?.error = null
                return true
            }else{
                tiScholar?.error = "Pls Enter Your Scholar Number Correctly "
                return false
            }
        }
        else{
            tiScholar?.isErrorEnabled = false
            tiScholar?.error = null
            return true
        }
        return true
    }
    fun validateSemester() : Boolean{
        val emojiCode = "1F64B" //Enter Code without u prefix
        val semester = etSemester?.text.toString().trim()
        val regex = Regex("^[0-9]$")
        if (semester.isEmpty()){
            tiSemester?.error = "Enter your Semester Number"
            return false
        }else if(semester.toInt()>10)
        {
            tiSemester?.error = "Pls Enter Your Semester Number Correctly "
            return false
        }
        else{
            tiSemester?.isErrorEnabled = false
            tiSemester?.error=null
            return true
        }
    }
    private fun getEmojiByUnicode(reactionCode: String): String {
        val code = reactionCode.toInt(16)
        return String(Character.toChars(code))
    }
    private fun emailToUserName(email : String ): String{
//        var count = 0
//        for (i in email){
//            if(i=='@'){
//                break
//            }
//            count++
//        }
//        var userName= email.slice(0 until count)
        var userName = email
        val regex = Regex("[^A-Za-z0-9]")
        userName = regex.replace(userName, "")
        return userName
    }
    fun getScholar():String{
        return etScholar?.text.toString()
    }
}

