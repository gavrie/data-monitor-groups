package com.sunbit.datamonitor

import com.sunbit.datamonitor.consumerOffsets.ConsumerOffsets
import com.sunbit.datamonitor.utils.loadProperties
import org.apache.kafka.streams.KafkaStreams
import org.apache.kafka.streams.StreamsBuilder
import org.apache.kafka.streams.StreamsConfig

const val APP_NAME = "data-monitor-groups"

fun main(args: Array<String>) {
    val props = loadProperties("config")

    props.apply {
        // Kafka Streams needs to consume the internal __consumer_offsets topic
        put("exclude.internal.topics", "false")
        put(StreamsConfig.APPLICATION_ID_CONFIG, APP_NAME)
    }

    val builder = StreamsBuilder()
    val offsets = ConsumerOffsets(props, groupId=APP_NAME)
    offsets.setupStreams(builder)

    val streams = KafkaStreams(builder.build(), props)
    streams.start()
}
