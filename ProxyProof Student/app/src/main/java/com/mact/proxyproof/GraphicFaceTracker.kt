package com.mact.proxyproof

import android.util.Log
import com.google.android.gms.vision.Detector.Detections
import com.google.android.gms.vision.Tracker
import com.google.android.gms.vision.face.Face

/* loaded from: classes4.dex */
class GraphicFaceTracker     /* JADX INFO: Access modifiers changed from: package-private */(private val cameraActivity: CameraActivity) :
    Tracker<Face>() {
    private var state = 0
    private fun blink(value: Float) {
        when (state) {
            0 -> {
                if (value > OPEN_THRESHOLD) {
                    state = 1
                    return
                }
                return
            }
            1 -> {
                if (value < CLOSE_THRESHOLD) {
                    state = 2
                    return
                }
                return
            }
            2 -> {
                if (value > OPEN_THRESHOLD) {
                    Log.i("Camera Demo", "blink has occurred!")
                    state = 0
                    cameraActivity.captureImage()
                    return
                }
                return
            }
            else -> return
        }
    }

    // com.google.android.gms.vision.Tracker
    override fun onUpdate(detectionResults: Detections<Face>, face: Face) {
        val left = face.isLeftEyeOpenProbability
        val right = face.isRightEyeOpenProbability
        if (left == -1.0f || right == -1.0f) {
            return
        }
        val value = Math.min(left, right)
        blink(value)
    }

    companion object {
        private const val CLOSE_THRESHOLD = 0.4f
        private const val OPEN_THRESHOLD = 0.85f
    }
}