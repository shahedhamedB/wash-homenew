package com.washathomes.model.stripe

/**
 * Created on 2019-11-07.
 */
data class IntentData(
    val amount: Int,
    val amount_capturable: Int,
    val amount_received: Int,
    val application: Any,
    val application_fee_amount: Any,
    val canceled_at: Any,
    val cancellation_reason: Any,
    val capture_method: String,
    val charges: Charges,
    val client_secret: String,
    val confirmation_method: String,
    val created: Int,
    val currency: String,
    val customer: String,
    val description: Any,
    val id: String,
    val invoice: Any,
    val last_payment_error: Any,
    val livemode: Boolean,
//    val metadata: List<Any>,
    val next_action: NextAction,
    val `object`: String,
    val on_behalf_of: Any,
    val payment_method: String,
    val payment_method_options: PaymentMethodOptions,
    val payment_method_types: List<String>,
    val receipt_email: Any,
    val review: Any,
    val setup_future_usage: Any,
    val shipping: Any,
    val source: Any,
    val statement_descriptor: Any,
    val statement_descriptor_suffix: Any,
    val status: String,
    val transfer_data: Any,
    val transfer_group: Any
)

data class Charges(
    val `data`: List<Any>,
    val has_more: Boolean,
    val `object`: String,
    val total_count: Int,
    val url: String
)

data class NextAction(
    val type: String,
    val use_stripe_sdk: UseStripeSdk
)

data class UseStripeSdk(
    val known_frame_issues: String,
    val stripe_js: String,
    val type: String
)

data class PaymentMethodOptions(
    val card: Card
)

data class Card(
    val installments: Any,
    val request_three_d_secure: String
)