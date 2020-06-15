package com.dream.encoder.encoder.video

import java.nio.ByteBuffer

class VideoFrameData(
    val trackIndex: Int,
    val frameData: ByteBuffer,
    val presentationTimeUs: Long
)