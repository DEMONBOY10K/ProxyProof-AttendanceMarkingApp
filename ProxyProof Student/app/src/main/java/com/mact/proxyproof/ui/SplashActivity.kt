package com.mact.proxyproof.ui;

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.mact.proxyproof.*


class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        val timer: Thread = object : Thread() {
            override fun run() {
                try {
                    sleep(1000) // 4 second wait
                } catch (e: InterruptedException) {
                    Log.d("ERROR", e.toString())
                    currentThread().interrupt()
                } finally {
                    applicationInitialize()
                }
            }
        }
        timer.start()
    }

    //  Checking either logged in or a new user
    private fun applicationInitialize() {
        val intent = Intent(this@SplashActivity, PermissionActivity::class.java)
        startActivity(intent)
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
        finish()
    }
}
