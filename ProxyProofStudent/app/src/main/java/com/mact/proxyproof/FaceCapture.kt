package com.mact.proxyproof


import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.view.Window
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.regula.facesdk.FaceSDK
import com.regula.facesdk.configuration.LivenessConfiguration
import com.regula.facesdk.enums.ImageType
import com.regula.facesdk.enums.LivenessStatus
import com.regula.facesdk.model.results.LivenessResponse
import kotlinx.android.synthetic.main.tutorial_facecapture.*
import java.io.ByteArrayOutputStream


class FaceCapture : AppCompatActivity() {
    private lateinit var imageView1: ImageView
    private lateinit var buttonLiveness: Button
    private lateinit var buttonFinish: Button
    private lateinit var tvMessage: TextView
    private lateinit var pbProcessing: ProgressBar
    private lateinit var tvProcessing: TextView
    private var imageUri: Uri? = null
    private var scholar : String? = null
    private var userName : String? = null
    private var storageReference: StorageReference? = null
    private lateinit var database : DatabaseReference
    private lateinit var user : FirebaseAuth
    override fun onBackPressed() {
    }
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_facecapture)
        val dialog = Dialog(this, R.style.AppTheme)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(true)
        dialog.setContentView(R.layout.tutorial_facecapture)
//        dialog.setOnCancelListener()
        dialog.show()
        user = FirebaseAuth.getInstance()
        storageReference = FirebaseStorage.getInstance().getReference("StudentFaceID/")
        if (user.currentUser != null) {
            user.currentUser?.let { it ->
                    userName = emailToUserName(user.currentUser?.email.toString())
                    val url = getString(R.string.firebase_db_location)
                    database = FirebaseDatabase.getInstance(url).getReference("users")
                    database.child(userName!!).get().addOnSuccessListener {
                        if (it.exists()) {
                            scholar = it.child("schNum").value.toString()
                            storageReference =
                                FirebaseStorage.getInstance().getReference("StudentFaceID/$scholar")
                        }
                    }
            }
        }
        imageView1 = findViewById(R.id.ivFacePreview)
        imageView1.layoutParams.height = 400

//        imageView2 = findViewById(R.id.imageView2)
//        imageView2.layoutParams.height = 400

        buttonLiveness = findViewById(R.id.buttonLiveness)
        buttonFinish = findViewById(R.id.btnFinish)
        tvMessage = findViewById(R.id.tvMessage)
        pbProcessing = findViewById(R.id.pbProcessing)
        tvProcessing = findViewById(R.id.tvProcessing)

        val faceCapture = dialog.btnToFaceCapture
        faceCapture.setOnClickListener{
            dialog.hide()
            startLiveness()
        }
        buttonLiveness.setOnClickListener {
            startLiveness()
        }
        buttonFinish.setOnClickListener {
            Intent(this, LoginActivity::class.java).also {
                startActivity(it)
                overridePendingTransition(R.anim.fadein_animation, R.anim.fadeout_animation)
                finish()
            }
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
        if (resultCode != RESULT_OK)
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
        FaceSDK.Instance().startLiveness(this, configuration) { livenessResponse: LivenessResponse ->
            if (livenessResponse.bitmap != null) {
                imageView1.setImageBitmap(livenessResponse.bitmap)
                imageView1.tag = ImageType.LIVE
                if (livenessResponse.liveness == LivenessStatus.PASSED) {
                    val uri:Uri = getImageUri(this, livenessResponse.bitmap!!)
                    storageReference?.putFile(uri)?.addOnSuccessListener {
                        Log.d("Upload", "Successful")
                    buttonLiveness.visibility = View.VISIBLE
                        buttonFinish.visibility =  View.VISIBLE
                    tvMessage.visibility = View.VISIBLE
                    tvProcessing.visibility = View.INVISIBLE
                    pbProcessing.visibility = View.INVISIBLE
                    }?.addOnFailureListener{
                        Log.d("Upload", "Failed")
                    }
                } else {
//                    textViewLiveness.text = getString(R.string.liveness_unknown)
                }
            } else {
//                textViewLiveness.text = getString(R.string.liveness_null)
            }

//            textViewSimilarity.text = getString(R.string.similarity_null)
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

    companion object {
        private const val PICK_IMAGE_1 = 1
        private const val PICK_IMAGE_2 = 2
    }
}
