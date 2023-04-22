package com.mact.proxyproof.sender

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaScannerConnection
import android.net.Uri
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.telephony.TelephonyManager
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.google.firebase.auth.FirebaseAuth
import com.mact.proxyproof.BaseActivity
import com.mact.proxyproof.LoginActivity
import com.mact.proxyproof.R
import com.mact.proxyproof.facemodel.IdentifyFace
import com.mact.proxyproof.models.ViewState
import kotlinx.coroutines.launch
import java.io.*
import kotlin.jvm.internal.Intrinsics


class FileSenderActivity : BaseActivity(){

    private val fileSenderViewModel by viewModels<FileSenderViewModel>()
    private var scholar: String? = null
    private var imei: String? = null
    var userName: String? = null
    private var user: FirebaseAuth? = null

//    private val getContentLaunch = registerForActivityResult(
//        ActivityResultContracts.GetContent()
//    ) { imageUri ->
//        if (imageUri != null) {
//
//            val file =File("/sdcard/Attendance/191112408.txt")
//            val uri = Uri.fromFile(file)
//            Log.d("uri",uri.toString())
//            MediaScannerConnection.scanFile(
//                this, arrayOf(file.absolutePath), null
//            ) { path: String?, uri: Uri ->
//                fileSenderViewModel.send(
//                    ipAddress = getHotspotIpAddress(
//                        context = applicationContext
//                    ),
//                    fileUri = uri
//                )
//                Log.d(
//                    "uri",
//                    uri.toString()
//                )
//
//            }
//
////            Log.d("uri", imageUri.toString() + "\n"+uri1.toString());
//        }
//    }

    private val btnChooseFile by lazy {
        findViewById<Button>(R.id.btnSendFile)
    }

    private val tvState by lazy {
        findViewById<TextView>(R.id.tvSendState)
    }
    fun tvState(State:String){
        tvState.text = State
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_file_sender)
        supportActionBar?.title = "file sender"
        btnChooseFile.isEnabled = false
        user = FirebaseAuth.getInstance()
        if (user!!.getCurrentUser() != null) {
            if (user!!.getCurrentUser()!!.isEmailVerified) {
                Log.d("currentUserAtLogin", user!!.getCurrentUser()!!.email + " has logged in")
                val url = getString(R.string.firebase_db_location)
                //					database = FirebaseDatabase.getInstance(url).getReference("users");
                val email = user!!.currentUser!!.email
                userName = emailToUserName(email!!)
                ReadBtn()
                Log.d("currentUserAtLogin", "$userName has logged in")
                Log.d("currentUserAtLogin", "$scholar has logged in")
                val handler = Handler(Looper.getMainLooper())
                handler.postDelayed({ //Do something after 100ms
                    createTxt()
                    btnChooseFile.isEnabled = true
                }, 1000)
            }
        }
        btnChooseFile.setOnClickListener {
//            getContentLaunch.launch("text/plain")
            val file =File("/sdcard/Attendance/Imported/$scholar.txt")
            if(!file.exists()){
                Toast.makeText(this.baseContext, "Scholar No. not found , Restarting App!!", Toast.LENGTH_SHORT).show()
                Intent(this, IdentifyFace::class.java).also {
                    startActivity(it)
                    overridePendingTransition(R.anim.fadein_animation, R.anim.fadeout_animation)
                    finish()
                }
            }
            else{
                MediaScannerConnection.scanFile(
                    this, arrayOf(file.absolutePath), null
                ) { path: String?, uri: Uri ->
                    fileSenderViewModel.send(
                        ipAddress = getHotspotIpAddress(
                            context = applicationContext
                        ),
                        fileUri = uri,scholar!!
                    )
                    Log.d("uri", uri.toString())
                }
            }

        }
        initEvent()
    }
    private fun emailToUserName(email: String): String? {
        val regex = Regex("[^A-Za-z0-9]")
        return regex.replace((email as CharSequence), "")
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
//                  EditText var10000 = this.textmsg;
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
    fun createTxt() {
        Log.d("current1", "inside txt")
        val dir1 = "/storage/emulated/0/Attendance/Imported"
        Log.d("current1", dir1)
        try {
            val dir = "/storage/emulated/0/Attendance/Imported"

            val saveFile = File(dir + File.separator + scholar + ".txt")
            File(dir).mkdirs()
            val fileOut = FileOutputStream(saveFile)
            imei = getIMEIDeviceId(this)
            Log.d("current1", this.imei.toString())
            Log.d("current1", dir)
            val outputWriter = OutputStreamWriter(fileOut)
            outputWriter.write("Device ID : " + this.imei)
            outputWriter.close()
            Toast.makeText(this.baseContext, "File saved successfully!", Toast.LENGTH_SHORT).show()
        } catch (var3: Exception) {
            var3.printStackTrace()
            Log.d("current1", "Exception txt")
        }
    }
    @SuppressLint("HardwareIds")
    fun getIMEIDeviceId(context: Context): String {
        Intrinsics.checkNotNullParameter(context, "context")
        val deviceId: String
        val var10000: String
        if (Build.VERSION.SDK_INT >= 29) {
            var10000 = Settings.Secure.getString(context.contentResolver, "android_id")
            Intrinsics.checkNotNullExpressionValue(
                var10000,
                "Settings.Secure.getStrin…ttings.Secure.ANDROID_ID)"
            )
        } else {
            val var6 = context.getSystemService(TELEPHONY_SERVICE)
                ?: throw NullPointerException("null cannot be cast to non-null type android.telephony.TelephonyManager")
            val mTelephony = var6 as TelephonyManager
            if (Build.VERSION.SDK_INT >= 23 && context.checkSelfPermission("android.permission.READ_PHONE_STATE") != PackageManager.PERMISSION_GRANTED) {
                return ""
            }
            var10000 =
                if (mTelephony.deviceId != null) if (Build.VERSION.SDK_INT >= 26) mTelephony.imei else mTelephony.deviceId else Settings.Secure.getString(
                    context.contentResolver,
                    "android_id"
                )
            Intrinsics.checkNotNullExpressionValue(
                var10000,
                "if (mTelephony.deviceId …ANDROID_ID)\n            }"
            )
        }
        deviceId = var10000
        Log.d("deviceId", deviceId)
        return deviceId
    }
    private fun initEvent() {
        lifecycleScope.launch {
            fileSenderViewModel.viewState.collect {
                when (it) {
                    ViewState.Idle -> {
                        tvState.text = ""
                        dismissLoadingDialog()
                    }

                    ViewState.Connecting -> {
                        showLoadingDialog()
                    }

                    is ViewState.Receiving -> {
                        showLoadingDialog()
                    }

                    is ViewState.Success -> {
                        dismissLoadingDialog()
                    }

                    is ViewState.Failed -> {
                        dismissLoadingDialog()
                    }
                }
            }
        }
        lifecycleScope.launch {
            fileSenderViewModel.log.collect {
                tvState.append(it)
                tvState.append("\n\n")
            }
        }
    }

    private fun getHotspotIpAddress(context: Context): String {
        val wifiManager =
            context.applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager
        val wifiInfo = wifiManager?.connectionInfo
        if (wifiInfo != null) {
            val dhcpInfo = wifiManager.dhcpInfo
            if (dhcpInfo != null) {
                val address = dhcpInfo.gateway
                return ((address and 0xFF).toString() + "." + (address shr 8 and 0xFF)
                        + "." + (address shr 16 and 0xFF)
                        + "." + (address shr 24 and 0xFF))
            }
        }
        return ""
    }

}