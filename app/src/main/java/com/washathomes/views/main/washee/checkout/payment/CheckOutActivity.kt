package com.washathomes.views.main.washee.checkout.payment

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.wallet.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.paypal.checkout.approve.OnApprove
import com.paypal.checkout.cancel.OnCancel
import com.paypal.checkout.createorder.CreateOrder
import com.paypal.checkout.createorder.CurrencyCode
import com.paypal.checkout.createorder.OrderIntent
import com.paypal.checkout.createorder.UserAction
import com.paypal.checkout.error.OnError
import com.paypal.checkout.order.Amount
import com.paypal.checkout.order.AppContext
import com.paypal.checkout.order.Order
import com.paypal.checkout.order.PurchaseUnit
import com.stripe.android.PaymentConfiguration
import com.stripe.android.databinding.PaymentFlowActivityBinding
import com.stripe.android.paymentsheet.PaymentSheet
import com.stripe.android.paymentsheet.PaymentSheetResult
import com.washathomes.R
import com.washathomes.apputils.appdefs.AppDefs
import com.washathomes.apputils.appdefs.Urls
import com.washathomes.apputils.modules.BooleanResponse
import com.washathomes.apputils.modules.CreateOrderObj
import com.washathomes.apputils.modules.ErrorResponse
import com.washathomes.apputils.modules.payment.CustomerResponse
import com.washathomes.apputils.modules.payment.ephemeral.EphemeralModel
import com.washathomes.apputils.modules.payment.ephemeral.EphemeralResponse
import com.washathomes.apputils.modules.payment.paymentintent.PaymentIntentResponse
import com.washathomes.apputils.modules.payment.strips.StripsResponse
import com.washathomes.apputils.remote.RetrofitAPIs
import com.washathomes.databinding.ActivityCheckoutBinding
import com.washathomes.views.main.washee.WasheeMainActivity
import com.washathomes.views.main.washee.checkout.payment.googlepayment.Constants.STRIPSSECRITKEY
import com.washathomes.views.main.washee.checkout.payment.googlepayment.Constants.STRIPUPLISHTKEY
import com.washathomes.views.main.washee.checkout.payment.googlepayment.Constants.URLSTRIPS
import com.washathomes.views.main.washee.checkout.payment.googlepayment.PaymentsUtil
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_checkout.*
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import org.json.JSONException
import org.json.JSONObject
import retrofit2.*
import retrofit2.converter.gson.GsonConverterFactory
import java.lang.annotation.ElementType
import java.util.*


@AndroidEntryPoint
class CheckOutActivity : AppCompatActivity() {
    lateinit var binding: ActivityCheckoutBinding
    var latitude = ""
    var longitude = ""
    var postalCode = ""
    lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private val LOCATION_CODE = 100
    private val TEZ_REQUEST_CODE = 123
    private lateinit var paymentsClient: PaymentsClient

    private val LOAD_PAYMENT_DATA_REQUEST_CODE = 991
    lateinit var paymentSheet: PaymentSheet
    lateinit var customerConfig: PaymentSheet.CustomerConfiguration
    lateinit var  paymentcargeId:String


