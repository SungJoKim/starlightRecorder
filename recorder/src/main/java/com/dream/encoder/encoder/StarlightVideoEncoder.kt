package com.dream.encoder.encoder

import android.media.*
import android.media.MediaCodec.BUFFER_FLAG_END_OF_STREAM
import android.util.Log
import com.dream.encoder.encoder.video.VideoEncoderInfo
import com.dream.encoder.encoder.video.VideoFrameData
import com.dream.encoder.muxer.StarlightMuxer

class StarlightVideoEncoder(
    private val videoEncoderInfo: VideoEncoderInfo,
    private val muxer: StarlightMuxer
) {

    private lateinit var mediaCodec: MediaCodec

    companion object {
        private const val TAG = "startlightMediaEncoder"
        private const val TIMEOUT_USEC = 10000L    // 10[msec]
    }

    fun prepare() {
        val videoMimeType = videoEncoderInfo.mimeType.typeName
        val videoCodecInfo = selectVideoCodec(videoMimeType)

        if (videoCodecInfo == null) {
            Log.e(TAG, "Unable to find an appropriate codec for $videoMimeType")
            return
        }
        Log.i(TAG, "selected codec: " + videoCodecInfo.name)

        val format = MediaFormat.createVideoFormat(
            videoMimeType,
            videoEncoderInfo.width,
            videoEncoderInfo.height
        )
        format.setInteger(MediaFormat.KEY_COLOR_FORMAT, videoEncoderInfo.colorFormat)
        format.setInteger(MediaFormat.KEY_BIT_RATE, videoEncoderInfo.bitRate)
        format.setInteger(MediaFormat.KEY_FRAME_RATE, videoEncoderInfo.frameRate)
        format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, videoEncoderInfo.iFrameInterval)
//        Log.i(TAG, "format: $format")

        mediaCodec = MediaCodec.createEncoderByType(videoMimeType)
        mediaCodec.let {
            it.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
            // get Surface for encoder input
            // this method only can call between #configure and #start
            it.start()
            Log.i(TAG, "prepare finishing")
        }
    }

    fun stop() {
        mediaCodec.stop()
        mediaCodec.release()
    }

    /**
     * select the first codec that match a specific MIME type
     *
     * @param mimeType
     * @return null if no codec matched
     */
    private fun selectVideoCodec(mimeType: String): MediaCodecInfo? {
//        Log.v(TAG, "selectVideoCodec:")

        // get the list of available codecs
        val list = MediaCodecList(MediaCodecList.ALL_CODECS).codecInfos
        val numCodecs = list
        for (codecInfo in list) {
            if (!codecInfo.isEncoder) {    // skipp decoder
                continue
            }
            for (type in codecInfo.supportedTypes) {
                if (type.equals(mimeType, ignoreCase = true)) {
//                    Log.i(TAG, "codec:" + codecInfo.name + ",MIME=" + types[j])
                    val format = selectColorFormat(codecInfo, mimeType)
                    if (format > 0) {
                        return codecInfo
                    }
                }
            }
        }
        return null
    }


    suspend fun encode(videoFrameData: VideoFrameData, isEos: Boolean, eosListener: ((Boolean) -> Unit)? = null): Boolean {
        val inputBufferId = mediaCodec.dequeueInputBuffer(TIMEOUT_USEC)
        if (inputBufferId >= 0) {
            val inputBuffer = mediaCodec.getInputBuffer(inputBufferId) ?: return false
            // fill inputBuffer with valid data
            inputBuffer.put(videoFrameData.frameData)
            mediaCodec.queueInputBuffer(
                inputBufferId,
                0,
                inputBuffer.capacity(),
                videoFrameData.presentationTimeUs,
                if (isEos) BUFFER_FLAG_END_OF_STREAM else 0
            )
        }
        if (!isEos) {
            outputEncodedData(videoFrameData.trackIndex)
        } else {
            var loop = true
            while (loop) {
                outputEncodedData(videoFrameData.trackIndex) {
                    loop = false
                    eosListener?.invoke(true)
                }
            }
        }
        return true
    }

    suspend fun outputEncodedData(trackIndex: Int, eosListener: ((Boolean) -> Unit)? = null) {
        val bufferInfo = MediaCodec.BufferInfo()
        val outputBufferId = mediaCodec.dequeueOutputBuffer(bufferInfo, TIMEOUT_USEC)
        if (outputBufferId >= 0) {
            val outputBuffer = mediaCodec.getOutputBuffer(outputBufferId)
            val bufferFormat = mediaCodec.getOutputFormat(outputBufferId)
            outputBuffer?.let {
                muxer.writeSampleData(trackIndex, outputBuffer, bufferInfo)
                mediaCodec.releaseOutputBuffer(outputBufferId, false)
            }

            if (bufferInfo.flags.and(BUFFER_FLAG_END_OF_STREAM) != 0) {
                eosListener?.invoke(true)
            }
        } else if (outputBufferId == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
            val outputFormat = mediaCodec.outputFormat
//            trackIndex = muxer.addTrack(outputFormat)
        }
    }

    /**
     * select the first codec that match a specific MIME type
     *
     * @param mimeType
     * @return null if no codec matched
     */
    private fun selectVideoEncoder(mimeType: String): MediaCodecInfo? {
        // get the list of available codecs
        val list = MediaCodecList(MediaCodecList.ALL_CODECS)
        val codecInfos = list.codecInfos

        val numCodecs = codecInfos.size
        for (i in 0 until numCodecs) {
            val codecInfo = codecInfos[i]

            if (!codecInfo.isEncoder) {    // skipp decoder
                continue
            }
            // select first codec that match a specific MIME type and color format
            val types = codecInfo.supportedTypes
            for (j in types.indices) {
                if (types[j].equals(mimeType, ignoreCase = true)) {
                    Log.i(TAG, "codec:" + codecInfo.name + ",MIME=" + types[j])
                    val format = selectColorFormat(codecInfo, mimeType)
                    if (format > 0) {
                        return codecInfo
                    }
                }
            }
        }
        return null
    }

    /**
     * select color format available on specific codec and we can use.
     *
     * @return 0 if no colorFormat is matched
     */
    private fun selectColorFormat(codecInfo: MediaCodecInfo, mimeType: String): Int {
        Log.i(TAG, "selectColorFormat: ")
        var result = 0
        val caps: MediaCodecInfo.CodecCapabilities
        try {
            Thread.currentThread()
                .priority = Thread.MAX_PRIORITY
            caps = codecInfo.getCapabilitiesForType(mimeType)
        } finally {
            Thread.currentThread()
                .priority = Thread.NORM_PRIORITY
        }
        var colorFormat: Int
        for (i in caps.colorFormats.indices) {
            colorFormat = caps.colorFormats[i]
            if (isRecognizedViewoFormat(colorFormat)) {
                if (result == 0)
                    result = colorFormat
                break
            }
        }
        if (result == 0)
            Log.e(TAG, "couldn't find a good color format for " + codecInfo.name + " / " + mimeType)
        return result
    }

    private fun isRecognizedViewoFormat(colorFormat: Int): Boolean {
        return colorFormat == videoEncoderInfo.colorFormat
    }
}