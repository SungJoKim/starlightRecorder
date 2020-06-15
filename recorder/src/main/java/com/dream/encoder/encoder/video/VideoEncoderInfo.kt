package com.dream.encoder.encoder.video

import android.media.MediaCodecInfo
import com.dream.encoder.format.type.video.VideoMimeType

class VideoEncoderInfo(
    val width: Int,
    val height: Int,
    val mimeType: VideoMimeType = VideoMimeType.AVC,
    val colorFormat: Int = MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Flexible,
    val frameRate: Int = 30,
    val iFrameInterval: Int = 3,
    val bitRate: Int = (width * height * frameRate * 0.20).toInt()
)