package com.mact.proxyproof.facemodel


import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.*
import android.media.Image
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.InputType
import android.util.Log
import android.util.Pair
import android.util.Size
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.google.android.gms.tasks.Task
import com.google.common.util.concurrent.ListenableFuture
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetector
import com.google.mlkit.vision.face.FaceDetectorOptions
import com.mact.proxyproof.R
import com.mact.proxyproof.sender.FileSenderActivity
import kotlinx.android.synthetic.main.activity_identifyface.*
import org.tensorflow.lite.Interpreter
import java.io.*
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.MappedByteBuffer
import java.nio.ReadOnlyBufferException
import java.nio.channels.FileChannel
import java.util.*
import java.util.concurrent.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.experimental.inv
import kotlin.jvm.internal.Intrinsics


class IdentifyFace : AppCompatActivity() {
    var detector: FaceDetector? = null

    private var cameraProviderFuture: ListenableFuture<ProcessCameraProvider>? = null
    private var previewView: PreviewView? = null

    var tfLite: Interpreter? = null
    var reco_name: TextView? = null
//    private var preview_info:TextView? = null
    private var textAbove_preview:TextView? = null
//    private lateinit var recognize: Button
//    var actions: Button? = null

    var cameraSelector: CameraSelector? = null
    var developerMode = false
    var distance = 0.0f
    var start = true
    var flipX: Boolean = false
    var context: Context = this
    private var cam_face: Int = CameraSelector.LENS_FACING_FRONT //Default Back Camera

    private lateinit var intValues: IntArray
    var inputSize = 112 //Input size for model

    var isModelQuantized = false
    private lateinit var embeedings: Array<FloatArray>
    var IMAGE_MEAN = 128.0f
    var IMAGE_STD = 128.0f
    var OUTPUT_SIZE = 192 //Output size of model

    private val SELECT_PICTURE = 1
    var cameraProvider: ProcessCameraProvider? = null
    private val MY_CAMERA_REQUEST_CODE = 100

    var modelFile = "mobile_face_net.tflite" //model name

    private var scholar : String? = null
    private var userName : String? = null
    private var storageReference: StorageReference? = null
    private lateinit var database : DatabaseReference
    private lateinit var user : FirebaseAuth
    private var bitmap : Bitmap? = null

    var variable = 0
    var faceMatchProgress = 0

    private var registered: HashMap<String, SimilarityClassifier.Recognition> =
        HashMap<String, SimilarityClassifier.Recognition>() //saved Faces

    @SuppressLint("SetTextI18n")
    @RequiresApi(api = Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_identifyface)
        user = FirebaseAuth.getInstance()
        storageReference = FirebaseStorage.getInstance().getReference("StudentFaceID/")
        if (user.currentUser != null) {
            user.currentUser?.let { it ->
                if (user.currentUser?.isEmailVerified == true) {
                    userName = emailToUserName(user.currentUser?.email.toString())
                    ReadBtn()
                }
            }
        }



        registered = readFromSP() //Load saved faces from memory when app starts

        reco_name = findViewById(R.id.textView)
//        preview_info = findViewById<TextView>(R.id.textView2)
        textAbove_preview = findViewById<TextView>(R.id.textAbovePreview)
        val sharedPref = getSharedPreferences("Distance", MODE_PRIVATE)
        distance = sharedPref.getFloat("distance", 0.65f)
//        recognize = findViewById(R.id.button3)
//        actions = findViewById<Button>(R.id.button2)
        textAbove_preview?.text = "Recognized Face:"
        //        preview_info.setText("        Recognized Face:");
        //Camera Permission
        if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.CAMERA), MY_CAMERA_REQUEST_CODE)
        }
        //On-screen Action Button
