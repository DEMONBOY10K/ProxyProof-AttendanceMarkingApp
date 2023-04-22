package com.mact.proxyproof


import android.app.Dialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
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
import com.mact.proxyproof.sender.FileSenderActivity
import com.regula.facesdk.FaceSDK
import com.regula.facesdk.configuration.LivenessConfiguration
import com.regula.facesdk.enums.ImageType
import com.regula.facesdk.enums.LivenessStatus
import com.regula.facesdk.model.MatchFacesImage
import com.regula.facesdk.model.results.LivenessResponse
import com.regula.facesdk.model.results.matchfaces.MatchFacesResponse
import com.regula.facesdk.model.results.matchfaces.MatchFacesSimilarityThresholdSplit
import com.regula.facesdk.request.MatchFacesRequest
import kotlinx.android.synthetic.main.activity_matchfaces.*
import kotlinx.android.synthetic.main.tutorial.*


class MatchFacesActivity : AppCompatActivity() {
    private lateinit var imageView1: ImageView
    private lateinit var imageView2: ImageView
    private lateinit var pbProcessCapture: ProgressBar
//    private lateinit var buttonMatch: Button
    private lateinit var buttonLiveness: Button
    private lateinit var buttonContinue: Button


    private lateinit var textViewSimilarity: TextView
//    private lateinit var textViewLiveness: TextView

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
        setContentView(R.layout.activity_matchfaces)
        val dialog = Dialog(this, R.style.AppTheme)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(true)
        dialog.setContentView(R.layout.tutorial)
//        dialog.setOnCancelListener()
        dialog.show()
        val handler = Handler(Looper.getMainLooper())
        user = FirebaseAuth.getInstance()
        storageReference = FirebaseStorage.getInstance().getReference("StudentFaceID/")

        if (user.currentUser != null) {
            user.currentUser?.let { it ->
                if (user.currentUser?.isEmailVerified == true) {

                    userName = emailToUserName(user.currentUser?.email.toString())
                    val url = getString(R.string.firebase_db_location)
                    database = FirebaseDatabase.getInstance(url).getReference("students")
                    database.child(userName!!).get().addOnSuccessListener {
                        if (it.exists()) {
                            scholar = it.child("schNum").value.toString()
                            storageReference =
                                FirebaseStorage.getInstance().getReference("StudentFaceID/$scholar")
                        }
                    }
                } else {

                }
            }
        }
        imageView1 = findViewById(R.id.ivFacePreview)
        imageView1.layoutParams.height = 400

        imageView2 = findViewById(R.id.imageView2)
        imageView2.layoutParams.height = 400

//        buttonMatch = findViewById(R.id.buttonMatch)
        buttonLiveness = findViewById(R.id.buttonLiveness)
        buttonContinue = findViewById(R.id.btnContinue)
        pbProcessCapture = findViewById(R.id.pbProcessingCapture)
        textViewSimilarity = findViewById(R.id.textViewSimilarity)
//        textViewLiveness = findViewById(R.id.textViewLiveness)

//        imageView1.setOnClickListener { showMenu(imageView1, PICK_IMAGE_1) }
//        imageView2.setOnClickListener { showMenu(imageView2, PICK_IMAGE_2) }

//        buttonMatch.setOnClickListener {
//            if (imageView1.drawable != null && imageView2.drawable != null) {
//                textViewSimilarity.text = getString(R.string.processing)
//
//                matchFaces(getImageBitmap(imageView1), getImageBitmap(imageView2))
//                buttonMatch.isEnabled = false
//                buttonLiveness.isEnabled = false
//                buttonContinue.isEnabled = false
//
//            } else {
//                Toast.makeText(
//                    this@MatchFacesActivity,
//                    getString(R.string.both_images_compulsory),
//                    Toast.LENGTH_SHORT
//                ).show()
////                pbProcessCapture.visibility = View.INVISIBLE
//            }
//        }

        buttonLiveness.setOnClickListener {
//            startLiveness()
            captureLiveness()
            pbProcessingCapture.visibility = View.VISIBLE
        }

