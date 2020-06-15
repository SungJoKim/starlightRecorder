package com.dream.starlightrecoder.record

import android.media.MediaFormat
import androidx.camera.core.ImageProxy
import com.dream.encoder.encoder.StarlightVideoEncoder
import com.dream.encoder.encoder.video.VideoEncoderInfo
import com.dream.encoder.encoder.video.VideoFrameData
import com.dream.encoder.format.StarlightMediaFormat
import com.dream.encoder.format.type.video.VideoMimeType
import com.dream.encoder.muxer.StarlightMuxer
import com.dream.starlightrecoder.utils.ext.toSingleByteArray
import kotlin.properties.Delegates

class RecordProcessor(
    private val starlightMuxer: StarlightMuxer
)
{
    var isStarted = false
    private var isEos = false
    private var trackIndex = -1
    private var startTimestamp = 0L
    private lateinit var videoEncoder: StarlightVideoEncoder

    fun start() {
        isStarted = true
        isEos = false
    }

    fun stop() {
        videoEncoder.stop()
        isStarted = false
    }

    fun addVideoTrack(width: Int, height: Int) {
        trackIndex = starlightMuxer.addTrack(
            StarlightMediaFormat.createVideoFormat(
                VideoMimeType.AVC,
                width,
                height
            )
        )
    }

    suspend fun addVideoFrame(imageProxy: ImageProxy) {
        if (trackIndex < 0) {
            addVideoTrack(imageProxy.width, imageProxy.height)
            starlightMuxer.start()
            startTimestamp = imageProxy.imageInfo.timestamp
        }
        if (!::videoEncoder.isInitialized) {
            videoEncoder = StarlightVideoEncoder(
                VideoEncoderInfo(
                    imageProxy.width,
                    imageProxy.height
                ),
                starlightMuxer
            ).apply {
                prepare()
            }
        }

        if (!isEos) {
            if (!isStarted) {
                isEos = true
            }
            videoEncoder.encode(
                VideoFrameData(
                    trackIndex,
                    imageProxy.toSingleByteArray(),
                    getVideoTimeStamp(imageProxy.imageInfo.timestamp)
                ),
                isEos
            ) {
                starlightMuxer.stop()
            }
        }
    }

    fun addAudioFrame() {

    }

    fun getVideoTimeStamp(newTimeStamp: Long): Long {
        return (newTimeStamp - startTimestamp)
    }
}