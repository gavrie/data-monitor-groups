package com.sunbit.datamonitor.consumerOffsets

import kafka.common.OffsetAndMetadata
import kafka.coordinator.group.OffsetKey
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

data class ConsumerOffsetDetails(
    val group: String,
    val topic: String,
    val partition: Int,
    val offset: Long?,
    val version: Short,
    val metadata: String?,
    val commitTimestamp: Long?,
    val expireTimestamp: Long?
) {
    constructor(key: OffsetKey, value: OffsetAndMetadata?) : this(
        group = key.key().group(),
        topic = key.key().topicPartition().topic(),
        partition = key.key().topicPartition().partition(),
        offset = value?.offset(),
        version = key.version(),
        metadata = value?.metadata(),
        commitTimestamp = value?.commitTimestamp(),
        expireTimestamp = value?.expireTimestamp()
    )
}
