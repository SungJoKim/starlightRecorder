package com.dream.encoder

import android.graphics.Bitmap

interface IStartLightEncoder {
    fun encode(bitmap: Bitmap): ByteArray
}