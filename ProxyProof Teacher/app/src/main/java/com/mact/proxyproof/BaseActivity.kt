package com.mact.proxyproof

import android.app.Activity
import android.app.ProgressDialog
import android.content.DialogInterface
import android.content.Intent
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.mact.proxyproof.receiver.FileReceiverViewModel


open class BaseActivity : AppCompatActivity() {

    private var loadingDialog: ProgressDialog? = null
    private val fileReceiverViewModel by viewModels<FileReceiverViewModel>()
    protected fun showLoadingDialog() {
        loadingDialog?.dismiss()
        loadingDialog = ProgressDialog(this).apply {
            setProgressStyle(ProgressDialog.BUTTON_POSITIVE)
            setCancelable(false)
            setCanceledOnTouchOutside(false)
            setMessage("Searching for Sender...")
            setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel",
                DialogInterface.OnClickListener { dialog, which ->
                    loadingDialog?.dismiss() //dismiss dialog
                    fileReceiverViewModel.socketClose()
                    Log.d("currentUserAtLogin", "cancelled")

                })
            show()
        }
    }

    protected fun dismissLoadingDialog() {
        loadingDialog?.dismiss()
    }

    protected fun <T : Activity> startActivity(clazz: Class<T>) {
        startActivity(Intent(this, clazz))
    }

}