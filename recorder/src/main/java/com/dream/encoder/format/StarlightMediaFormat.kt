package com.dream.encoder.format

import android.media.MediaFormat
import com.dream.encoder.format.type.audio.AudioMimeType
import com.dream.encoder.format.type.video.VideoMimeType

class StarlightMediaFormat private constructor(
    val mediaFormat: MediaFormat
) {

    companion object {
        fun createVideoFormat(
            videoMimeType: VideoMimeType,
            width: Int,
            height: Int
        ): StarlightMediaFormat {
            return StarlightMediaFormat(
                MediaFormat.createVideoFormat(
                    videoMimeType.typeName,
                    width,
                    height
                )
            )
        }

        fun createAudioFormat(
            audioMimeType: AudioMimeType,
            sampleRate: Int,
            channelCount: Int
        ): StarlightMediaFormat {
            return StarlightMediaFormat(
                MediaFormat.createAudioFormat(
                    audioMimeType.typeName,
                    sampleRate,
                    channelCount
                )
            )
        }
    }
}