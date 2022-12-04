package com.washathomes.apputils.modules.payment.strips

data class StripsResponse(
    val companyName: String,
    val customer: String,
    val ephemeralKey: String,
    val paymentIntent: String,
    val publishableKey: String,
    val payment_id_to_charge:String
)