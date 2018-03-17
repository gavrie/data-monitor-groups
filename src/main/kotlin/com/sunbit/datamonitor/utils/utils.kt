package com.sunbit.datamonitor.utils

import java.io.InputStream

// Create an object so we can use a ClassLoader from a top level function.
internal object Resources

internal class EmptyInputStream : InputStream() {
    override fun available(): Int {
        return 0
    }

    override fun read(): Int {
        return -1
    }
}

fun resourceStream(name: String, failWhenNotFound: Boolean = true) = Resources.javaClass.getResourceAsStream(name) ?:
        if (!failWhenNotFound) EmptyInputStream() else throw IllegalArgumentException("No such file: $name")



