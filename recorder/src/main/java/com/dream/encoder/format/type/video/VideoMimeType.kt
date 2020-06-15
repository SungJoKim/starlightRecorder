package com.dream.encoder.format.type.video

enum class VideoMimeType(val typeName: String) {
    VP8("video/x-vnd.on2.vp8"),
    VP9("video/x-vnd.on2.vp9"),
    AV1("video/av01"),
    AVC("video/avc"),
    HEVC("video/hevc"),
    MPEG4("video/mp4v-es"),
    H263("video/3gpp"),
    MPEG2("video/mpeg2"),
    RAW("video/raw"),
    DOLBY_VISION("video/dolby-vision"),
    SCRAMBLED("video/scrambled")
}