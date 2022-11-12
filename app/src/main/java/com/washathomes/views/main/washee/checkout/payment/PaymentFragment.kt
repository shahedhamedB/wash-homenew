package com.washathomes.views.main.washee.checkout.payment

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.wallet.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.paypal.android.sdk.payments.*
import com.stripe.android.*
import com.stripe.android.model.*
import com.stripe.android.view.BillingAddressFields
import com.stripe.android.view.PaymentMethodsActivityStarter
import com.washathomes.apputils.appdefs.AppDefs
import com.washathomes.apputils.appdefs.Urls
import com.washathomes.apputils.modules.BooleanResponse
import com.washathomes.apputils.modules.CreateOrderObj
import com.washathomes.apputils.modules.ErrorResponse
import com.washathomes.apputils.payment.ExampleEphemeralKeyProvider
import com.washathomes.apputils.remote.RetrofitAPIs
import com.washathomes.apputils.remote.StripeAPI
import com.washathomes.R
import com.washathomes.views.main.washee.WasheeMainActivity
import com.washathomes.databinding.FragmentPaymentBinding
import com.washathomes.model.stripe.IntentResponse
import com.washathomes.util.payment.StripeClient
import dagger.hilt.android.AndroidEntryPoint
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.math.BigDecimal
import java.util.*

@AndroidEntryPoint
class PaymentFragment : Fragment() {

    lateinit var binding: FragmentPaymentBinding
    lateinit var navController: NavController
    lateinit var washeeMainActivity: WasheeMainActivity
    lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var stripe: Stripe
    private lateinit var paymentsClient: PaymentsClient
    private val LOAD_PAYMENT_DATA_REQUEST_CODE = 53
    private lateinit var idempotencyKey: String
    private var paymentMethod: PaymentMethod? = null
    private val LOCATION_CODE = 100
    lateinit var intentResponse: IntentResponse
    val clientKey = "ATWUSgGrSAamf7geOE5oZFxWHEVqaByhq95Nu_wzVr9vOABQRE3Zj8t9SM7J922Uup93eqNSvt2WtFAg"
    val PAYPAL_REQUEST_CODE = 123

    // Paypal Configuration Object
    private val config = PayPalConfiguration() // Start with mock environment.  When ready,
        // switch to sandbox (ENVIRONMENT_SANDBOX)
        // or live (ENVIRONMENT_PRODUCTION)
        .environment(PayPalConfiguration.ENVIRONMENT_SANDBOX) // on below line we are passing a client id.
        .clientId(clientKey)

