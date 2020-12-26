package com.mv.engine

import android.support.annotation.Keep


@Keep
data class FaceBox(
    val left: Int,
    val top: Int,
    val right: Int,
    val bottom: Int,
    var confidence: Float
)