        buttonContinue.setOnClickListener {
//            imageView1.setImageDrawable(null)
//            imageView2.setImageDrawable(null)
//            textViewSimilarity.text = getString(R.string.similarity_null)
//            textViewLiveness.text = getString(R.string.liveness_null)
            if(textViewSimilarity.text.toString().replace("Similarity: ","").replace("%","") != "null"){
                if(textViewSimilarity.text.toString().replace("Similarity: ","").replace("%","").toDouble() > 97.0 ){
                    val handler = Handler(Looper.getMainLooper())
                    handler.postDelayed({ //Do something after 100ms
                    }, 3000)
                    Intent(this,FileSenderActivity::class.java).also { newIt->
                        startActivity(newIt)
                        overridePendingTransition(R.anim.fadein_animation, R.anim.fadeout_animation)
                        finish()
                    }
                }
                else{
                    Toast.makeText(
                        this@MatchFacesActivity,
                        "RECAPTURE Your Image",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }else{
                Toast.makeText(
                    this@MatchFacesActivity,
                    "RECAPTURE Your Image",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
        val countDown = dialog.findViewById(R.id.tvCountdown) as TextView
        val liveness = dialog.btnToLiveness
        liveness.setOnClickListener{
            liveness.visibility = View.INVISIBLE
            dialog.tvToLiveness.visibility = View.VISIBLE
            object : CountDownTimer(4000, 1000) {
                // Callback function, fired on regular interval
                override fun onTick(millisUntilFinished: Long) {
                    var counter =(millisUntilFinished / 1000)
                    when (counter) {
                        3.toLong() -> {countDown.text = "3"}
                        2.toLong() -> {
                            countDown.text = "2"
//                        countDown.textSize = 100F
                        }
                        1.toLong() -> {
                            countDown.text = "1"
                            Log.d("currentUserAtLogin", "hide")
                            val pathReference = storageReference
                            val ONE_MEGABYTE: Long = 1024 * 1024
                            pathReference?.getBytes(ONE_MEGABYTE)?.addOnSuccessListener {
                                val bitmap = BitmapFactory.decodeByteArray(it, 0, it.size)
                                imageView1.setImageBitmap(bitmap)
                                imageView1.tag = ImageType.EXTERNAL
                            }?.addOnFailureListener {
                                // Handle any errors
                            }
                        }
                        0.toLong() -> {
                            captureLiveness()
                            dialog.hide()
                        }
                    }

                }
                // Callback function, fired
                // when the time is up
                override fun onFinish() {
//                    buttonLiveness.performClick()
//                    captureLiveness()
                }
            }.start()
        }

//        btnDownload.setOnClickListener {
//            Log.d("currentUserAtLogin", scholar.toString())
//            val pathReference = storageReference
//            val ONE_MEGABYTE: Long = 1024 * 1024
//            pathReference?.getBytes(ONE_MEGABYTE)?.addOnSuccessListener {
//                // Data for "images/island.jpg" is returned, use this as needed
//                val bitmap = BitmapFactory.decodeByteArray(it, 0, it.size)
//                imageView1.setImageBitmap(bitmap)
//                imageView1.tag = ImageType.EXTERNAL
//            }?.addOnFailureListener {
//                // Handle any errors
//            }
//        }
    }
    private fun emailToUserName(email : String ): String{
        var userName= email
        val regex = Regex("[^A-Za-z0-9]")
        userName = regex.replace(userName, "")
        return userName
    }
    private fun match(){
        if (imageView1.drawable != null && imageView2.drawable != null) {
            textViewSimilarity.text = getString(R.string.processing)

            matchFaces(getImageBitmap(imageView1), getImageBitmap(imageView2))
//            buttonMatch.isEnabled = false
            buttonLiveness.isEnabled = false
            buttonContinue.isEnabled = false

        } else {
            Toast.makeText(
                this@MatchFacesActivity,
                getString(R.string.both_images_compulsory),
                Toast.LENGTH_SHORT
            ).show()
//                pbProcessCapture.visibility = View.INVISIBLE
        }
    }

//    private fun showMenu(imageView: ImageView?, i: Int) {
//        val popupMenu = PopupMenu(this@MatchFacesActivity, imageView)
//        popupMenu.setOnMenuItemClickListener { menuItem: MenuItem ->
//            when (menuItem.itemId) {
//                R.id.gallery -> {
//                    openGallery(i)
//                    return@setOnMenuItemClickListener true
//                }
//                R.id.camera -> {
//                    startFaceCaptureActivity(imageView)
//                    return@setOnMenuItemClickListener true
//                }
//                else -> return@setOnMenuItemClickListener false
//            }
//        }
//        popupMenu.menuInflater.inflate(R.menu.menu, popupMenu.menu)
//        popupMenu.show()
//    }

    private fun getImageBitmap(imageView: ImageView?): Bitmap {
        imageView?.invalidate()
        val drawable = imageView?.drawable as BitmapDrawable

        return drawable.bitmap
    }

//    private fun openGallery(id: Int) {
//        val gallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI)
//        startActivityForResult(gallery, id)
//    }

//    private fun startFaceCaptureActivity(imageView: ImageView?) {
//        val configuration = FaceCaptureConfiguration.Builder().setCameraSwitchEnabled(true).build()
//        FaceSDK.Instance().presentFaceCaptureActivity(this@MatchFacesActivity, configuration) { faceCaptureResponse: FaceCaptureResponse? ->
//            if (faceCaptureResponse?.image != null) {
//                imageView!!.setImageBitmap(faceCaptureResponse.image!!.bitmap)
//                imageView.tag = ImageType.LIVE
//            }
//        }
//    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != RESULT_OK)
            return
        if (data != null) {
            imageUri = data.data
        }
        textViewSimilarity.text = getString(R.string.similarity_null)

        var imageView: ImageView? = null
        if (requestCode == PICK_IMAGE_1)
            imageView = imageView1;
        else if (requestCode == PICK_IMAGE_2)
            imageView = imageView2;

        imageView?.setImageURI(imageUri)
        imageView?.tag = ImageType.PRINTED
    }

    private fun matchFaces(first: Bitmap, second: Bitmap) {
        val firstImage = MatchFacesImage(first, imageView1.tag as ImageType, true)
        val secondImage = MatchFacesImage(second, imageView2.tag as ImageType, true)
        val matchFacesRequest = MatchFacesRequest(arrayListOf(firstImage, secondImage));
        FaceSDK.Instance().matchFaces(matchFacesRequest) { matchFacesResponse: MatchFacesResponse ->
            val split = MatchFacesSimilarityThresholdSplit(matchFacesResponse.results, 0.75)
            if (split.matchedFaces.size > 0) {
                val similarity = split.matchedFaces[0].similarity
                textViewSimilarity.text = getString(R.string.similarity_result,  String.format("%.2f", similarity * 100))
            } else {
                textViewSimilarity.text = getString(R.string.similarity_null)
            }
            Log.d("similar", textViewSimilarity.text.toString().replace("Similarity: ","").replace("%",""))
//            buttonMatch.isEnabled = true
            buttonLiveness.isEnabled = true
            buttonContinue.isEnabled = true
            pbProcessCapture.visibility = View.INVISIBLE
        }
    }

    private fun captureLiveness() {
        val configuration = LivenessConfiguration.Builder().setCameraSwitchEnabled(true).build()
        FaceSDK.Instance().startLiveness(this@MatchFacesActivity, configuration) { livenessResponse: LivenessResponse ->
            if (livenessResponse.bitmap != null) {
                imageView2.setImageBitmap(livenessResponse.bitmap)
                imageView2.tag = ImageType.LIVE
                if (livenessResponse.liveness == LivenessStatus.PASSED) {
//                    textViewLiveness.text = getString(R.string.liveness_passed)
//                    buttonMatch.performClick()
                    match()
                } else {
//                    textViewLiveness.text = getString(R.string.liveness_unknown)
                }
            } else {
//                textViewLiveness.text = getString(R.string.liveness_null)
            }

            textViewSimilarity.text = getString(R.string.similarity_null)
        }
    }

    companion object {
        private const val PICK_IMAGE_1 = 1
        private const val PICK_IMAGE_2 = 2
    }
}
