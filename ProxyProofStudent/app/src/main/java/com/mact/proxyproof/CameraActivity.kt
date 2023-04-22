package com.mact.proxyproof

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.vision.CameraSource
import com.google.android.gms.vision.face.FaceDetector
import com.google.android.gms.vision.face.LargestFaceFocusingProcessor
import com.mact.proxyproof.facemodel.IdentifyFace
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

/* loaded from: classes4.dex */
class CameraActivity : AppCompatActivity(), SurfaceHolder.Callback,
    CameraSource.PictureCallback {
    private var cameraSource: CameraSource? = null
    private var detector: FaceDetector? = null
    private val neededPermissions = arrayOf("android.permission.CAMERA")
    private var surfaceHolder: SurfaceHolder? = null
    private var surfaceView: SurfaceView? = null
    var home: Button? = null
    var save: Button? = null
    var back: Button? = null

    /* JADX INFO: Access modifiers changed from: protected */
    // androidx.fragment.app.FragmentActivity, androidx.activity.ComponentActivity, androidx.core.app.ComponentActivity, android.app.Activity
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera)
        surfaceView = findViewById<View>(R.id.surfaceView) as SurfaceView
        val build = FaceDetector.Builder(this).setProminentFaceOnly(true).setTrackingEnabled(true)
            .setClassificationType(1).setMode(0).build()
        detector = build
        if (!build.isOperational) {
            Log.w("MainActivity", "Detector Dependencies are not yet available")
            return
        }
        Log.w("MainActivity", "Detector Dependencies are available")
        if (surfaceView != null) {
            val result = checkPermission()
            if (result) {
                setViewVisibility(R.id.tv_capture)
                setViewVisibility(R.id.surfaceView)
                setupSurfaceHolder()
            }
        }

    }

    private fun checkPermission(): Boolean {
        var strArr: Array<String?>
        val permissionsNotGranted = ArrayList<String>()
        for (permission in neededPermissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != 0) {
                permissionsNotGranted.add(permission)
            }
        }
        if (!permissionsNotGranted.isEmpty()) {
            var shouldShowAlert = false
            val it: Iterator<String> = permissionsNotGranted.iterator()
            while (it.hasNext()) {
                shouldShowAlert =
                    ActivityCompat.shouldShowRequestPermissionRationale(this, it.next())
            }
            if (shouldShowAlert) {
                showPermissionAlert(permissionsNotGranted.toTypedArray())
            } else {
                requestPermissions(permissionsNotGranted.toTypedArray())
            }
            return false
        }
        return true
    }

    @SuppressLint("ResourceType")
    private fun showPermissionAlert(permissions: Array<String>) {
        val alertBuilder = AlertDialog.Builder(this)
        alertBuilder.setCancelable(true)
        alertBuilder.setTitle(R.string.permission_required)
        alertBuilder.setMessage(R.string.permission_message)
        alertBuilder.setPositiveButton(
            17039379
        ) { dialog, which ->
            // from class: com.b2scam.camerademo.ui.MainActivity.1
            // android.content.DialogInterface.OnClickListener
            this@CameraActivity.requestPermissions(permissions)
        }
        val alert = alertBuilder.create()
        alert.show()
    }

    /* JADX INFO: Access modifiers changed from: private */
    fun requestPermissions(permissions: Array<String>?) {
        ActivityCompat.requestPermissions(this, permissions!!, 101)
    }

    // androidx.fragment.app.FragmentActivity, androidx.activity.ComponentActivity, android.app.Activity
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        if (requestCode == 101) {
            for (result in grantResults) {
                if (result == -1) {
                    Toast.makeText(this, R.string.permission_warning as Int, Toast.LENGTH_LONG)
                        .show()
                    setViewVisibility(R.string.permission_message)
                    checkPermission()
                    return
                }
            }
            setViewVisibility(R.id.tv_capture)
            setViewVisibility(R.id.surfaceView)
            setupSurfaceHolder()
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    private fun setViewVisibility(id: Int) {
        val view = findViewById<View>(id)
        if (view != null) {
            view.visibility = View.VISIBLE
        }
    }

    private fun setupSurfaceHolder() {
        cameraSource = CameraSource.Builder(this, detector).setFacing(1).setRequestedFps(2.0f)
            .setAutoFocusEnabled(true).build()
        val holder = surfaceView!!.holder
        surfaceHolder = holder
        holder.addCallback(this)
    }

    fun captureImage() {
        Handler(Looper.getMainLooper()).postDelayed({ runOnUiThread { clickImage() } }, 200L)
    }

    /* JADX INFO: Access modifiers changed from: private */
    fun clickImage() {
        val cameraSource = cameraSource
        cameraSource?.takePicture(null, this)
    }

    // android.view.SurfaceHolder.Callback
    override fun surfaceCreated(surfaceHolder: SurfaceHolder) {
        startCamera()
    }

    private fun startCamera() {
        try {
            if (ActivityCompat.checkSelfPermission(this, "android.permission.CAMERA") != 0) {
                return
            }
            cameraSource!!.start(surfaceHolder)
            detector!!.setProcessor(
                LargestFaceFocusingProcessor(
                    detector,
                    GraphicFaceTracker(this)
                )
            )
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    // android.view.SurfaceHolder.Callback
    override fun surfaceChanged(surfaceHolder: SurfaceHolder, i: Int, i1: Int, i2: Int) {}

    // android.view.SurfaceHolder.Callback
    override fun surfaceDestroyed(surfaceHolder: SurfaceHolder) {
        cameraSource!!.stop()
    }

    override fun onPictureTaken(bytes: ByteArray) {
        findViewById<View>(R.id.saveToGallery).visibility = View.VISIBLE
        Intent(this, IdentifyFace::class.java).also { newIt->
            startActivity(newIt)
            overridePendingTransition(R.anim.fadein_animation, R.anim.fadeout_animation)
            finish()
        }
    }

    private fun SaveImage(finalBitmap: Bitmap?) {
        val root = Environment.getExternalStorageDirectory().absolutePath
        val myDir = File("$root/DCIM/Camera")
        myDir.mkdirs()
        @SuppressLint("SimpleDateFormat") val timeStamp =
            SimpleDateFormat("yyyyMMdd_hhmmss").format(
                Date()
            )
        //        String fname = "Image-"+".jpg";
        val fname = timeStamp + "_b2scam.jpg"
        val file = File(myDir, fname)
        if (file.exists()) file.delete()
        try {
            val out = FileOutputStream(file)
            Log.d("failed_to_save", "Saved file")
            finalBitmap!!.compress(Bitmap.CompressFormat.JPEG, 90, out)
            out.flush()
            out.close()
            this.sendBroadcast(Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(file)))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    } //

    //    private void storeImage(Bitmap image) {
    //        File pictureFile = getOutputMediaFile();
    //        if (pictureFile == null) {
    //            Log.d("failed_to_save",
    //                    "Error creating media file, check storage permissions: ");// e.getMessage());
    //            return;
    //        }
    //        try {
    //            FileOutputStream fos = new FileOutputStream(pictureFile);
    //            Log.d("failed_to_save", "Saved file");
    //            image.compress(Bitmap.CompressFormat.PNG, 90, fos);
    //            fos.close();
    //        } catch (FileNotFoundException e) {
    //            Log.d("failed_to_save", "File not found: " + e.getMessage());
    //        } catch (IOException e) {
    //            Log.d("failed_to_save", "Error accessing file: " + e.getMessage());
    //        }
    //    }
    //    private  File getOutputMediaFile(){
    //        // To be safe, you should check that the SDCard is mounted
    //        // using Environment.getExternalStorageState() before doing this.
    //        File mediaStorageDir = new File(Environment.getExternalStorageDirectory()
    //                + "/Android/data/"
    //                + getApplicationContext().getPackageName()
    //                + "/Files");
    //
    //        // This location works best if you want the created images to be shared
    //        // between applications and persist after your app has been uninstalled.
    //
    //        // Create the storage directory if it does not exist
    //        if (! mediaStorageDir.exists()){
    //            if (! mediaStorageDir.mkdirs()){
    //                return null;
    //            }
    //        }
    //        // Create a media file name
    //        @SuppressLint("SimpleDateFormat") String timeStamp = new SimpleDateFormat("ddMMyyyy_HHmm").format(new Date());
    //        File mediaFile;
    //        String mImageName="MI_"+ timeStamp +".jpg";
    //        mediaFile = new File(mediaStorageDir.getPath() + File.separator + mImageName);
    //        return mediaFile;
    //    }
    companion object {
        const val CAMERA_REQUEST = 101
        var bitmap: Bitmap? = null
    }
}