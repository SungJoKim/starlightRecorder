package com.dream.encoder.format.type.audio

enum class AudioMimeType(val typeName: String) {
    AMR_NB("audio/3gpp"),
    AMR_WB("audio/amr-wb"),
    MPEG("audio/mpeg"),
    AAC("audio/mp4a-latm"),
    QCELP("audio/qcelp"),
    VORBIS("audio/vorbis"),
    OPUS("audio/opus"),
    G711_ALAW("audio/g711-alaw"),
    G711_MLAW("audio/g711-mlaw"),
    RAW("audio/raw"),
    FLAC("audio/flac"),
    MSGSM("audio/gsm"),
    AC3("audio/ac3"),
    EAC3("audio/eac3"),
    EAC3_JOC("audio/eac3-joc"),
    AC4("audio/ac4"),
    SCRAMBLED("audio/scrambled")
}