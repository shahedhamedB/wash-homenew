package com.washathomes.model.stripe

/**
 * Created on 2019-11-07.
 */
data class IntentResponse(
    val messages: List<String>,
    val response: IntentData
)