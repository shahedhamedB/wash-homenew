package com.washathomes.apputils.payment

import androidx.annotation.Size
import com.washathomes.apputils.appdefs.Urls
import com.washathomes.apputils.remote.StripeAPI
import com.stripe.android.EphemeralKeyProvider
import com.stripe.android.EphemeralKeyUpdateListener
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * An implementation of [EphemeralKeyProvider] that can be used to generate
 * ephemeral keys on the backend.
 */
class ExampleEphemeralKeyProvider : EphemeralKeyProvider {



    override fun createEphemeralKey(
        @Size(min = 4) apiVersion: String,
        keyUpdateListener: EphemeralKeyUpdateListener
    ) {
        val retrofit: Retrofit = Retrofit.Builder().baseUrl(Urls.STRIPE_URL)
            .addConverterFactory(GsonConverterFactory.create()).build()
        val call: Call<ResponseBody> =
            retrofit.create(StripeAPI::class.java).createEphemeralKey(hashMapOf("stripe_version" to apiVersion))
        call.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(
                call: Call<ResponseBody>,
                response: retrofit2.Response<ResponseBody>
            ) {
                if (response.isSuccessful && response.body() != null) {
                    val ephemeralKeyJson = response.body()!!
                    keyUpdateListener.onKeyUpdate(ephemeralKeyJson.toString())
                } else {
                    keyUpdateListener.onKeyUpdateFailure(0, "")
                }
            }

            override fun onFailure(call: Call<ResponseBody>?, t: Throwable?) {
                keyUpdateListener.onKeyUpdateFailure(0, t?.message ?: "")
            }
        })
    }
}