//        actions?.setOnClickListener { loadphoto() }
//        recognize.setOnClickListener {
//            if (recognize.text.toString() == "Recognize") {
//                start = true
//                textAbove_preview?.text = "Recognized Face:"
//                recognize.text = "Add Face"
//                reco_name?.visibility = View.VISIBLE
//                preview_info?.text = ""
//                //preview_info.setVisibility(View.INVISIBLE);
//            } else {
//                textAbove_preview?.text = "Face Preview: "
//                recognize.text = "Recognize"
//                reco_name?.visibility = View.INVISIBLE
//                preview_info?.text = "1.Bring Face in view of Camera.\n\n2.Your Face preview will appear here.\n\n3.Click Add button to save face."
//            }
//        }

        //Load model
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
        cameraBind()

        val ONE_MEGABYTE: Long = 1024 * 1024
        val handler = Handler(Looper.getMainLooper())
        handler.postDelayed({
            storageReference?.getBytes(ONE_MEGABYTE)?.addOnSuccessListener {
                bitmap = BitmapFactory.decodeByteArray(it, 0, it.size)
                Log.d("user", "Bitmap Collected")
            }?.addOnFailureListener {
                // Handle any errors
            }
        }, 3000)

        btnVerified.setOnClickListener{
            Intent(this,FileSenderActivity::class.java).also {newIt->
                startActivity(newIt)
                overridePendingTransition(R.anim.fadein_animation, R.anim.fadeout_animation)
                finish()
            }
        }
