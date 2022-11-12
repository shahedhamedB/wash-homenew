package com.washathomes.apputils.remote

import com.washathomes.model.stripe.IntentResponse
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*

interface StripeAPI {

    @FormUrlEncoded
    @POST("ephemeral_keys")
    fun createEphemeralKey(@FieldMap apiVersionMap: HashMap<String, String>): Call<ResponseBody>

    @FormUrlEncoded
    @POST("create_payment_intent")
    fun createPaymentIntent(@FieldMap params: HashMap<String, Any>): Call<IntentResponse>

}