package com.washathomes.util.payment

import android.content.Context
import com.washathomes.apputils.appdefs.Urls
import com.washathomes.views.main.washee.WasheeMainActivity.Companion.getAppContext
import com.stripe.android.PaymentConfiguration
import com.stripe.android.Stripe

class StripeClient(val context: Context) {

    var token: String = ""
    var stripe: Stripe = Stripe(context, PaymentConfiguration.getInstance(context).publishableKey)

    companion object {
        private val applicationContext = getAppContext() as Context
        val BASE_URL =
            Urls.BASE_URL + "/stripe_auth/"//"https://stripe-test-vma.herokuapp.com"
        const val STRIPE_ACCOUNT_ID = "acct_1HqxCmGNvYMhrs1a"
        const val RETURN_URL = "stripe://payment_auth"
    }

}