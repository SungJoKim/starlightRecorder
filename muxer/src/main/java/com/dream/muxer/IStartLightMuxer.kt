package com.dream.muxer

import android.graphics.Bitmap

interface IStartLightMuxer {
    fun addFrame(streamId: Int, frameData: FrameData)
}