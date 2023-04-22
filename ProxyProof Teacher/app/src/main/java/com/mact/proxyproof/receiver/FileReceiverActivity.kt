package com.mact.proxyproof.receiver

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.mact.proxyproof.BaseActivity
import com.mact.proxyproof.ExportActivity
import com.mact.proxyproof.R
import com.mact.proxyproof.explorer.MainActivity
import com.mact.proxyproof.fragments.InstructionsDialog
import com.mact.proxyproof.models.ViewState
import com.mact.proxyproof.schedule.ScheduleActivity

import kotlinx.android.synthetic.main.activity_file_receiver.*

import kotlinx.coroutines.launch


class FileReceiverActivity : BaseActivity() {

    private val fileReceiverViewModel by viewModels<FileReceiverViewModel>()

    private val tvState by lazy {
        findViewById<TextView>(R.id.tvReceiveState)
    }

    private val btnStartReceive by lazy {
        findViewById<Button>(R.id.btnStartReceive)
    }
    private var attendanceCount :Int = 0
    private  var fileNames : MutableList<String> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_file_receiver)
        supportActionBar?.title = "file receiver"

        val instructionDialog = InstructionsDialog()
        btnInstruction.setOnClickListener {
            instructionDialog.show(this.supportFragmentManager,"customDialogInstruction")
            instructionDialog.isCancelable = true
        }
        btnStartReceive.setOnClickListener {
            tvState.text = ""
            fileReceiverViewModel.startListener()
            tvAttendance.visibility = View.VISIBLE
            tvAttendanceCount.visibility = View.VISIBLE
            tvAttendanceCount.text = attendanceCount.toString()
        }
        btnExportAttendance?.setOnClickListener{
            fileReceiverViewModel.socketClose()
            Intent(this, ExportActivity::class.java).also { newIt->
                startActivity(newIt)
                overridePendingTransition(R.anim.fadein_animation, R.anim.fadeout_animation)
//                finish()
            }
        }
        btnExplorer?.setOnClickListener{
            fileReceiverViewModel.socketClose()
            Intent(this, MainActivity::class.java).also { newIt->
                startActivity(newIt)
                overridePendingTransition(R.anim.fadein_animation, R.anim.fadeout_animation)
//                finish()
            }
        }
        btnSchedule?.setOnClickListener{
            fileReceiverViewModel.socketClose()
            Intent(this, ScheduleActivity::class.java).also { newIt->
                startActivity(newIt)
                overridePendingTransition(R.anim.fadein_animation, R.anim.fadeout_animation)
//                finish()
            }
        }
        initEvent()
    }

    private fun initEvent() {
        lifecycleScope.launch {
            fileReceiverViewModel.viewState.collect {
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
                        attendanceCount += 1
                        tvAttendanceCount.text = attendanceCount.toString()
                        Log.d("Success", it.toString())
                        val handler = Handler(Looper.getMainLooper())
                        handler.postDelayed({
                            btnStartReceive.performClick()
                        }, 1000)

                    }
                    is ViewState.Failed -> {
                        dismissLoadingDialog()
                    }
                }
            }
        }
        lifecycleScope.launch {
            fileReceiverViewModel.log.collect {
                tvState.append(it)
                tvState.append("\n\n")
                Log.d("",it)
                Log.d("",it.substring(0,15))
                if(it.substring(0,15) == "File Received ="){
                    fileNames.add(it.substring(16))
                    Log.d("", fileNames.toString())
                }
            }
        }
    }

}