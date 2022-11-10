package com.washathomes.views.main.washee.checkout.overview

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.washathomes.apputils.appdefs.AppDefs
import com.washathomes.apputils.appdefs.Urls
import com.washathomes.apputils.modules.*
import com.washathomes.apputils.remote.RetrofitAPIs
import com.washathomes.R
import com.washathomes.views.main.washee.WasheeMainActivity
import com.washathomes.databinding.FragmentOverviewBinding
import dagger.hilt.android.AndroidEntryPoint
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.*
@AndroidEntryPoint
class OverviewFragment : Fragment() {

    lateinit var binding: FragmentOverviewBinding
    lateinit var navController: NavController
    lateinit var washeeMainActivity: WasheeMainActivity
    lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    lateinit var promoCodeResponse: PromoCode
    private val LOCATION_CODE = 100
    var subTotal = 0.00
    var taxValue = 0.00
    var latitude = ""
    var longitude = ""
    var postalCode = ""
    var promoId = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val layout = inflater.inflate(R.layout.fragment_overview, container, false)
        binding = FragmentOverviewBinding.inflate(layoutInflater)
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
        onClick()
        getCurrentLocation()
        getCart()
    }

    private fun initViews(view: View){
        navController = Navigation.findNavController(view)
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(washeeMainActivity)
    }

    private fun onClick() {
        binding.toolbarBackIcon.setOnClickListener { navController.popBackStack() }
        binding.viewItemsLayout.setOnClickListener { navController.navigate(OverviewFragmentDirections.actionOverviewFragmentToViewItemsFragment()) }
        binding.promoCodeBtn.setOnClickListener {
            washeeMainActivity.hideKeyboard()
            val promo = binding.promoCodeEdit.text.toString()
            if (promo.isNotEmpty()){
                getPromoCode(promo)
            }
        }
        binding.overviewNextButton.setOnClickListener { updateOverview() }
    }

    private fun setData(){
        binding.viewItems.text = resources.getString(R.string.view_items)+" ("+AppDefs.cartData.CartItem.size+")"
        if (AppDefs.deliveryInfo.deliverySpeed == "0"){
            binding.deliverySpeed.text = resources.getString(R.string.normal_delivery)
        }else{
            binding.deliverySpeed.text = resources.getString(R.string.express_delivery)
        }
        if (AppDefs.deliveryInfo.deliveryOptions == "0"){
            binding.deliveryOption.text = resources.getString(R.string.self_drop_off_amp_pickup)
        }else{
            binding.deliveryOption.text = resources.getString(R.string.washer_driver_to_collect_amp_return)
        }
        binding.pickUpAddress.text = AppDefs.deliveryInfo.pickUpAddress
        binding.dropOffAddress.text = AppDefs.deliveryInfo.dropOffAddress
        binding.pickUpDate.text = AppDefs.deliveryInfo.pickUpDate
        binding.dropOffDate.text = AppDefs.deliveryInfo.dropOffDate
        binding.pickUpTime.text = AppDefs.deliveryInfo.pickUpTime
        binding.dropOffTime.text = AppDefs.deliveryInfo.dropOffTime
        if (AppDefs.deliveryInfo.insurance == "0"){
            binding.insurance.text = resources.getString(R.string.i_dont_want_to_insure_my_laundry_items)
        }else{
            binding.insurance.text = resources.getString(R.string.i_want_to_insure_my_laundry_items)
        }
        val taxPercent = AppDefs.deliveryInfoPrices[8].price.toDouble()*100
        binding.paymentTaxLabel.text = resources.getString(R.string.tax)+" ("+taxPercent+"%)"
        binding.paymentCurrentTotalText.text = ""+AppDefs.cartData.total_price
        binding.paymentSubTotalText.text = ""+AppDefs.cartData.sub_total
        binding.paymentTaxText.text = ""+AppDefs.cartData.taks
        binding.paymentDiscountText.text = ""+AppDefs.cartData.discount
//        calculateTotal(0.00)
    }

    private fun getCart(){
        binding.progressBar.visibility = View.VISIBLE
        val cartObj = CartObj(latitude, longitude, postalCode)
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
        val getCartCall: Call<Cart> =
            retrofit.create(RetrofitAPIs::class.java).getCart(cartObj)
        getCartCall.enqueue(object : Callback<Cart> {
            override fun onResponse(call: Call<Cart>, response: Response<Cart>) {
                binding.progressBar.visibility = View.GONE
                if (response.isSuccessful){
                    AppDefs.cartData = response.body()!!.results
                    setData()

                }else{
                    val gson = Gson()
                    val type = object : TypeToken<ErrorResponse>() {}.type //ErrorResponse is the data class that matches the error response
                    val errorResponse = gson.fromJson<ErrorResponse>(response.errorBody()!!.charStream(), type) // errorResponse is an instance of ErrorResponse that will contain details about the error
                    if (errorResponse.status.massage.toString() == "No Data"){
                    }else {
                        Toast.makeText(
                            washeeMainActivity,
                            errorResponse.status.massage.toString(),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }

            override fun onFailure(call: Call<Cart>, t: Throwable) {
                binding.progressBar.visibility = View.GONE
                Toast.makeText(washeeMainActivity, resources.getString(R.string.internet_connection), Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun calculateTotal(discount: Double){
        val discountValue = discount*subTotal
        if (discount != 0.00){
            binding.paymentDiscountLabel.text = resources.getString(R.string.discount)+" "+discount+"%"
            binding.paymentDiscountText.text = ""+washeeMainActivity.formatter.format(discountValue)
        }
        val total = subTotal+taxValue-discountValue
        binding.paymentCurrentTotalText.text = ""+washeeMainActivity.formatter.format(total)
    }

    private fun getPromoCode(promo: String){
        binding.progressBar.visibility = View.VISIBLE
        val promoCodeObj = PromoCodeObj(promo, latitude, longitude, postalCode)
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
        val getCartCall: Call<PromoCodeResponse> =
            retrofit.create(RetrofitAPIs::class.java).getPromoCode(promoCodeObj)
        getCartCall.enqueue(object : Callback<PromoCodeResponse> {
            override fun onResponse(call: Call<PromoCodeResponse>, response: Response<PromoCodeResponse>) {
                binding.progressBar.visibility = View.GONE
                if (response.isSuccessful){
                    promoCodeResponse = response.body()!!.results
                    promoId = promoCodeResponse.id
                    getCart()
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

            override fun onFailure(call: Call<PromoCodeResponse>, t: Throwable) {
                binding.progressBar.visibility = View.GONE
                binding.layout.visibility = View.VISIBLE
                Toast.makeText(washeeMainActivity, resources.getString(R.string.internet_connection), Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun updateOverview(){
        binding.progressBar.visibility = View.VISIBLE

        val overviewObj = OverviewObj(promoId, latitude, longitude, postalCode)
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
            retrofit.create(RetrofitAPIs::class.java).updateOverview(overviewObj)
        updateDeliveryCall.enqueue(object : Callback<BooleanResponse> {
            override fun onResponse(call: Call<BooleanResponse>, response: Response<BooleanResponse>) {
                binding.progressBar.visibility = View.GONE
                if (response.isSuccessful) {
//                    createOrder()
                    if (AppDefs.cartData.discount.substring(0, AppDefs.cartData.discount.indexOf(" ")) == AppDefs.cartData.sub_total.substring(0, AppDefs.cartData.sub_total.indexOf(" "))){
                        createOrder()
                    }else{
                        navController.navigate(OverviewFragmentDirections.actionOverviewFragmentToPaymentFragment())
                    }
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

    private fun createOrder(){
        binding.progressBar.visibility = View.VISIBLE

        val orderObj = CreateOrderObj(latitude, longitude, postalCode, "1", "", "")
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

}