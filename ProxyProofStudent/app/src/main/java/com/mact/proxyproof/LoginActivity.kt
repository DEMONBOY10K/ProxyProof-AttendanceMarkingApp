package com.mact.proxyproof

import android.Manifest.permission.READ_PHONE_STATE
import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.*
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.telephony.TelephonyManager
import android.util.Log
import android.util.Pair
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
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetector
import com.google.mlkit.vision.face.FaceDetectorOptions
import com.mact.proxyproof.facemodel.IdentifyFace
import com.mact.proxyproof.facemodel.SimilarityClassifier
import com.mact.proxyproof.fragments.ResetPasswordDialog
//import kotlinx.android.synthetic.main.activity_identifyface.*
import kotlinx.android.synthetic.main.activity_login2.*
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.io.IOException
import java.io.OutputStreamWriter
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel


class LoginActivity : AppCompatActivity() {
    private lateinit var database : DatabaseReference
    private lateinit var user : FirebaseAuth
    private var count = 0
    private var backPressedTime:Long = 0
    lateinit var backToast:Toast
    private var scholar: String? = null
    private var imei: String? = null
    private val PHONE_STATE_PERMISSION_CODE = 102
    private var userName : String? =null;
    //////////////////////////////////////////////////////////////////
    var detector: FaceDetector? = null
    var tfLite: Interpreter? = null
//    var reco_name: TextView? = null
//    private var preview_info: TextView? = null
//    private lateinit var recognize: Button
    var developerMode = false
    var distance = 1.0f
    var start = true
    var flipX: Boolean = false
    var context: Context = this
    private lateinit var intValues: IntArray
    var inputSize = 112 //Input size for model
    var isModelQuantized = false
    private lateinit var embeedings: Array<FloatArray>
    var IMAGE_MEAN = 128.0f
    var IMAGE_STD = 128.0f
    var OUTPUT_SIZE = 192 //Output size of model
    var modelFile = "mobile_face_net.tflite" //model name
    private var registered: HashMap<String, SimilarityClassifier.Recognition> =
        HashMap<String, SimilarityClassifier.Recognition>() //saved Faces
    ///////////////////////////////////////////////////////////////////////

