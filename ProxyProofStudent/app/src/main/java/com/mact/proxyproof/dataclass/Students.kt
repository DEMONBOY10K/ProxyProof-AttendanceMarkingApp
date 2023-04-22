package com.mact.proxyproof.dataclass

import android.graphics.Bitmap

data class Students(
    val fName: String? = null,
    val lName: String? = null,
    val email: String? = null,
    val schNum: String? = null,
    val age: String? = null,
    val semester: Int? = null,
    val password: String? = null,
    val img: Bitmap? = null
)