//        btnDownload.setOnClickListener{
//            try {
//                val impphoto: InputImage =
//                    InputImage.fromBitmap(bitmap!!, 0)
//                detector?.process(impphoto)
//                    ?.addOnSuccessListener { faces ->
//                        if (faces.size != 0) {
//                            recognize.text = "Recognize"
//                            reco_name!!.visibility = View.INVISIBLE
//                            preview_info?.setText("1.Bring Face in view of Camera.\n\n2.Your Face preview will appear here.\n\n3.Click Add button to save face.")
//                            val face: Face = faces[0] as Face
//                            //                                System.out.println(face);
//
//                            //write code to recreate bitmap from source
//                            //Write code to show bitmap to canvas
//                            var frame_bmp: Bitmap? = null
//                            try {
//                                frame_bmp = bitmap
//                            } catch (e: IOException) {
//                                e.printStackTrace()
//                            }
//                            val frame_bmp1 = rotateBitmap(frame_bmp, 0, flipX, false)
//
//                            //face_preview.setImageBitmap(frame_bmp1);
//                            val boundingBox = RectF(face.getBoundingBox())
//                            val cropped_face = getCropBitmapByCPU(frame_bmp1, boundingBox)
//                            val scaled = getResizedBitmap(cropped_face, 112, 112)
//                            // face_preview.setImageBitmap(scaled);
//                            recognizeImage(scaled)
//                            addFace()
//                            val handler = Handler(Looper.getMainLooper())
//                            handler.postDelayed({
//                                insertToSP(registered, 0) //mode: 0:save all, 1:clear all, 2:update all
//                            }, 3000)
//
//
//                            //                                System.out.println(boundingBox);
//                            try {
//                                Thread.sleep(100)
//                            } catch (e: InterruptedException) {
//                                e.printStackTrace()
//                            }
//                        }
//                    }?.addOnFailureListener {
//                        start = true
//                        Toast.makeText(context, "Failed to add", Toast.LENGTH_SHORT).show()
//                    }
//            } catch (e: IOException) {
//                e.printStackTrace()
//            }
//        }
//        btnSave.setOnClickListener{
//            insertToSP(registered, 0)
//        }
//        btnDelete.setOnClickListener{
//            registered.clear()
//            insertToSP(registered, 1)
//        }

    }
    fun ReadBtn() {
        try {
            val fileIn = openFileInput(userName + ".txt")
            val InputRead = InputStreamReader(fileIn as InputStream)
            val inputBuffer = CharArray(100)
            var s = ""
            val var6 = false
            while (true) {
                val var7 = InputRead.read(inputBuffer)
                if (var7 <= 0) {
                    InputRead.close()
                    //					EditText var10000 = this.textmsg;
//					Intrinsics.checkNotNull(var10000);
//					var10000.setText((CharSequence)s);
                    break
                }
                val var8 = 0
                val readstring: String = String(inputBuffer, var8, var7)
                s = Intrinsics.stringPlus(s, readstring)
            }
            scholar = s
            Log.d("currentUserAtLogin", "$scholar has logged in")
        } catch (var10: java.lang.Exception) {
            var10.printStackTrace()
        }
    }
    private fun emailToUserName(email : String ): String{
        var userName= email
        val regex = Regex("[^A-Za-z0-9]")
        userName = regex.replace(userName, "")
        return userName
    }

    private fun addFace() {
        run {
            start = false
            val builder =
                AlertDialog.Builder(context)
            builder.setTitle("Enter Name")

            // Set up the input
            val input = EditText(context)
            input.inputType = InputType.TYPE_CLASS_TEXT
            builder.setView(input)

            // Set up the buttons
            builder.setPositiveButton(
                "ADD"
            ) { dialog, which -> //Toast.makeText(context, input.getText().toString(), Toast.LENGTH_SHORT).show();

                //Create and Initialize new object with Face embeddings and Name.
                val result = SimilarityClassifier.Recognition(
                    "0", "", -1f
                )
                result.extra = embeedings
                registered[input.text.toString()] = result
                start = true
            }
            builder.setNegativeButton(
                "Cancel"
            ) { dialog, which ->
                start = true
                dialog.cancel()
            }
            builder.show()
        }
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == MY_CAMERA_REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "camera permission granted", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(this, "camera permission denied", Toast.LENGTH_LONG).show()
            }
        }
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

    //Bind camera and preview view
    private fun cameraBind() {
        cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        previewView = findViewById(R.id.previewView)
        cameraProviderFuture!!.addListener({
            try {
                cameraProvider = cameraProviderFuture!!.get()
                bindPreview(cameraProvider!!)
            } catch (e: ExecutionException) {
                // No errors need to be handled for this in Future.
                // This should never be reached.
            } catch (e: InterruptedException) {
            }
        }, ContextCompat.getMainExecutor(this))
    }

    fun bindPreview(cameraProvider: ProcessCameraProvider) {
        val preview: Preview = Preview.Builder()
            .build()
        cameraSelector = CameraSelector.Builder()
            .requireLensFacing(cam_face)
            .build()
        preview.setSurfaceProvider(previewView?.getSurfaceProvider())
        val imageAnalysis: ImageAnalysis = ImageAnalysis.Builder()
            .setTargetResolution(Size(640, 480))
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST) //Latest frame is shown
            .build()
        val executor: Executor = Executors.newSingleThreadExecutor()
        imageAnalysis.setAnalyzer(executor, object : ImageAnalysis.Analyzer {
            @SuppressLint("SetTextI18n")
            override fun analyze(imageProxy: ImageProxy) {
                try {
                    Thread.sleep(0) //Camera preview refreshed every 10 millisec(adjust as required)
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }
                var image: InputImage? = null
                @SuppressLint(
                    "UnsafeExperimentalUsageError",
                    "UnsafeOptInUsageError"
                ) val mediaImage:  // Camera Feed-->Analyzer-->ImageProxy-->mediaImage-->InputImage(needed for ML kit face detection)
                        Image? = imageProxy.image
                if (mediaImage != null) {
                    image = InputImage.fromMediaImage(
                        mediaImage,
                        imageProxy.imageInfo.rotationDegrees
                    )
                    //                    System.out.println("Rotation "+imageProxy.getImageInfo().getRotationDegrees());
                }

//                System.out.println("ANALYSIS");

                //Process acquired image to detect faces
                val result: Task<List<Face>> = detector?.process(image!!)
                    ?.addOnSuccessListener { faces ->
                        if (faces.isNotEmpty()) {
                            val face: Face =
                                faces[0] as Face //Get first face from detected faces
                            //                                                    System.out.println(face);

                            //mediaImage to Bitmap
                            val frame_bmp = toBitmap(mediaImage)
                            val rot: Int = imageProxy.getImageInfo().getRotationDegrees()

                            //Adjust orientation of Face
                            val frame_bmp1 = rotateBitmap(frame_bmp, rot, false, false)


                            //Get bounding box of face
                            val boundingBox = RectF(face.getBoundingBox())

                            //Crop out bounding box from whole Bitmap(image)
                            var cropped_face = getCropBitmapByCPU(frame_bmp1, boundingBox)
                            if (flipX) cropped_face =
                                rotateBitmap(cropped_face, 0, flipX, false)
                            //Scale the acquired Face to 112*112 which is required input for model
                            val scaled = getResizedBitmap(cropped_face, 112, 112)
                            if (start) recognizeImage(scaled) //Send scaled bitmap to create face embeddings.
                            //                                                    System.out.println(boundingBox);
                        } else {
                            if (registered.isEmpty())
                                reco_name!!.text = "Add Face"
                            else
                                reco_name!!.text = "No Face Detected!"

                        }
                    }
                    ?.addOnFailureListener {
                        // Task failed with an exception
                        // ...
                    }
                    ?.addOnCompleteListener {
                        imageProxy.close()
                    } as Task<List<Face>>
            }
        })
        cameraProvider.bindToLifecycle(
            this as LifecycleOwner,
            cameraSelector!!,
            imageAnalysis,
            preview
        )
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
                        reco_name!!.text = """
                        Nearest: $name
                        Dist: ${String.format("%.3f", distance_local)}
                        2nd Nearest: ${nearest[1]!!.first}
                        Dist: ${String.format("%.3f", nearest[1]!!.second)}
                        """.trimIndent() else reco_name!!.text =
                        """
     Unknown 
     Dist: ${String.format("%.3f", distance_local)}
     Nearest: $name
     Dist: ${String.format("%.3f", distance_local)}
     2nd Nearest: ${nearest[1]!!.first}
     Dist: ${String.format("%.3f", nearest[1]!!.second)}
     """.trimIndent()