    private var bitmap : Bitmap? = null
    private var storageReference: StorageReference? = null

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
        setContentView(R.layout.activity_login2)
        val slideRightAnimation = AnimationUtils.loadAnimation(this, R.anim.slide_left_to_center_animation)
        val fadeInAnimation = AnimationUtils.loadAnimation(this, R.anim.alphafadein700_animation)
        lavLogin.startAnimation(slideRightAnimation)
        constraintLayoutLogin.startAnimation(fadeInAnimation)
        user = FirebaseAuth.getInstance()
        if(user.currentUser!= null){
            user.currentUser?.let {
                if(user.currentUser?.isEmailVerified == true){
                    Log.d("currentUserAtLogin",it.email.toString()+" has logged in")
                    Intent(this,CameraActivity::class.java).also { newIt->
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
            Intent(this, SignUpActivity::class.java).also {
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
            val resetDialog = ResetPasswordDialog()
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
                database = FirebaseDatabase.getInstance(url).getReference("students")
                database.child(userName!!).get().addOnSuccessListener {
                    if(it.exists()){
                        val userEmail = it.child("email").value
                        val userPass = it.child("password").value
                        val userFName = it.child("fName").value.toString()
                        Log.d("user",user.currentUser.toString())
                        if(userEmail==email
//                            &&userPass==password
                        ){
                            showDialog(this,"Login Successful")
                            scholar = it.child("schNum").value.toString()
                            storageReference =
                                FirebaseStorage.getInstance().getReference("StudentFaceID/$userName$scholar")
                            imei = getIMEIDeviceId(this)
                            createTxt()
                            saveFaceData()
//                            Intent(this,IdentifyFace::class.java).also {newIt->
//                                startActivity(newIt)
//                                overridePendingTransition(R.anim.fadein_animation, R.anim.fadeout_animation)
//                                finish()
//                            }
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
                btnLogIn.visibility=View.VISIBLE
                pbLoading.visibility=View.INVISIBLE
                val alert = "Email Verification Pending!"
                tvAlert.text = alert
                Log.d("currentUserAtLogin", user.currentUser?.email.toString()+" has not verified his email address")
            }
        }, 1500) // 1500 is the delayed time in milliseconds.
    }

    fun saveFaceData(){
        try {
            tfLite = loadModelFile(this, modelFile)?.let { Interpreter(it) }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        //Initialize Face Detector
        val highAccuracyOpts: FaceDetectorOptions = FaceDetectorOptions.Builder()
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
            .build()
        detector = FaceDetection.getClient(highAccuracyOpts)
//        cameraBind()

        val ONE_MEGABYTE: Long = 1024 * 1024
        val handler = Handler(Looper.getMainLooper())
        handler.postDelayed({
            storageReference?.getBytes(ONE_MEGABYTE)?.addOnSuccessListener {
                bitmap = BitmapFactory.decodeByteArray(it, 0, it.size)
                Log.d("user", "Bitmap Collected")
                val handler2 = Handler(Looper.getMainLooper())
                handler2.postDelayed({
                    try {
                        val impphoto: InputImage =
                            InputImage.fromBitmap(bitmap!!, 0)
                        detector?.process(impphoto)
                            ?.addOnSuccessListener { faces ->
                                if (faces.size != 0) {
//                        recognize.text = "Recognize"
//                        reco_name!!.visibility = View.INVISIBLE
//                        preview_info?.setText("1.Bring Face in view of Camera.\n\n2.Your Face preview will appear here.\n\n3.Click Add button to save face.")
                                    val face: Face = faces[0] as Face
                                    //                                System.out.println(face);

                                    //write code to recreate bitmap from source
                                    //Write code to show bitmap to canvas
                                    var frame_bmp: Bitmap? = null
                                    try {
                                        frame_bmp = bitmap
                                    } catch (e: IOException) {
                                        e.printStackTrace()
                                    }
                                    val frame_bmp1 = rotateBitmap(frame_bmp, 0, flipX, false)

                                    //face_preview.setImageBitmap(frame_bmp1);
                                    val boundingBox = RectF(face.getBoundingBox())
                                    val cropped_face = getCropBitmapByCPU(frame_bmp1, boundingBox)
                                    val scaled = getResizedBitmap(cropped_face, 112, 112)
                                    // face_preview.setImageBitmap(scaled);
                                    recognizeImage(scaled)
                                    addFace()
                                    val handler3 = Handler(Looper.getMainLooper())
                                    handler3.postDelayed({
                                        insertToSP(registered, 0) //mode: 0:save all, 1:clear all, 2:update all
                                        val handler4 = Handler(Looper.getMainLooper())
                                        handler4.postDelayed({
                                            insertToSP(registered, 0) //mode: 0:save all, 1:clear all, 2:update all
                                            logedIn()
                                        }, 2000)
                                        }, 1000)

                                    //                                System.out.println(boundingBox);
                                    try {
                                        Thread.sleep(100)
                                    } catch (e: InterruptedException) {
                                        e.printStackTrace()
                                    }
                                }
                            }?.addOnFailureListener {
                                start = true
                                Toast.makeText(context, "Failed to add", Toast.LENGTH_SHORT).show()
                            }
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }, 3000)
            }?.addOnFailureListener {
                // Handle any errors
                Log.d("user", "Bitmap Collection Failed")
            }
        }, 1500)
    }
    @Throws(IOException::class)
    private fun loadModelFile(activity: Activity, MODEL_FILE: String): MappedByteBuffer? {
        val fileDescriptor = activity.assets.openFd(MODEL_FILE)
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = fileDescriptor.startOffset
        val declaredLength = fileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }
    private fun rotateBitmap(
        bitmap: Bitmap?, rotationDegrees: Int, flipX: Boolean, flipY: Boolean
    ): Bitmap {
        val matrix = Matrix()

        // Rotate the image back to straight.
        matrix.postRotate(rotationDegrees.toFloat())

        // Mirror the image along the X or Y axis.
        matrix.postScale(if (flipX) -1.0f else 1.0f, if (flipY) -1.0f else 1.0f)
        val rotatedBitmap =
            Bitmap.createBitmap(bitmap!!, 0, 0, bitmap.width, bitmap.height, matrix, true)

        // Recycle the old bitmap if it has changed.
        if (rotatedBitmap != bitmap) {
            bitmap.recycle()
        }
        return rotatedBitmap
    }

    private fun getCropBitmapByCPU(source: Bitmap?, cropRectF: RectF): Bitmap {
        val resultBitmap = Bitmap.createBitmap(
            cropRectF.width().toInt(),
            cropRectF.height().toInt(),
            Bitmap.Config.ARGB_8888
        )
        val cavas = Canvas(resultBitmap)

        // draw background
        val paint = Paint(Paint.FILTER_BITMAP_FLAG)
        paint.color = Color.WHITE
        cavas.drawRect(
            RectF(0F, 0F, cropRectF.width(), cropRectF.height()),
            paint
        )
        val matrix = Matrix()
        matrix.postTranslate(-cropRectF.left, -cropRectF.top)
        cavas.drawBitmap(source!!, matrix, paint)
        if (!source.isRecycled) {
            source.recycle()
        }
        return resultBitmap
    }
    fun getResizedBitmap(bm: Bitmap, newWidth: Int, newHeight: Int): Bitmap {
        val width = bm.width
        val height = bm.height
        val scaleWidth = newWidth.toFloat() / width
        val scaleHeight = newHeight.toFloat() / height
        // CREATE A MATRIX FOR THE MANIPULATION
        val matrix = Matrix()
        // RESIZE THE BIT MAP
        matrix.postScale(scaleWidth, scaleHeight)

        // "RECREATE" THE NEW BITMAP
        val resizedBitmap = Bitmap.createBitmap(
            bm, 0, 0, width, height, matrix, false
        )
        bm.recycle()
        return resizedBitmap
    }
    @SuppressLint("SetTextI18n")
    fun recognizeImage(bitmap: Bitmap) {

        // set Face to Preview
        //Create ByteBuffer to store normalized image
        val imgData = ByteBuffer.allocateDirect(1 * inputSize * inputSize * 3 * 4)
        imgData.order(ByteOrder.nativeOrder())
        intValues = IntArray(inputSize * inputSize)

        //get pixel values from Bitmap to normalize
        bitmap.getPixels(intValues, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)
        imgData.rewind()
        for (i in 0 until inputSize) {
            for (j in 0 until inputSize) {
                val pixelValue = intValues[i * inputSize + j]
                if (isModelQuantized) {
                    // Quantized model
                    imgData.put((pixelValue shr 16 and 0xFF).toByte())
                    imgData.put((pixelValue shr 8 and 0xFF).toByte())
                    imgData.put((pixelValue and 0xFF).toByte())
                } else { // Float model
                    imgData.putFloat(((pixelValue shr 16 and 0xFF) - IMAGE_MEAN) / IMAGE_STD)
                    imgData.putFloat(((pixelValue shr 8 and 0xFF) - IMAGE_MEAN) / IMAGE_STD)
                    imgData.putFloat(((pixelValue and 0xFF) - IMAGE_MEAN) / IMAGE_STD)
                }
            }
        }
        //imgData is input to our model
        val inputArray = arrayOf<Any>(imgData)
        val outputMap: MutableMap<Int, Any> = HashMap()
        embeedings =
            Array(1) { FloatArray(OUTPUT_SIZE) } //output of model will be stored in this variable
        outputMap[0] = embeedings
        tfLite?.runForMultipleInputsOutputs(inputArray, outputMap) //Run model
        var distance_local: Float
        val id = "0"
        val label = "?"

        //Compare new face with saved Faces.
        if (registered.size > 0) {
            val nearest = findNearest(embeedings[0]) //Find 2 closest matching face
            if (nearest[0] != null) {
                val name = nearest[0]!!.first //get name and distance of closest matching face
                // label = name;
                distance_local = nearest[0]!!.second
                if (developerMode) {
                    if (distance_local < distance) //If distance between Closest found face is more than 1.000 ,then output UNKNOWN face.
                    {
//                        reco_name!!.text = """
//                        Nearest: $name
//                        Dist: ${String.format("%.3f", distance_local)}
//                        2nd Nearest: ${nearest[1]!!.first}
//                        Dist: ${String.format("%.3f", nearest[1]!!.second)}
//                        """.trimIndent() else reco_name!!.text =
//                        """
//     Unknown
//     Dist: ${String.format("%.3f", distance_local)}
//     Nearest: $name
//     Dist: ${String.format("%.3f", distance_local)}
//     2nd Nearest: ${nearest[1]!!.first}
//     Dist: ${String.format("%.3f", nearest[1]!!.second)}
//     """.trimIndent()
                }

//                    System.out.println("nearest: " + name + " - distance: " + distance_local);
                } else {
                    if (distance_local < distance) //If distance between Closest found face is more than 1.000 ,then output UNKNOWN face.
                    {
//                        reco_name!!.text = name
                        if(scholar == name){
//                            variable++
//                            Log.d("user",variable.toString())
//                            if(variable>10){
//                                btnVerified.visibility=View.VISIBLE
//                            }
                        }
                    }
                    else{
//                        variable = 0
//                        reco_name!!.text = "Unknown"
                        //System.out.println("nearest: " + name + " - distance: " + distance_local);


                    }
                }
            }
        }
    }
    private fun findNearest(emb: FloatArray): List<Pair<String, Float>?> {
        val neighbour_list: MutableList<Pair<String, Float>?> = ArrayList()
        var ret: Pair<String, Float>? = null //to get closest match
        var prev_ret: Pair<String, Float>? = null //to get second closest match

        for (entry : Map.Entry<String, SimilarityClassifier.Recognition> in registered.entries){
            val name = entry.key
            val knownEmb = (entry.value.getExtra() as Array<FloatArray?>?)!![0]

            var distance = 0f
            for (i in emb.indices) {
                val diff = emb[i] - knownEmb!![i]
                distance += diff * diff
            }
            distance = Math.sqrt(distance.toDouble()).toFloat()
            if (ret == null || distance < ret.second) {
                prev_ret = ret
                ret = Pair(name, distance)
            }
        }

        if (prev_ret == null) prev_ret = ret
        neighbour_list.add(ret)
        neighbour_list.add(prev_ret)
        return neighbour_list
    }
    private fun addFace() {
        run {
            start = false
//            val builder =
//                AlertDialog.Builder(context)
//            builder.setTitle("Enter Name")
//
//            // Set up the input
//            val input = EditText(context)
//            input.inputType = InputType.TYPE_CLASS_TEXT
//            builder.setView(input)

            // Set up the buttons
//            builder.setPositiveButton(
//                "ADD"
//            ) { dialog, which -> //Toast.makeText(context, input.getText().toString(), Toast.LENGTH_SHORT).show();

                //Create and Initialize new object with Face embeddings and Name.
                val result = SimilarityClassifier.Recognition(
                    "0", "", -1f
                )
                result.extra = embeedings
//                registered[input.text.toString()] = result
                registered[scholar.toString()] = result
                start = true
//            }
//            builder.setNegativeButton(
//                "Cancel"
//            ) { dialog, which ->
//                start = true
//                dialog.cancel()
//            }
//            builder.show()
        }
    }
    private fun  insertToSP(jsonMap: java.util.HashMap<String, SimilarityClassifier.Recognition>, mode: Int) {
        if (mode == 1) //mode: 0:save all, 1:clear all, 2:update all
            jsonMap.clear() else if (mode == 0) jsonMap.putAll(readFromSP())
        val jsonString = Gson().toJson(jsonMap)
        //        for (Map.Entry<String, SimilarityClassifier.Recognition> entry : jsonMap.entrySet())
//        {
//            System.out.println("Entry Input "+entry.getKey()+" "+  entry.getValue().getExtra());
//        }
        val sharedPreferences = getSharedPreferences("HashMap", MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("map", jsonString)
        //System.out.println("Input josn"+jsonString.toString());
        editor.apply()
        Toast.makeText(context, "Recognitions Saved", Toast.LENGTH_SHORT).show()
    }
    //Load Faces from Shared Preferences.Json String to Recognition object
    private fun readFromSP(): HashMap<String, SimilarityClassifier.Recognition> {
        val sharedPreferences = getSharedPreferences("HashMap", MODE_PRIVATE)
        val defValue = Gson().toJson(HashMap<String, SimilarityClassifier.Recognition>())
        val json = sharedPreferences.getString("map", defValue)
        // System.out.println("Output json"+json.toString());
        val token: TypeToken<HashMap<String?, SimilarityClassifier.Recognition?>?> =
            object : TypeToken<HashMap<String?, SimilarityClassifier.Recognition?>?>() {}
        val retrievedMap: HashMap<String, SimilarityClassifier.Recognition> =
            Gson().fromJson<HashMap<String, SimilarityClassifier.Recognition>>(json, token.type)
        // System.out.println("Output map"+retrievedMap.toString());

        //During type conversion and save/load procedure,format changes(eg float converted to double).
        //So embeddings need to be extracted from it in required format(eg.double to float).
        for (entry : Map.Entry<String, SimilarityClassifier.Recognition> in retrievedMap.entries) {
            val output = Array(1) {
                FloatArray(
                    OUTPUT_SIZE
                )
            }
            var arrayList = entry.value.getExtra() as java.util.ArrayList<*>?
            arrayList = arrayList!![0] as java.util.ArrayList<*>
            for (counter in arrayList.indices) {
                output[0][counter] = (arrayList[counter] as Double).toFloat()
            }
            entry.value.extra = output

            //System.out.println("Entry output "+entry.getKey()+" "+entry.getValue().getExtra() );
        }
        //        System.out.println("OUTPUT"+ Arrays.deepToString(outut));
        Toast.makeText(context, "Recognitions Loaded", Toast.LENGTH_SHORT).show()
        return retrievedMap
    }











fun logedIn(){
    Intent(this,CameraActivity::class.java).also {newIt->
        startActivity(newIt)
        overridePendingTransition(R.anim.fadein_animation, R.anim.fadeout_animation)
        finish()
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

    @RequiresApi(Build.VERSION_CODES.O)
    fun createTxt() {
        // add-write text into file
        try {
            val fileout = openFileOutput("$userName.txt", MODE_PRIVATE)
            val fileIdentity = openFileOutput("identity.txt", MODE_PRIVATE)
            ActivityCompat.requestPermissions(this,
                arrayOf(READ_PHONE_STATE),
                PHONE_STATE_PERMISSION_CODE
            )
//            val telephonyManager = getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
//            imei = telephonyManager.deviceId
            Log.d("current1", "$imei")
            val outputWriter = OutputStreamWriter(fileout)
            val outputWriterIdentity = OutputStreamWriter(fileIdentity)
            outputWriter.write(scholar)
            outputWriterIdentity.write("Student")
            outputWriter.close()
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

    fun getIMEIDeviceId(context: Context): String? {
        val deviceId: String
        deviceId = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
        } else {
            val mTelephony = context.getSystemService(TELEPHONY_SERVICE) as TelephonyManager
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (context.checkSelfPermission(READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                    return ""
                }
            }
            assert(mTelephony != null)
            if (mTelephony.deviceId != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    mTelephony.imei
                } else {
                    mTelephony.deviceId
                }
            } else {
                Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
            }
        }
        Log.d("deviceId", deviceId)
        return deviceId
    }

}