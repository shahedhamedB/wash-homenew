package com.washathomes.apputils.modules.payment.ephemeral

data class EphemeralResponse(
    val associated_objects: List<AssociatedObject>,
    val created: Int,
    val expires: Int,
    val id: String,
    val livemode: Boolean,
    val `object`: String,
    val secret: String
)