    var latitude = ""
    var longitude = ""
    var postalCode = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val layout = inflater.inflate(R.layout.fragment_payment, container, false)
        binding = FragmentPaymentBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is WasheeMainActivity) {
            washeeMainActivity = context
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews(view)
        getCurrentLocation()
        onClick()
        setData()
//        getEphemeralKeys()
    }

    private fun initViews(view: View){
        navController = Navigation.findNavController(view)
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(washeeMainActivity)


        idempotencyKey = UUID.randomUUID()!!.toString()
        PaymentConfiguration.init(requireContext(), getString(R.string.stripe_publishable_key))
        stripe = if (StripeClient.STRIPE_ACCOUNT_ID.isNotEmpty()) {
            Stripe(
                requireContext(), PaymentConfiguration.getInstance(requireContext()).publishableKey,
                StripeClient.STRIPE_ACCOUNT_ID
            )
        } else {
            Stripe(
                requireContext(),
                PaymentConfiguration.getInstance(requireContext()).publishableKey
            )
        }

        CustomerSession.initCustomerSession(requireContext(), ExampleEphemeralKeyProvider())

        binding.paymentGooglePayLayout.isEnabled = true
        paymentsClient = Wallet.getPaymentsClient(requireActivity(), Wallet.WalletOptions.Builder().setEnvironment(WalletConstants.ENVIRONMENT_TEST).build())

//        val config = CheckoutConfig(
//            application = washeeMainActivity.application,
//            clientId = "ATWUSgGrSAamf7geOE5oZFxWHEVqaByhq95Nu_wzVr9vOABQRE3Zj8t9SM7J922Uup93eqNSvt2WtFAg",
//            environment = Environment.SANDBOX,
//            returnUrl = "com.washathomes://paypalpay",
//            currencyCode = CurrencyCode.USD,
//            userAction = UserAction.PAY_NOW,
//            settingsConfig = SettingsConfig(
//                loggingEnabled = true
//            )
//        )
//        PayPalCheckout.setConfig(config)

        isReadyToPay()
    }

    private fun onClick(){
        binding.toolbarBackIcon.setOnClickListener { navController.popBackStack() }
        binding.paymentGooglePayLayout.setOnClickListener { makePaymentWithGoogle() }
        binding.paymentCreditCardLayout.setOnClickListener { payWithCard() }
        binding.paymentPayPalLayout.setOnClickListener { paypalPayment() }
        binding.paymentNextButton.setOnClickListener {
            makePayment()
        }
    }

    private fun payWithCard() {
        PaymentMethodsActivityStarter(this)
            .startForResult(
                PaymentMethodsActivityStarter.Args.Builder()
                    .setBillingAddressFields(BillingAddressFields.None ).build()
            )
//        PaymentMethodsActivityStarter(this).startForResult()
    }

    private fun isReadyToPay() {
        paymentsClient.isReadyToPay(createIsReadyToPayRequest())
            .addOnCompleteListener { task ->
                try {
                    if (task.isSuccessful) {
                        // show Google Pay as payment option
                        binding.paymentGooglePayLayout.isEnabled = true
                    } else {
                        // hide Google Pay as payment option
                        Toast.makeText(
                            context,
                            "Google Pay is unavailable",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                } catch (exception: ApiException) {
                    Toast.makeText(
                        context,
                        "Exception: " + exception.localizedMessage,
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
    }

    private fun createIsReadyToPayRequest(): IsReadyToPayRequest {
        return IsReadyToPayRequest.fromJson(
            JSONObject()
                .put(
                    "allowedAuthMethods", JSONArray()
                        .put("PAN_ONLY")
                        .put("CRYPTOGRAM_3DS")
                )
                .put(
                    "allowedCardNetworks",
                    JSONArray()
                        .put("AMEX")
                        .put("DISCOVER")
                        .put("JCB")
                        .put("MASTERCARD")
                        .put("VISA")
                )
                .toString()
        )
    }

    private fun createPaymentDataRequest(): PaymentDataRequest {
        // create PaymentMethod
        val cardPaymentMethod = JSONObject()
            .put("type", "CARD")
            .put(
                "parameters",
                JSONObject()
                    .put(
                        "allowedAuthMethods", JSONArray()
                            .put("PAN_ONLY")
                            .put("CRYPTOGRAM_3DS")
                    )
                    .put(
                        "allowedCardNetworks",
                        JSONArray()
                            .put("AMEX")
                            .put("DISCOVER")
                            .put("JCB")
                            .put("MASTERCARD")
                            .put("VISA")
                    )

                    // require billing address
                    .put("billingAddressRequired", true)
                    .put(
                        "billingAddressParameters",
                        JSONObject()
                            // require full billing address
                            .put("format", "FULL")

                            // require phone number
                            .put("phoneNumberRequired", true)
                    )
            )
            .put(
                "tokenizationSpecification",
                GooglePayConfig(requireContext()).tokenizationSpecification
            )

        // create PaymentDataRequest
        val paymentDataRequest = JSONObject()
            .put("apiVersion", 2)
            .put("apiVersionMinor", 0)
            .put(
                "allowedPaymentMethods",
                JSONArray().put(cardPaymentMethod)
            )
            .put(
                "transactionInfo", JSONObject()
                    .put("totalPrice", "10.00")
                    .put("totalPriceStatus", "FINAL")
                    .put("currencyCode", "USD")
            )
            .put(
                "merchantInfo", JSONObject()
                    .put("merchantName", "Example Merchant")
            )

            // require email address
            .put("emailRequired", true)
            .toString()

        return PaymentDataRequest.fromJson(paymentDataRequest)
    }

    private fun makePaymentWithGoogle() {
        AutoResolveHelper.resolveTask(
            paymentsClient.loadPaymentData(createPaymentDataRequest()),
            requireActivity(),
            LOAD_PAYMENT_DATA_REQUEST_CODE
        )
    }

    private fun handleGooglePayResult(data: Intent) {
        val paymentData = PaymentData.getFromIntent(data) ?: return
        val paymentMethodCreateParams =
            PaymentMethodCreateParams.createFromGooglePay(JSONObject(paymentData.toJson()))

        stripe.createPaymentMethod(paymentMethodCreateParams = paymentMethodCreateParams,
            idempotencyKey = idempotencyKey,
            callback = object : ApiResultCallback<PaymentMethod> {
                override fun onSuccess(result: PaymentMethod) {
                }

                override fun onError(e: Exception) {
                }
            })
    }

    private fun makePayment() {
        if (paymentMethod != null) {
            createPaymentIntent(createPaymentIntentParams())
        }
    }

    private fun confirmPayment() {
        if (paymentMethod?.id?.isNotEmpty() == true) {
            stripe.confirmPayment(
                this,
                createConfirmPaymentIntentParams(intentResponse.response.client_secret)
            )
        }
    }

    private fun createPaymentIntentParams(): HashMap<String, Any> {
        paymentMethod?.let {
            return hashMapOf(
                "payment_method" to (it.id ?: ""),
                "customer" to (it.customerId ?: "")
            )
        }
        return HashMap()
    }

    private fun createConfirmPaymentIntentParams(
        clientSecret: String
    ): ConfirmPaymentIntentParams {
        return ConfirmPaymentIntentParams.createWithPaymentMethodId(paymentMethod!!.id!!, clientSecret)
    }

    private fun createPaymentIntent(params: HashMap<String, Any>){
        binding.progressBar.visibility = View.VISIBLE
        val okHttpClient = OkHttpClient.Builder().apply {
            addInterceptor(
                Interceptor { chain ->
                    val builder = chain.request().newBuilder()
                    builder.header("Content-Type", "application/json; charset=UTF-8")
                    builder.header("Authorization", AppDefs.user.token!!)
                    return@Interceptor chain.proceed(builder.build())
                }
            )
        }.build()
        val retrofit: Retrofit = Retrofit.Builder().baseUrl(Urls.BASE_URL).client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create()).build()
        val updateDeliveryCall: Call<IntentResponse> =
            retrofit.create(StripeAPI::class.java).createPaymentIntent(params)
        updateDeliveryCall.enqueue(object : Callback<IntentResponse> {
            override fun onResponse(call: Call<IntentResponse>, response: Response<IntentResponse>) {
                binding.progressBar.visibility = View.GONE
                if (response.isSuccessful) {
                    intentResponse = response.body()!!
                    confirmPayment()
                }else{
                    val gson = Gson()
                    val type = object : TypeToken<ErrorResponse>() {}.type //ErrorResponse is the data class that matches the error response
                    val errorResponse = gson.fromJson<ErrorResponse>(response.errorBody()!!.charStream(), type) // errorResponse is an instance of ErrorResponse that will contain details about the error
                    Toast.makeText(
                        washeeMainActivity,
                        errorResponse.status.massage.toString(),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<IntentResponse>, t: Throwable) {
                Toast.makeText(washeeMainActivity, resources.getString(R.string.internet_connection), Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun createOrder(id: String){
        binding.progressBar.visibility = View.VISIBLE

        val orderObj = CreateOrderObj(latitude, longitude, postalCode, "1", id, "")
        val okHttpClient = OkHttpClient.Builder().apply {
            addInterceptor(
                Interceptor { chain ->
                    val builder = chain.request().newBuilder()
                    builder.header("Content-Type", "application/json; charset=UTF-8")
                    builder.header("Authorization", AppDefs.user.token!!)
                    return@Interceptor chain.proceed(builder.build())
                }
            )
        }.build()
        val retrofit: Retrofit = Retrofit.Builder().baseUrl(Urls.BASE_URL).client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create()).build()
        val updateDeliveryCall: Call<BooleanResponse> =
            retrofit.create(RetrofitAPIs::class.java).createOrder(orderObj)
        updateDeliveryCall.enqueue(object : Callback<BooleanResponse> {
            override fun onResponse(call: Call<BooleanResponse>, response: Response<BooleanResponse>) {
                binding.progressBar.visibility = View.GONE
                if (response.isSuccessful) {
                    Toast.makeText(washeeMainActivity, resources.getString(R.string.order_created), Toast.LENGTH_SHORT).show()
                    val mainIntent = Intent(washeeMainActivity, WasheeMainActivity::class.java)
                    startActivity(mainIntent)
                    washeeMainActivity.finish()
                }else{
                    val gson = Gson()
                    val type = object : TypeToken<ErrorResponse>() {}.type //ErrorResponse is the data class that matches the error response
                    val errorResponse = gson.fromJson<ErrorResponse>(response.errorBody()!!.charStream(), type) // errorResponse is an instance of ErrorResponse that will contain details about the error
                    Toast.makeText(
                        washeeMainActivity,
                        errorResponse.status.massage.toString(),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<BooleanResponse>, t: Throwable) {
                Toast.makeText(washeeMainActivity, resources.getString(R.string.internet_connection), Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun setData(){
        val taxPercent = AppDefs.deliveryInfoPrices[8].price.toDouble()*100
        binding.paymentTaxLabel.text = resources.getString(R.string.tax)+" ("+taxPercent+"%)"
        binding.paymentCurrentTotalText.text = ""+AppDefs.cartData.total_price
        binding.paymentSubTotalText.text = ""+AppDefs.cartData.sub_total
        binding.paymentTaxText.text = ""+AppDefs.cartData.taks
        binding.paymentDiscountText.text = ""+AppDefs.cartData.discount
//        calculateTotal(0.00)
    }

    private fun getCurrentLocation(){
        if (ActivityCompat.checkSelfPermission(
                washeeMainActivity,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                washeeMainActivity,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermission()
            return
        }
        fusedLocationProviderClient.lastLocation.addOnCompleteListener(washeeMainActivity){ task ->
            val location: Location? = task.result
            if (location != null){
                latitude = ""+location.latitude
                longitude = ""+location.longitude
                getAddress(location.latitude, location.longitude)
            }else{
                Toast.makeText(washeeMainActivity, "Null", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun getAddress(latitude: Double, longitude: Double){
        val geocoder: Geocoder
        val addresses: List<Address>
        geocoder = Geocoder(washeeMainActivity, Locale.getDefault())

        addresses = geocoder.getFromLocation(
            latitude,
            longitude,
            1
        ) // Here 1 represent max location result to returned, by documents it recommended 1 to 5

//        address = addresses[0].getAddressLine(0) // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
        if (addresses[0].postalCode != null){
            postalCode = addresses[0].postalCode
        }
    }

    private fun requestPermission(){
        ActivityCompat.requestPermissions(
            washeeMainActivity, arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION),
            LOCATION_CODE
        )
    }

    private fun buildCardString(data: PaymentMethod.Card): String {
        return ""+data.brand + " " + getString(R.string.selected_card_summary) + " " + data.last4
    }

    private fun paypalPayment() {

        // Getting the amount from editText
        val amount: String = AppDefs.cartData.total_price.substring(0, AppDefs.cartData.total_price.indexOf(" "))

        // Creating a paypal payment on below line.
        val payment = PayPalPayment(
            BigDecimal(amount), "USD", "Course Fees",
            PayPalPayment.PAYMENT_INTENT_SALE
        )

        // Creating Paypal Payment activity intent
        val intent = Intent(washeeMainActivity, PaymentActivity::class.java)

        //putting the paypal configuration to the intent
        intent.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION, config)

        // Putting paypal payment to the intent
        intent.putExtra(PaymentActivity.EXTRA_PAYMENT, payment)

        // Starting the intent activity for result
        // the request code will be used on the method onActivityResult
        startActivityForResult(intent, PAYPAL_REQUEST_CODE)
    }

    override fun onActivityResult(
        requestCode: Int, resultCode: Int, data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode === PAYPAL_REQUEST_CODE) {

            // If the result is OK i.e. user has not canceled the payment
            if (resultCode === Activity.RESULT_OK) {

                // Getting the payment confirmation
                val confirm: PaymentConfirmation =
                    data!!.getParcelableExtra(PaymentActivity.EXTRA_RESULT_CONFIRMATION)!!

                // if confirmation is not null
                if (confirm != null) {
                    try {
                        // Getting the payment details
                        val paymentDetails = confirm.toJSONObject().toString(4)
                        // on below line we are extracting json response and displaying it in a text view.
                        val payObj = JSONObject(paymentDetails)
                        val payID = payObj.getJSONObject("response").getString("id")
                        val state = payObj.getJSONObject("response").getString("state")
                        createOrder(payID)
//                        paymentTV.setText("Payment $state\n with payment id is $payID")
                    } catch (e: JSONException) {
                        // handling json exception on below line
                        Log.e("Error", "an extremely unlikely failure occurred: ", e)
                    }
                }
            } else if (resultCode === Activity.RESULT_CANCELED) {
                // on below line we are checking the payment status.
                Log.i("paymentExample", "The user canceled.")
            } else if (resultCode === PaymentActivity.RESULT_EXTRAS_INVALID) {
                // on below line when the invalid paypal config is submitted.
                Log.i(
                    "paymentExample",
                    "An invalid Payment or PayPalConfiguration was submitted. Please see the docs."
                )
            }
        }
        if (requestCode == PaymentMethodsActivityStarter.REQUEST_CODE &&
            resultCode == Activity.RESULT_OK && data != null
        ) {
            paymentMethod = PaymentMethodsActivityStarter.Result.fromIntent(data)?.paymentMethod
            if (paymentMethod?.card != null) {
                binding.paymentCreditCardText.text = buildCardString(paymentMethod?.card!!)
                val brandIcon = ContextCompat.getDrawable(
                    requireContext(),
                    CardBrand.fromCode(paymentMethod?.card?.brand?.toString()).icon
                )

                binding.paymentCreditCardText.setCompoundDrawablesRelativeWithIntrinsicBounds(
                    brandIcon,
                    null,
                    null,
                    null
                )
            }
            binding.paymentCreditCardCheck.visibility = View.VISIBLE
        } else if (requestCode == LOAD_PAYMENT_DATA_REQUEST_CODE) {
            when (resultCode) {
                Activity.RESULT_OK -> {
                    if (data != null) {
                        handleGooglePayResult(data)
                    }
                }
                Activity.RESULT_CANCELED -> {
                    Toast.makeText(
                        context,
                        "Canceled", Toast.LENGTH_LONG
                    ).show()
                }
                AutoResolveHelper.RESULT_ERROR -> {
                    val status = AutoResolveHelper.getStatusFromIntent(data)
                    val statusMessage = status?.statusMessage ?: "unknown"
                    Toast.makeText(
                        context,
                        "Error: $statusMessage",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                // Log the status for debugging
                // Generally there is no need to show an error to
                // the user as the Google Payment API will do that
                else -> {
                }
            }
        } else {
            stripe.onPaymentResult(requestCode, data,
                object : ApiResultCallback<PaymentIntentResult> {
                    override fun onSuccess(result: PaymentIntentResult) {
                        // If authentication succeeded, the PaymentIntent will have
                        // user actions resolved; otherwise, handle the PaymentIntent
                        // status as appropriate (e.g. the customer may need to choose
                        // a new payment method)

                        val paymentIntent = result.intent
                        val status = paymentIntent.status
                        if (status == StripeIntent.Status.Succeeded || status == StripeIntent.Status.RequiresCapture) {
                            createOrder(intentResponse.response.id)
                            // show success UI
                        } else if (StripeIntent.Status.RequiresPaymentMethod == status) {
                            // attempt authentication again or
                            // ask for a new Payment Method
//                            Toast.makeText(context, getString(R.string.payment_autharization_error_message), Toast.LENGTH_LONG).show()
                            Toast.makeText(
                                context,
                                result.intent.lastPaymentError?.message
                                    ?: getString(R.string.payment_autharization_error_message),
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }

                    override fun onError(e: Exception) {
                        Log.d("Error on Payment : ", e.toString())
                    }
                })
        }
    }

}