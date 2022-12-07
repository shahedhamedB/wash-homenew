package com.washathomes

import android.app.Application
import androidx.databinding.library.BuildConfig
import com.paypal.checkout.PayPalCheckout
import com.paypal.checkout.config.CheckoutConfig
import com.paypal.checkout.config.Environment
import com.paypal.checkout.config.SettingsConfig
import com.paypal.checkout.createorder.CurrencyCode
import com.paypal.checkout.createorder.UserAction
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

@HiltAndroidApp
class WhashApp : Application() {
    val YOUR_CLIENT_ID = "AVq5k6xrbGYF5xyf3aWf6e4Nw-5En6A9cscPkRIWHMZK-iymZk0mDVxe-OmuG6S72YQJOkxvl8BII36q"
    val YOUR_CLIENT_ID_Live = "AXJJNjWsWVKJcqvHej47KyQwoxhPXRWrg4sjVK3wynuKbrmMNF1MqP_PO6ADlnqjRWxkSpyc9DTpnKRS"

    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
        val config = CheckoutConfig(
            application = this,
            clientId = YOUR_CLIENT_ID_Live,
            environment = Environment.LIVE,
            //"${com.washathomes.BuildConfig.APPLICATION_ID}://paypalpay",
            currencyCode = CurrencyCode.USD,
            userAction = UserAction.PAY_NOW,
            settingsConfig = SettingsConfig(
                loggingEnabled = true
            )
        )
        PayPalCheckout.setConfig(config)

    }
}