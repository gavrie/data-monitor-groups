package com.sunbit.datamonitor.utils

import mu.KotlinLogging
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.InputStream
import java.util.*

const val CONFIG_DIR = "/etc/kafka"

private val logger = KotlinLogging.logger {}

fun getStream(name: String, dir: String = CONFIG_DIR, failWhenNotFound: Boolean): InputStream {
    val filename = "$dir/$name"
    logger.debug("Going to read from $filename")
    try {
        return FileInputStream(filename)
    } catch (e: FileNotFoundException) {
        logger.debug("Could not read from $filename: ${e.message}")
    }

    val resource = "/$name"
    logger.debug("Falling back to reading from resource: $resource")
    return resourceStream(resource, failWhenNotFound)
}

fun loadProperties(name: String, dir: String = CONFIG_DIR): Properties {
    val props = Properties()
    getStream("$name.properties", dir, true).use {
        props.load(it)
    }
    getStream("$name-local.properties", dir, false).use {
        props.load(it)
    }
    return props
}