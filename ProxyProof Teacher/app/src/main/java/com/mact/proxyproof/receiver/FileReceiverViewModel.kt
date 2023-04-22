package com.mact.proxyproof.receiver

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.mact.proxyproof.Constants
import com.mact.proxyproof.models.FileTransfer
import com.mact.proxyproof.models.ViewState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.ObjectInputStream
import java.net.InetSocketAddress
import java.net.ServerSocket


class FileReceiverViewModel(context: Application) :
    AndroidViewModel(context) {

    private val _viewState = MutableSharedFlow<ViewState>()

    val viewState: SharedFlow<ViewState> = _viewState

    private val _log = MutableSharedFlow<String>()

    val log: SharedFlow<String> = _log

    private var job: Job? = null
    var serverSocket: ServerSocket? = null
    var clientInputStream: InputStream? = null
    var objectInputStream: ObjectInputStream? = null
    var fileOutputStream: FileOutputStream? = null

    fun startListener() {
        if (job != null) {
            return
        }
        job = viewModelScope.launch(context = Dispatchers.IO) {
            _viewState.emit(value = ViewState.Idle)


            try {
                _viewState.emit(value = ViewState.Connecting)
//                _log.emit(value = "Turn on Socket")

                serverSocket = ServerSocket()
                serverSocket!!.bind(InetSocketAddress(Constants.PORT))
                serverSocket!!.reuseAddress = true
                serverSocket!!.soTimeout = 30000

//                _log.emit(value = "socket accept，Disconnect if unsuccessful within thirty seconds")

                val client = serverSocket!!.accept()

                _viewState.emit(value = ViewState.Receiving)

                clientInputStream = client.getInputStream()
                objectInputStream = ObjectInputStream(clientInputStream)
                val fileTransfer = objectInputStream!!.readObject() as FileTransfer
                val dir = "/storage/emulated/0/Attendance/Imported"
                File(dir).mkdirs()
//                val file = File(getCacheDir(context = getApplication()), fileTransfer.fileName)
                val file = File(dir, fileTransfer.fileName)

                _log.emit(value = "Connection Succeeded")

//                _log.emit(value = "The file will be saved to: $file")

//                _log.emit(value = "Starting file transfer")


                fileOutputStream = FileOutputStream(file)
                val buffer = ByteArray(1024 * 512)
                while (true) {
                    val length = clientInputStream?.read(buffer)
                    if (length!! > 0) {
                        fileOutputStream!!.write(buffer, 0, length)
                    } else {
                        break
                    }
//                    _log.emit(value = "Transferring files,length :$length")

                }
                _viewState.emit(value = ViewState.Success(file = file))

                _log.emit(value = "File Received = ${fileTransfer.fileName}")

            } catch (e: Throwable) {
                _log.emit(value = "Error: " + e.message)

                _viewState.emit(value = ViewState.Failed(throwable = e))
            } finally {
                serverSocket?.close()
                clientInputStream?.close()
                objectInputStream?.close()
                fileOutputStream?.close()
            }
        }
        job?.invokeOnCompletion {
            job = null
        }
    }
    fun socketClose(){
        serverSocket?.close()
        clientInputStream?.close()
        objectInputStream?.close()
        fileOutputStream?.close()
        Log.d("currentUserAtLogin", "cancelled2")
    }
    private fun getCacheDir(context: Context): File {
        val cacheDir = File(context.cacheDir, "FileTransfer")
        cacheDir.mkdirs()
        return cacheDir
    }


}