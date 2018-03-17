package com.sunbit.datamonitor.consumerOffsets

// This code is based on https://github.com/chtefi/kafka-streams-consumer-offsets-to-json.
// The original code is in Scala, I translated it to Kotlin.

import kafka.coordinator.group.GroupMetadataManager
import kafka.coordinator.group.OffsetKey
import mu.KotlinLogging
import org.apache.kafka.clients.consumer.CommitFailedException
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.clients.consumer.OffsetAndMetadata
import org.apache.kafka.common.TopicPartition
import org.apache.kafka.common.serialization.ByteArrayDeserializer
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.streams.Consumed
import org.apache.kafka.streams.KeyValue
import org.apache.kafka.streams.StreamsBuilder
import java.nio.ByteBuffer
import java.util.*
import java.util.concurrent.TimeUnit

private const val INPUT_TOPIC = "__consumer_offsets"
private const val SCHEMAS_TOPIC = "schemas"

private val logger = KotlinLogging.logger {}

class ConsumerOffsets(props: Properties, groupId: String, private val monitorSchemas: Boolean) {
    private val consumer = KafkaConsumer<ByteArray, ByteArray>(
        Properties().apply {
            putAll(props)
            put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, ByteArrayDeserializer::class.java)
            put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ByteArrayDeserializer::class.java)

            put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false")
            put(ConsumerConfig.GROUP_ID_CONFIG, groupId)
        }
    )

    init {
        resetOffsets()
    }

    private fun baseKey(key: ByteArray) =
        GroupMetadataManager.readMessageKey(ByteBuffer.wrap(key))

    private fun isInputTopic(k: OffsetKey) =
        k.key().topicPartition().topic() == INPUT_TOPIC

    private fun toDetails(key: OffsetKey, value: ByteArray) = run {
        val topicPartition = key.key().topicPartition()

        if (topicPartition.topic() == INPUT_TOPIC) throw RuntimeException("Unexpected")

        KeyValue.pair(
            null,
            ConsumerOffsetDetails(
                key = key,
                value = GroupMetadataManager.readOffsetMessageValue(ByteBuffer.wrap(value))
            )
        )
    }

    private fun resetOffsets() {
        val topic = if (monitorSchemas) SCHEMAS_TOPIC else INPUT_TOPIC

        logger.info { "Going to reset offsets for topic $topic"}

        val partitions = consumer.partitionsFor(topic)

        val offsets = partitions.map {
            TopicPartition(topic, it.partition()) to OffsetAndMetadata(0)
        }.toMap()

        var attempts = 60
        while (true) {
            try {
                consumer.commitSync(offsets)
                break
            } catch (e: CommitFailedException) {
                logger.info { "Consumer group not ready, sleeping..." }
                TimeUnit.MILLISECONDS.sleep(500)
                if (attempts-- > 0) continue

                throw e
            }
        }

        logger.info { "Done." }
        return
    }

    fun setupStreams(builder: StreamsBuilder) {
        if (monitorSchemas) {
            builder.stream<String, String>(
                SCHEMAS_TOPIC,
                Consumed.with(Serdes.String(), Serdes.String())
            )
                .peek { k, v ->
                    logger.info { "Schema: $k => $v" }
                }
        } else {
            builder.stream<ByteArray, ByteArray>(
                INPUT_TOPIC,
                Consumed.with(Serdes.ByteArray(), Serdes.ByteArray())
            )
                .filterNot { _, v -> v == null }
                .map { k, v -> KeyValue.pair(baseKey(k), v) }
                .filter { k, _ -> k is OffsetKey }
                .map { k, v -> KeyValue.pair(k as OffsetKey, v) }
                .filterNot { k, _ -> isInputTopic(k) }
                .map { k, v -> toDetails(k, v) }
                .peek { _, details ->
                    logger.info { "ConsumerOffsetDetails: $details" }
                }

        }
    }
}
