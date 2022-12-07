/*
 * Copyright 2017 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.washathomes.views.main.washee.checkout.payment.googlepayment

import com.google.android.gms.wallet.WalletConstants

object Constants {

    const val PAYMENTS_ENVIRONMENT = WalletConstants.ENVIRONMENT_TEST

    val SUPPORTED_NETWORKS = listOf(
        "AMEX",
        "DISCOVER",
        "JCB",
        "MASTERCARD",
        "VISA")


    val SUPPORTED_METHODS = listOf(
        "PAN_ONLY",
        "CRYPTOGRAM_3DS")

    const val COUNTRY_CODE = "US"

    const val CURRENCY_CODE = "USD"

    val SHIPPING_SUPPORTED_COUNTRIES = listOf("US", "GB")

    const val PAYMENT_GATEWAY_TOKENIZATION_NAME = "example"

    val PAYMENT_GATEWAY_TOKENIZATION_PARAMETERS = mapOf(
        "gateway" to PAYMENT_GATEWAY_TOKENIZATION_NAME,
        "gatewayMerchantId" to "exampleGatewayMerchantId"
    )

    const val DIRECT_TOKENIZATION_PUBLIC_KEY = "REPLACE_ME"

    val DIRECT_TOKENIZATION_PARAMETERS = mapOf(
        "protocolVersion" to "ECv1",
        "publicKey" to DIRECT_TOKENIZATION_PUBLIC_KEY
    )
    val URLSTRIPS = "https://api.stripe.com/v1/"
    const val STRIPSSECRITKEY="sk_test_51HqxCmGNvYMhrs1a7ExCpvTdnCCaEYVnTKxUBBZ59uFfhenzSK5kHw2feONhjGzxCsRdjRKAhX4PiZudBuVwXfCo00P5oArylZ"
    const val STRIPUPLISHTKEY="pk_test_51HqxCmGNvYMhrs1av0rfgi2UuVUxa5AEi4KPt7pD1tyWTd8O6Al0qPDTgSMdidKbcKTFJfANxczlvWxWrFT23WD200kTWhKq4G"
    const val STRIPSlIVE="pk_live_51HqxCmGNvYMhrs1aR40MglH7IH11khbzfL93y8fd39NhMuYVgdHx988swtWvWkAK2jFqzU4CqbjhyquHR3n4ms4q00toAXwS9g"
}