    /*lateinit var customerId: String
    lateinit var ephemeralKeys: String
    lateinit var clientSecret:String*/


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCheckoutBinding.inflate(layoutInflater)
        setContentView(binding.root)
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)


        getCurrentLocation()
        getPayPal()
        setData()

        paymentsClient = PaymentsUtil.createPaymentsClient(this)
        possiblyShowGooglePayButton()

        binding.paymentGooglePayLayout.setOnClickListener { requestPayment() }
        PaymentConfiguration.init(
            applicationContext,
            STRIPUPLISHTKEY
        )
        paymentSheet = PaymentSheet(this, ::onPaymentSheetResult)
       // getStripss()
        binding.paymentCreditCardLayout.setOnClickListener {


            getStripsPayment()
        }
        binding.toolbarBackIcon.setOnClickListener { finish() }

    }



   /* private fun getStripss() {


        val okHttpClient = OkHttpClient.Builder().apply {
            addInterceptor(
                Interceptor { chain ->
                    val builder = chain.request().newBuilder()
                    //  builder.header("Content-Type", "application/json; charset=UTF-8")
                    builder.header("Authorization", "Bearer $STRIPSSECRITKEY")
                    return@Interceptor chain.proceed(builder.build())
                }
            )
        }.build()
        val retrofit: Retrofit = Retrofit.Builder().baseUrl(URLSTRIPS).client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create()).build()
        val updateDeliveryCall: Call<CustomerResponse> =
            retrofit.create(RetrofitAPIs::class.java).createCustomers()
        updateDeliveryCall.enqueue(object : Callback<CustomerResponse> {
            override fun onResponse(
                call: Call<CustomerResponse>,
                response: Response<CustomerResponse>
            ) {

                if (response.isSuccessful) {
                    Log.d("thisdata", response.body()!!.id)
                    customerId = response.body()!!.id
                    getEphericalKey(customerId)


                } else {
                    val gson = Gson()
                    val type = object :
                        TypeToken<ErrorResponse>() {}.type //ErrorResponse is the data class that matches the error response
                    val errorResponse = gson.fromJson<ErrorResponse>(
                        response.errorBody()!!.charStream(),
                        type
                    ) // errorResponse is an instance of ErrorResponse that will contain details about the error

                }
            }

            override fun onFailure(call: Call<CustomerResponse>, t: Throwable) {
                Toast.makeText(
                    this@CheckOutActivity,
                    resources.getString(R.string.internet_connection),
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    private fun getEphericalKey(customerId: String) {

        val okHttpClient = OkHttpClient.Builder().apply {
            addInterceptor(
                Interceptor { chain ->
                    val builder = chain.request().newBuilder()
                    //builder.header("Content-Type", "application/json; charset=UTF-8")
                    builder.header("Authorization", "Bearer $STRIPSSECRITKEY")
                    builder.header("Stripe-Version", "2022-11-15")
                    return@Interceptor chain.proceed(builder.build())
                }
            )
        }.build()
        val retrofit: Retrofit = Retrofit.Builder().baseUrl(URLSTRIPS).client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create()).build()
        val updateDeliveryCall: Call<EphemeralResponse> =
            retrofit.create(RetrofitAPIs::class.java).createPhemeralKeys(hashMapOf(

                "customer" to (customerId)
            ))
        updateDeliveryCall.enqueue(object : Callback<EphemeralResponse> {
            override fun onResponse(
                call: Call<EphemeralResponse>,
                response: Response<EphemeralResponse>
            ) {

                if (response.isSuccessful) {
                    Log.d("thisdata", response.body()!!.id)
                    ephemeralKeys=response.body()!!.id
                    getClientSecret(customerId,ephemeralKeys)


                } else {
                    val gson = Gson()
                    val type = object :
                        TypeToken<ErrorResponse>() {}.type //ErrorResponse is the data class that matches the error response
                    val errorResponse = gson.fromJson<ErrorResponse>(
                        response.errorBody()!!.charStream(),
                        type

                    ) // errorResponse is an instance of ErrorResponse that will contain details about the error

                    Log.d("thisdata", errorResponse.toString())

                }
            }

            override fun onFailure(call: Call<EphemeralResponse>, t: Throwable) {
                Toast.makeText(
                    this@CheckOutActivity,
                    resources.getString(R.string.internet_connection),
                    Toast.LENGTH_SHORT
                ).show()
            }
        })

    }

    private fun getClientSecret(customerId: String, ephemeralKeys: String) {

        val okHttpClient = OkHttpClient.Builder().apply {
            addInterceptor(
                Interceptor { chain ->
                    val builder = chain.request().newBuilder()
                    //builder.header("Content-Type", "application/json; charset=UTF-8")
                    builder.header("Authorization", "Bearer $STRIPSSECRITKEY")

                    return@Interceptor chain.proceed(builder.build())
                }
            )
        }.build()
        val retrofit: Retrofit = Retrofit.Builder().baseUrl(URLSTRIPS).client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create()).build()
        val updateDeliveryCall: Call<PaymentIntentResponse> =
            retrofit.create(RetrofitAPIs::class.java).createPaymentIntents(hashMapOf(

                "customer" to (customerId),
                "amount" to (1000),
                "currency" to ("eur"),
                "automatic_payment_methods[enabled]" to (true)

            ))
        updateDeliveryCall.enqueue(object : Callback<PaymentIntentResponse> {
            override fun onResponse(
                call: Call<PaymentIntentResponse>,
                response: Response<PaymentIntentResponse>
            ) {

                if (response.isSuccessful) {
                    Log.d("thisdata", response.body()!!.client_secret)
                    clientSecret=response.body()!!.client_secret





                } else {
                    val gson = Gson()
                    val type = object :
                        TypeToken<ErrorResponse>() {}.type //ErrorResponse is the data class that matches the error response
                    val errorResponse = gson.fromJson<ErrorResponse>(
                        response.errorBody()!!.charStream(),
                        type

                    ) // errorResponse is an instance of ErrorResponse that will contain details about the error

                    Log.d("thisdata", errorResponse.toString())

                }
            }

            override fun onFailure(call: Call<PaymentIntentResponse>, t: Throwable) {
                Toast.makeText(
                    this@CheckOutActivity,
                    resources.getString(R.string.internet_connection),
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }*/

    private fun PaymentFlow(paymentIntent:String,companyName:String,customerId:String,ephemeralKeys:String) {
        paymentSheet.presentWithPaymentIntent(
            paymentIntent,
            PaymentSheet.Configuration(
                companyName,
                PaymentSheet.CustomerConfiguration(
                    customerId,
                    ephemeralKeys
                )
            )
        )
    }




    fun onPaymentSheetResult(paymentSheetResult: PaymentSheetResult) {
        when (paymentSheetResult) {
            is PaymentSheetResult.Canceled -> {
                print("Canceled")
            }
            is PaymentSheetResult.Failed -> {
                print("Error: ${paymentSheetResult.error}")
            }
            is PaymentSheetResult.Completed -> {
                // Display for example, an order confirmation screen
                print("Completed")
                createOrder("2",paymentcargeId)


                Toast.makeText(this, "Success", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun possiblyShowGooglePayButton() {

        val isReadyToPayJson = PaymentsUtil.isReadyToPayRequest() ?: return
        val request = IsReadyToPayRequest.fromJson(isReadyToPayJson.toString()) ?: return

        // The call to isReadyToPay is asynchronous and returns a Task. We need to provide an
        // OnCompleteListener to be triggered when the result of the call is known.
        val task = paymentsClient.isReadyToPay(request)
        task.addOnCompleteListener { completedTask ->
            try {
                completedTask.getResult(ApiException::class.java)?.let(::setGooglePayAvailable)
            } catch (exception: ApiException) {
                // Process error
                Log.w("isReadyToPay failed", exception)
            }
        }
    }

    private fun setGooglePayAvailable(available: Boolean) {
        if (available) {
            binding.paymentGooglePayLayout.visibility = View.VISIBLE
        } else {
            Toast.makeText(
                this,
                "Unfortunately, Google Pay is not available on this device",
                Toast.LENGTH_LONG
            ).show();
        }
    }

    private fun requestPayment() {

        binding.paymentGooglePayLayout.isClickable = false
        val amount: Double =
            AppDefs.cartData.total_price.substring(0, AppDefs.cartData.total_price.indexOf(" "))
                .toDouble()
        val paymentDataRequestJson = PaymentsUtil.getPaymentDataRequest(amount.toLong())
        if (paymentDataRequestJson == null) {
            Log.e("RequestPayment", "Can't fetch payment data request")
            return
        }
        val request = PaymentDataRequest.fromJson(paymentDataRequestJson.toString())
        if (request != null) {
            AutoResolveHelper.resolveTask(
                paymentsClient.loadPaymentData(request), this, LOAD_PAYMENT_DATA_REQUEST_CODE
            )
        }
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            // Value passed in AutoResolveHelper
            LOAD_PAYMENT_DATA_REQUEST_CODE -> {
                when (resultCode) {
                    RESULT_OK ->
                        data?.let { intent ->
                            PaymentData.getFromIntent(intent)?.let(::handlePaymentSuccess)
                        }

                    RESULT_CANCELED -> {
                        // The user cancelled the payment attempt
                    }

                    AutoResolveHelper.RESULT_ERROR -> {
                        AutoResolveHelper.getStatusFromIntent(data)?.let {
                            handleError(it.statusCode)
                        }
                    }
                }

                // Re-enables the Google Pay payment button.
                binding.paymentGooglePayLayout.isClickable = true
            }
        }
    }

    private fun handlePaymentSuccess(paymentData: PaymentData) {
        val paymentInformation = paymentData.toJson() ?: return

        try {
            // Token will be null if PaymentDataRequest was not constructed using fromJson(String).
            val paymentMethodData =
                JSONObject(paymentInformation).getJSONObject("paymentMethodData")
            val billingName = paymentMethodData.getJSONObject("info")
                .getJSONObject("billingAddress").getString("name")
            Log.d("BillingName", paymentMethodData.toString())
            createOrder("1","")
            Toast.makeText(this, "Successfully received payment ", Toast.LENGTH_LONG).show()

            // Logging token string.
            Log.d(
                "GooglePaymentToken", paymentMethodData
                    .getJSONObject("tokenizationData")
                    .getString("token")
            )

        } catch (e: JSONException) {
            Log.e("handlePaymentSuccess", "Error: " + e.toString())
        }

    }

    private fun handleError(statusCode: Int) {
        Log.w("loadPaymentData failed", String.format("Error code: %d", statusCode))
    }

    fun getPayPal() {
        val amount: String =
            AppDefs.cartData.total_price.substring(0, AppDefs.cartData.total_price.indexOf(" "))
        binding.paymentButtonContainer.setup(
            createOrder =
            CreateOrder { createOrderActions ->
                val order =
                    Order(
                        intent = OrderIntent.CAPTURE,
                        appContext = AppContext(userAction = UserAction.PAY_NOW),
                        purchaseUnitList =
                        listOf(
                            PurchaseUnit(
                                amount =
                                Amount(currencyCode = CurrencyCode.USD, value = "$amount")
                            )
                        )
                    )
                createOrderActions.create(order)
            },
            onApprove =
            OnApprove { approval ->

                approval.orderActions.capture { captureOrderResult ->
                    Log.i("CaptureOrder", "CaptureOrderResult: $captureOrderResult")
                    Log.i("CaptureOrder", approval.data.payerId.toString())
                    createOrder("1",approval.data.payerId.toString())


                }
            }, onCancel = OnCancel {
                Log.d("OnCancel", "Buyer canceled the PayPal experience.")
            }, onError = OnError { errorInfo ->
                Log.d("OnError", "Error: $errorInfo")
                Toast.makeText(this, "$errorInfo", Toast.LENGTH_SHORT).show()
            }

        )
    }

    private fun createOrder(typePayment:String,id: String) {
        binding.progressBar.visibility = View.VISIBLE

        val orderObj = CreateOrderObj(latitude, longitude, postalCode, typePayment, id, "")
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
            override fun onResponse(
                call: Call<BooleanResponse>,
                response: Response<BooleanResponse>
            ) {
                binding.progressBar.visibility = View.GONE
                if (response.isSuccessful) {
                    Toast.makeText(
                        this@CheckOutActivity,
                        resources.getString(R.string.order_created),
                        Toast.LENGTH_SHORT
                    ).show()
                    val mainIntent = Intent(this@CheckOutActivity, WasheeMainActivity::class.java)
                    startActivity(mainIntent)
                    this@CheckOutActivity.finish()
                } else {
                    val gson = Gson()
                    val type = object :
                        TypeToken<ErrorResponse>() {}.type //ErrorResponse is the data class that matches the error response
                    val errorResponse = gson.fromJson<ErrorResponse>(
                        response.errorBody()!!.charStream(),
                        type
                    ) // errorResponse is an instance of ErrorResponse that will contain details about the error

                }
            }

            override fun onFailure(call: Call<BooleanResponse>, t: Throwable) {
                Toast.makeText(
                    this@CheckOutActivity,
                    resources.getString(R.string.internet_connection),
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }
    private fun getStripsPayment() {


        val okHttpClient = OkHttpClient.Builder().apply {
            addInterceptor(
                Interceptor { chain ->
                    val builder = chain.request().newBuilder()
                  //  builder.header("Content-Type", "application/json; charset=UTF-8")
                    builder.header("Authorization", AppDefs.user.token!!)
                    return@Interceptor chain.proceed(builder.build())
                }
            )
        }.build()
        val retrofit: Retrofit = Retrofit.Builder().baseUrl(Urls.BASE_URL2).client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create()).build()
        val updateDeliveryCall: Call<StripsResponse> =
            retrofit.create(RetrofitAPIs::class.java).createStripePayment()
        updateDeliveryCall.enqueue(object : Callback<StripsResponse> {
            override fun onResponse(
                call: Call<StripsResponse>,
                response: Response<StripsResponse>
            ) {

                if (response.isSuccessful) {
                    var result=response.body()
                    paymentcargeId=result!!.paymentIntent
                    PaymentFlow(result!!.paymentIntent,result.companyName,result.customer,result.ephemeralKey)
                } else {
                    val gson = Gson()
                    val type = object :
                        TypeToken<ErrorResponse>() {}.type //ErrorResponse is the data class that matches the error response
                    val errorResponse = gson.fromJson<ErrorResponse>(
                        response.errorBody()!!.charStream(),
                        type
                    ) // errorResponse is an instance of ErrorResponse that will contain details about the error

                }
            }

            override fun onFailure(call: Call<StripsResponse>, t: Throwable) {
                Toast.makeText(
                    this@CheckOutActivity,
                    resources.getString(R.string.internet_connection),
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    private fun getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermission()
            return
        }
        fusedLocationProviderClient.lastLocation.addOnCompleteListener(this) { task ->
            val location: Location? = task.result
            if (location != null) {
                latitude = "" + location.latitude
                longitude = "" + location.longitude
                getAddress(location.latitude, location.longitude)
            } else {
                Toast.makeText(this, "Please enable your location", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun getAddress(latitude: Double, longitude: Double) {
        val geocoder: Geocoder
        val addresses: List<Address>
        geocoder = Geocoder(this, Locale.getDefault())

        addresses = geocoder.getFromLocation(
            latitude,
            longitude,
            1
        ) as List<Address>// Here 1 represent max location result to returned, by documents it recommended 1 to 5

//        address = addresses[0].getAddressLine(0) // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
        if (addresses[0].postalCode != null) {
            postalCode = addresses[0].postalCode
        }
    }

    private fun requestPermission() {
        ActivityCompat.requestPermissions(
            this, arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
            ),
            LOCATION_CODE
        )
    }

    private fun setData() {
        val taxPercent = AppDefs.deliveryInfoPrices[8].price.toDouble() * 100
        binding.paymentTaxLabel.text = resources.getString(R.string.tax) + " (" + taxPercent + "%)"
        binding.paymentCurrentTotalText.text = "" + AppDefs.cartData.total_price
        binding.paymentSubTotalText.text = "" + AppDefs.cartData.sub_total
        binding.paymentTaxText.text = "" + AppDefs.cartData.taks
        binding.paymentDiscountText.text = "" + AppDefs.cartData.discount
        binding.paymentDeliveryText.text = ""+AppDefs.cartData.delivery_pickup_amount
//        calculateTotal(0.00)
    }
}
