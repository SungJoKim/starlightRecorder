package com.dream.encoder.muxer

import android.media.MediaCodec
import android.media.MediaFormat
import android.media.MediaMuxer
import com.dream.encoder.format.StarlightMediaFormat
import java.nio.ByteBuffer
import java.util.concurrent.locks.ReentrantLock

class StarlightMuxer(
    filePath: String,
    mediaFormat: Int = MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4
) {
    private var startTimeUs: Long = 0L
    private var preventAudioPresentationTimeUs: Long = 0L
    private var isStarted: Boolean = false
    private val mediaMuxer: MediaMuxer = MediaMuxer(filePath, mediaFormat)
    private val lock = ReentrantLock()

    fun start() {
        synchronized(lock) {
            mediaMuxer.start()
            isStarted = true
        }
    }

    fun stop() {
        synchronized(lock) {
            mediaMuxer.stop()
            mediaMuxer.release()
            isStarted = false
        }
    }

    fun addTrack(format: StarlightMediaFormat): Int {
        synchronized(lock) {
            if (isStarted) {
                throw IllegalStateException("muxer already started")
            }
            return mediaMuxer.addTrack(format.mediaFormat)
        }
    }

    /**
     * write encoded data to muxer
     *
     * @param trackIndex
     * @param byteBuf
     * @param bufferInfo
     */
    suspend fun writeSampleData(trackIndex: Int, byteBuf: ByteBuffer, bufferInfo: MediaCodec.BufferInfo) {
        synchronized(lock) {
            if (!isStarted) {
                throw IllegalStateException("muxer not started")
            }

            if (startTimeUs == 0L) {
                startTimeUs = bufferInfo.presentationTimeUs
            }

            if (preventAudioPresentationTimeUs < bufferInfo.presentationTimeUs) {
                mediaMuxer.writeSampleData(trackIndex, byteBuf, bufferInfo)
                preventAudioPresentationTimeUs = bufferInfo.presentationTimeUs

            }
        }
    }
}