//                    System.out.println("nearest: " + name + " - distance: " + distance_local);
                } else {
                    if (distance_local < distance) //If distance between Closest found face is more than 1.000 ,then output UNKNOWN face.
                    {
                        reco_name!!.text = name
                        if(scholar == name){
                            variable++
                            pBarFace.progress  = faceMatchProgress
                            tvFaceMatchProgress.text = "$faceMatchProgress%"
                            faceMatchProgress += 10
                            Log.d("user",variable.toString())
                            if(variable>10){
                                btnVerified.visibility=View.VISIBLE
                                tvVerification.text = "Verified!!"
                                pBarFace.visibility=View.INVISIBLE
                                tvFaceMatchProgress.visibility=View.INVISIBLE
                                reco_name!!.visibility=View.INVISIBLE
                                textAbovePreview.visibility=View.INVISIBLE
                            }
                        }
                    }
                    else{
                        variable = 0
                        reco_name!!.text = "Unknown"
                        faceMatchProgress = 0
                        pBarFace.progress  = faceMatchProgress
                        //System.out.println("nearest: " + name + " - distance: " + distance_local);


                    }
                }
            }
        }
    }


    //Compare Faces by distance between face embeddings
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

    //IMPORTANT. If conversion not done ,the toBitmap conversion does not work on some devices.
    private fun YUV_420_888toNV21(image: Image?): ByteArray {
        val width = image!!.width
        val height = image.height
        val ySize = width * height
        val uvSize = width * height / 4
        val nv21 = ByteArray(ySize + uvSize * 2)
        val yBuffer = image.planes[0].buffer // Y
        val uBuffer = image.planes[1].buffer // U
        val vBuffer = image.planes[2].buffer // V
        var rowStride = image.planes[0].rowStride
        assert(image.planes[0].pixelStride == 1)
        var pos = 0
        if (rowStride == width) { // likely
            yBuffer[nv21, 0, ySize]
            pos += ySize
        } else {
            var yBufferPos = -rowStride.toLong() // not an actual position
            while (pos < ySize) {
                yBufferPos += rowStride.toLong()
                yBuffer.position(yBufferPos.toInt())
                yBuffer[nv21, pos, width]
                pos += width
            }
        }
        rowStride = image.planes[2].rowStride
        val pixelStride = image.planes[2].pixelStride
        assert(rowStride == image.planes[1].rowStride)
        assert(pixelStride == image.planes[1].pixelStride)
        if (pixelStride == 2 && rowStride == width && uBuffer[0] == vBuffer[1]) {
            // maybe V an U planes overlap as per NV21, which means vBuffer[1] is alias of uBuffer[0]
            val savePixel = vBuffer[1]
            try {
                vBuffer.put(1, savePixel.inv())
                if (uBuffer[0] == savePixel.inv()) {
                    vBuffer.put(1, savePixel)
                    vBuffer.position(0)
                    uBuffer.position(0)
                    vBuffer[nv21, ySize, 1]
                    uBuffer[nv21, ySize + 1, uBuffer.remaining()]
                    return nv21 // shortcut
                }
            } catch (ex: ReadOnlyBufferException) {
                // unfortunately, we cannot check if vBuffer and uBuffer overlap
            }

            // unfortunately, the check failed. We must save U and V pixel by pixel
            vBuffer.put(1, savePixel)
        }

        // other optimizations could check if (pixelStride == 1) or (pixelStride == 2),
        // but performance gain would be less significant
        for (row in 0 until height / 2) {
            for (col in 0 until width / 2) {
                val vuPos = col * pixelStride + row * rowStride
                nv21[pos++] = vBuffer[vuPos]
                nv21[pos++] = uBuffer[vuPos]
            }
        }
        return nv21
    }

    private fun toBitmap(image: Image?): Bitmap {
        val nv21 = YUV_420_888toNV21(image)
        val yuvImage = YuvImage(nv21, ImageFormat.NV21, image!!.width, image.height, null)
        val out = ByteArrayOutputStream()
        yuvImage.compressToJpeg(Rect(0, 0, yuvImage.width, yuvImage.height), 75, out)
        val imageBytes = out.toByteArray()
        //System.out.println("bytes"+ Arrays.toString(imageBytes));

        //System.out.println("FORMAT"+image.getFormat());
        return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
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

    //Load Photo from phone storage
    private fun loadphoto() {
        start = false
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), SELECT_PICTURE)
    }

    //Similar Analyzing Procedure

    @SuppressLint("SetTextI18n")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            if (requestCode == SELECT_PICTURE) {
                val selectedImageUri = data?.data
                try {
                    val impphoto: InputImage =
                        InputImage.fromBitmap(getBitmapFromUri(selectedImageUri)!!, 0)
                    detector?.process(impphoto)
                        ?.addOnSuccessListener { faces ->
                            if (faces.size != 0) {
//                                recognize.text = "Recognize"
                                reco_name!!.visibility = View.INVISIBLE
//                                preview_info?.setText("1.Bring Face in view of Camera.\n\n2.Your Face preview will appear here.\n\n3.Click Add button to save face.")
                                val face: Face = faces[0] as Face
                                //                                System.out.println(face);

                                //write code to recreate bitmap from source
                                //Write code to show bitmap to canvas
                                var frame_bmp: Bitmap? = null
                                try {
                                    frame_bmp = getBitmapFromUri(selectedImageUri)
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
            }
        }
    }

    @Throws(IOException::class)
    private fun getBitmapFromUri(uri: Uri?): Bitmap? {
        val parcelFileDescriptor = contentResolver.openFileDescriptor(uri!!, "r")
        val fileDescriptor = parcelFileDescriptor!!.fileDescriptor
        val image = BitmapFactory.decodeFileDescriptor(fileDescriptor)
        parcelFileDescriptor.close()
        return image
    }

}