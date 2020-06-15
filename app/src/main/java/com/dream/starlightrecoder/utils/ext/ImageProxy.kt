package com.dream.starlightrecoder.utils.ext

import androidx.camera.core.ImageProxy
import java.nio.ByteBuffer

fun ImageProxy.toSingleByteArray(): ByteBuffer {
    val size = planes.sumBy {
        it.buffer.capacity()
    }
    val newBuffer = ByteBuffer.allocate(size)
    planes.forEach {
        newBuffer.put(it.buffer)
    }
    return newBuffer
}