package com.dream.muxer

sealed class FrameData {
    class AudioFrameData (
        val timeStamp: Long,
        val data: ByteArray
    )
    class VideoFrameData(
        val timeStamp: Long,
        val data: ByteArray
    )
}