package com.mact.proxyproof.fragments

import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.Window
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

import com.mact.proxyproof.R
import com.mact.proxyproof.SignUpActivity
import com.regula.facesdk.FaceSDK
import com.regula.facesdk.configuration.LivenessConfiguration
import com.regula.facesdk.enums.ImageType
import com.regula.facesdk.enums.LivenessStatus
import com.regula.facesdk.model.results.LivenessResponse
import kotlinx.android.synthetic.main.fragment_signup3.*
import kotlinx.android.synthetic.main.fragment_signup4.*
import kotlinx.android.synthetic.main.tutorial_facecapture.*


class SignUpFragment4 : Fragment(R.layout.fragment_signup4) {
    private lateinit var imageView1: ImageView
//    private lateinit var buttonLiveness: Button
//    private lateinit var buttonFinish: Button
//    private lateinit var tvMessage: TextView
//    private lateinit var pbProcessing: ProgressBar
//    private lateinit var tvProcessing: TextView
    private var imageUri: Uri? = null
    private var scholar : String? = null
    private var userName : String? = null
    private var storageReference: StorageReference? = null
    private lateinit var database : DatabaseReference
    private lateinit var user : FirebaseAuth
    lateinit var mainActivityView : SignUpActivity
    var liveResponse: LivenessResponse? = null
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mainActivityView = (activity as SignUpActivity)
        userName = emailToUserName(mainActivityView.etEmail.text.toString())
        val dialog = Dialog(mainActivityView, R.style.AppTheme)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(true)
        dialog.setContentView(R.layout.tutorial_facecapture)
//        dialog.setOnCancelListener()
        dialog.show()
//        user = FirebaseAuth.getInstance()
//        storageReference = FirebaseStorage.getInstance().getReference("StudentFaceID/")
//        if (user.currentUser != null) {
//            user.currentUser?.let { it ->
//                userName = emailToUserName(user.currentUser?.email.toString())
//                val url = getString(R.string.firebase_db_location)
//                database = FirebaseDatabase.getInstance(url).getReference("users")
//                database.child(userName!!).get().addOnSuccessListener {
//                    if (it.exists()) {
//                        scholar = it.child("schNum").value.toString()
//                        storageReference =
//                            FirebaseStorage.getInstance().getReference("StudentFaceID/$scholar")
//                    }
//                }
//            }
//        }
//        scholar = mainActivityView.etScholar.text.toString()
        scholar = mainActivityView.getScholar()
        storageReference =
            FirebaseStorage.getInstance().getReference("StudentFaceID/$userName$scholar")
        imageView1 = ivFaceData
//        imageView1.layoutParams.height = 120

//        imageView2 = findViewById(R.id.imageView2)
//        imageView2.layoutParams.height = 400

//        buttonLiveness = buttonLiveness
//        buttonFinish = btnFinish
//        tvMessage = tvMessage
//        pbProcessing = pbProcessing
//        tvProcessing = tvProcessing

        val faceCapture = dialog.btnToFaceCapture
        faceCapture.setOnClickListener{
            dialog.hide()
            startLiveness()

        }
        buttonLiveness.setOnClickListener {
            startLiveness()
        }
        btnFinish.setOnClickListener {
            mainActivityView.registerUser()
            tvProcessing.visibility = View.VISIBLE
            pbProcessing.visibility = View.VISIBLE
            btnFinish.isActivated = false
//            Intent(this, LoginActivity::class.java).also {
//                startActivity(it)
//                overridePendingTransition(R.anim.fadein_animation, R.anim.fadeout_animation)
//                finish()
//            }
        }

    }
    private fun emailToUserName(email : String ): String{
        var userName= email
        val regex = Regex("[^A-Za-z0-9]")
        userName = regex.replace(userName, "")
        return userName
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != AppCompatActivity.RESULT_OK)
            return
        if (data != null) {
            imageUri = data.data
        }
        var imageView: ImageView? = null
        if (requestCode == PICK_IMAGE_1)
            imageView = imageView1;
        else if (requestCode == PICK_IMAGE_2)
            imageView?.setImageURI(imageUri)
        imageView?.tag = ImageType.PRINTED
    }

    private fun startLiveness() {
        val configuration = LivenessConfiguration.Builder().setCameraSwitchEnabled(true).build()
        FaceSDK.Instance().startLiveness(mainActivityView, configuration) { livenessResponse: LivenessResponse ->
            if (livenessResponse.bitmap != null) {
                imageView1.setImageBitmap(livenessResponse.bitmap)
                imageView1.tag = ImageType.LIVE
                if (livenessResponse.liveness == LivenessStatus.PASSED) {
                    mainActivityView.getLiveResponse(livenessResponse)
                    buttonLiveness.visibility = View.VISIBLE
                    btnFinish.visibility =  View.VISIBLE
                    btnFinish.isActivated =  true
                    tvInfo.text = ""
                    tvProcessing.visibility = View.INVISIBLE
                    pbProcessing.visibility = View.INVISIBLE
                } else {
                    tvProcessing.visibility = View.INVISIBLE
                    pbProcessing.visibility = View.INVISIBLE
                    tvInfo.text = "Recapture your Face and \n Try to keep it straight"
                    btnFinish.isActivated =  false
                }
            } else {
                tvProcessing.visibility = View.INVISIBLE
                pbProcessing.visibility = View.INVISIBLE
                tvInfo.text = "Recapture your Face"
                btnFinish.isActivated =  false
            }

//            textViewSimilarity.text = getString(R.string.similarity_null)
        }
    }



    companion object {
        private const val PICK_IMAGE_1 = 1
        private const val PICK_IMAGE_2 = 2
    }
}
