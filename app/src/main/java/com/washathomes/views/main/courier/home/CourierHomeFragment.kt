package com.washathomes.views.main.courier.home

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.Task
import com.google.firebase.messaging.FirebaseMessaging
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.washathomes.apputils.appdefs.AppDefs
import com.washathomes.apputils.appdefs.Urls
import com.washathomes.apputils.modules.*
import com.washathomes.apputils.remote.RetrofitAPIs
import com.washathomes.R
import com.washathomes.views.main.courier.CourierMainActivity
import com.washathomes.views.main.courier.home.adapters.ActiveOrdersAdapter
import com.washathomes.views.main.courier.home.adapters.PendingOrdersAdapter
import com.washathomes.databinding.FragmentCourierHomeBinding
import dagger.hilt.android.AndroidEntryPoint
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.*
import kotlin.collections.ArrayList

@AndroidEntryPoint
class CourierHomeFragment : Fragment() {

    lateinit var binding: FragmentCourierHomeBinding
    lateinit var courierMainActivity: CourierMainActivity
    lateinit var navController: NavController
    var activeOrders: ArrayList<ActiveOrder> = ArrayList()
    var pendingOrders: ArrayList<PendingOrder> = ArrayList()
    var notifications: ArrayList<Notification> = ArrayList()
    lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private val LOCATION_CODE = 100
    var type = "active"
    var token = ""
    var latitude = ""
    var longitude = ""
    var postalCode = ""
    var isOptionsVisible = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val layout = inflater.inflate(R.layout.fragment_courier_home, container, false)
        binding = FragmentCourierHomeBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is CourierMainActivity) {
            courierMainActivity = context
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews(view)
        setData()
        onClick()
        getCurrentLocation()
        getActiveOrders()
        getNotifications()
        getToken()
        getPrices()
    }

    private fun initViews(view: View){
        navController = Navigation.findNavController(view)
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(courierMainActivity)
    }

    private fun setData(){
        if (AppDefs.user.results!!.image!!.isNotEmpty()){
            Glide.with(courierMainActivity).load(AppDefs.user.results!!.image).into(binding.washerHomeWasherImage)
        }
        binding.washerHomeWelcome.text = resources.getString(R.string.hey_there)+" "+ AppDefs.user.results!!.name
        binding.washerHomeSwitchAvailability.isChecked = AppDefs.user.results!!.dreiver_available == "1"
        getOrders("1")
    }

    private fun onClick(){
        binding.washerHomeSwitchAvailability.setOnClickListener { changeAvailability() }
        binding.activeOrdersTab.setOnClickListener {
            type = "active"
            binding.activeOrdersTitle.setTextColor(resources.getColor(R.color.blue))
            binding.activeOrdersLine.setBackgroundColor(resources.getColor(R.color.blue))

            binding.pendingOrdersTitle.setTextColor(resources.getColor(R.color.black_trans30))
            binding.pendingOrdersLine.setBackgroundColor(resources.getColor(R.color.grey))

            getActiveOrders()
        }
        binding.pendingOrdersTab.setOnClickListener {
            type = "pending"
            binding.activeOrdersTitle.setTextColor(resources.getColor(R.color.black_trans30))
            binding.activeOrdersLine.setBackgroundColor(resources.getColor(R.color.grey))

            binding.pendingOrdersTitle.setTextColor(resources.getColor(R.color.blue))
            binding.pendingOrdersLine.setBackgroundColor(resources.getColor(R.color.blue))

            getPendingOrders()
        }
        binding.refreshLayout.setOnRefreshListener {
            if (type == "active") {
                getActiveOrders()
            } else {
                getPendingOrders()
            }
            binding.refreshLayout.isRefreshing = false
        }
        binding.toolbarLayout.notifications.setOnClickListener {
            navController.navigate(CourierHomeFragmentDirections.actionCourierHomeFragmentToCourierNotificationsFragment()) }
        binding.toolbarLayout.toolbarLeftIcon.setOnClickListener {
            navController.navigate(CourierHomeFragmentDirections.actionCourierHomeFragmentToCourierNotificationsFragment()) }
        binding.toolbarLayout.toolbarNotifyBadge.setOnClickListener {
            navController.navigate(CourierHomeFragmentDirections.actionCourierHomeFragmentToCourierNotificationsFragment()) }
        binding.optionsFB.setOnClickListener {
            if (isOptionsVisible){
                isOptionsVisible = false
                binding.optionsLayout.visibility = View.GONE
            }else{
                isOptionsVisible = true
                binding.optionsLayout.visibility = View.VISIBLE
            }
        }
        binding.chatWithUs.setOnClickListener {
            openWhatsApp(AppDefs.deliveryInfoPrices[11].price)
        }
        binding.callUs.setOnClickListener { callPhone() }
        binding.contactUs.setOnClickListener { openWebPageInBrowser() }
    }

    private fun getPrices(){
        val locationObj = LocationObj(latitude, longitude, postalCode)
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
        val getCartCall: Call<DeliveryInfoPrices> =
            retrofit.create(RetrofitAPIs::class.java).getDeliveryInfo(locationObj)
        getCartCall.enqueue(object : Callback<DeliveryInfoPrices> {
            override fun onResponse(call: Call<DeliveryInfoPrices>, response: Response<DeliveryInfoPrices>) {
                if (response.isSuccessful){
                    AppDefs.deliveryInfoPrices = response.body()!!.results
                }else{
                    val gson = Gson()
                    val type = object : TypeToken<ErrorResponse>() {}.type //ErrorResponse is the data class that matches the error response
                    val errorResponse = gson.fromJson<ErrorResponse>(response.errorBody()!!.charStream(), type) // errorResponse is an instance of ErrorResponse that will contain details about the error
                    Toast.makeText(
                        courierMainActivity,
                        errorResponse.status.massage.toString(),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<DeliveryInfoPrices>, t: Throwable) {
                Toast.makeText(courierMainActivity, resources.getString(R.string.internet_connection), Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun openWhatsApp(num: String) {
        val url = "https://api.whatsapp.com/send?phone="+num
        val i = Intent(Intent.ACTION_VIEW)
        i.data = Uri.parse(url)
        startActivity(i)
    }

    private fun callPhone(){
        val intent = Intent(Intent.ACTION_DIAL)
        intent.data = Uri.parse("tel:"+AppDefs.deliveryInfoPrices[12].price)
        startActivity(intent)
    }

    private fun openWebPageInBrowser() {
        val browserIntent =
            Intent(Intent.ACTION_VIEW, Uri.parse(AppDefs.deliveryInfoPrices[13].price))
        startActivity(browserIntent)
    }

    private fun changeAvailability(){
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
        val availabilityCall: Call<UserData> =
            retrofit.create(RetrofitAPIs::class.java).changeCourierAvailability()
        availabilityCall.enqueue(object : Callback<UserData> {
            override fun onResponse(call: Call<UserData>, response: Response<UserData>) {
                binding.progressBar.visibility = View.GONE
                if (response.isSuccessful){
                    AppDefs.user = response.body()!!
                    saveUserToSharedPreferences()
                    setData()
                }else{
                    val gson = Gson()
                    val type = object : TypeToken<ErrorResponse>() {}.type //ErrorResponse is the data class that matches the error response
                    val errorResponse = gson.fromJson<ErrorResponse>(response.errorBody()!!.charStream(), type) // errorResponse is an instance of ErrorResponse that will contain details about the error
                    Toast.makeText(courierMainActivity, errorResponse.status.massage.toString(), Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<UserData>, t: Throwable) {
                binding.progressBar.visibility = View.GONE
                Toast.makeText(courierMainActivity, resources.getString(R.string.internet_connection), Toast.LENGTH_SHORT).show()
            }

        })
    }

    private fun getActiveOrders(){
        activeOrders.clear()
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
        val ordersCall: Call<ActiveOrders> =
            retrofit.create(RetrofitAPIs::class.java).getCourierActiveOrders()
        ordersCall.enqueue(object : Callback<ActiveOrders> {
            override fun onResponse(call: Call<ActiveOrders>, response: Response<ActiveOrders>) {
                binding.progressBar.visibility = View.GONE
                if (response.isSuccessful){
                    activeOrders = response.body()!!.results
                    setActiveOrdersAdapter()
                }else{
//                    val gson = Gson()
//                    val type = object : TypeToken<ErrorResponse>() {}.type //ErrorResponse is the data class that matches the error response
//                    val errorResponse = gson.fromJson<ErrorResponse>(response.errorBody()!!.charStream(), type) // errorResponse is an instance of ErrorResponse that will contain details about the error
//                    Toast.makeText(courierMainActivity, errorResponse.status.massage.toString(), Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ActiveOrders>, t: Throwable) {
                binding.progressBar.visibility = View.GONE
                Toast.makeText(courierMainActivity, resources.getString(R.string.internet_connection), Toast.LENGTH_SHORT).show()
            }

        })
    }

    private fun setActiveOrdersAdapter(){
        val adapter = ActiveOrdersAdapter(this, activeOrders)
        binding.ordersRV.adapter = adapter
        binding.ordersRV.layoutManager = LinearLayoutManager(courierMainActivity)
    }

    private fun getPendingOrders(){
        pendingOrders.clear()
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
        val ordersCall: Call<PendingOrders> =
            retrofit.create(RetrofitAPIs::class.java).getCourierPendingOrders()
        ordersCall.enqueue(object : Callback<PendingOrders> {
            override fun onResponse(call: Call<PendingOrders>, response: Response<PendingOrders>) {
                binding.progressBar.visibility = View.GONE
                if (response.isSuccessful){
                    pendingOrders = response.body()!!.results
                    setPendingOrdersAdapter()
                }else{
                    val gson = Gson()
                    val type = object : TypeToken<ErrorResponse>() {}.type //ErrorResponse is the data class that matches the error response
                    val errorResponse = gson.fromJson<ErrorResponse>(response.errorBody()!!.charStream(), type) // errorResponse is an instance of ErrorResponse that will contain details about the error
                    Toast.makeText(courierMainActivity, errorResponse.status.massage.toString(), Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<PendingOrders>, t: Throwable) {
                binding.progressBar.visibility = View.GONE
                Toast.makeText(courierMainActivity, resources.getString(R.string.internet_connection), Toast.LENGTH_SHORT).show()
            }

        })
    }

    private fun setPendingOrdersAdapter(){
        val adapter = PendingOrdersAdapter(this, pendingOrders)
        binding.ordersRV.adapter = adapter
        binding.ordersRV.layoutManager = LinearLayoutManager(courierMainActivity)
    }

    fun pendingOrderDetails(order: PendingOrderObj){
        AppDefs.pendingOrder = order
        navController.navigate(CourierHomeFragmentDirections.actionCourierHomeFragmentToOrderPlacedOverviewFragment2(true))
    }

    fun orderDetails(order: ActiveOrderObj){
        AppDefs.activeOrder = order
        when (order.status) {
            "0" -> {
                navController.navigate(CourierHomeFragmentDirections.actionCourierHomeFragmentToOrderPlacedFragment2())
            }
            "1" -> {
                navController.navigate(CourierHomeFragmentDirections.actionCourierHomeFragmentToOrderPlacedOverviewFragment2(false))
            }
            "2" -> {
                navController.navigate(CourierHomeFragmentDirections.actionCourierHomeFragmentToOrderInProgressFragment3())
            }
            "4" -> {
                navController.navigate(CourierHomeFragmentDirections.actionCourierHomeFragmentToOrderInProgressFragment3())
            }
            "5" -> {
                navController.navigate(CourierHomeFragmentDirections.actionCourierHomeFragmentToOrderInProgressFragment3())
            }
            "6" -> {
                navController.navigate(CourierHomeFragmentDirections.actionCourierHomeFragmentToOrderInProgressFragment3())
            }
            "7" -> {
                navController.navigate(CourierHomeFragmentDirections.actionCourierHomeFragmentToOrderInProgressFragment3())
            }
            "8" -> {
                navController.navigate(CourierHomeFragmentDirections.actionCourierHomeFragmentToOrderDeliveredFragment2())
            }
        }
    }

    private fun saveUserToSharedPreferences() {
        val sharedPreferences =
            courierMainActivity.getSharedPreferences(AppDefs.SHARED_PREF_KEY, Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString(AppDefs.ID_KEY, AppDefs.user.results!!.id)

        val gson = Gson()
        val json = gson.toJson(AppDefs.user)
        editor.putString(AppDefs.USER_KEY, json)
        editor.putString(AppDefs.TYPE_KEY, "3")
        editor.apply()
    }

    private fun getOrders(status: String){
//        binding.progressBar.visibility = View.VISIBLE
        val statusStr = StatusStr(status)
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
        val languagesCall: Call<OrderHistoryResponse> =
            retrofit.create(RetrofitAPIs::class.java).getCourierOrderHistory(statusStr)
        languagesCall.enqueue(object : Callback<OrderHistoryResponse> {
            override fun onResponse(call: Call<OrderHistoryResponse>, response: Response<OrderHistoryResponse>) {
                binding.progressBar.visibility = View.GONE
                if (response.isSuccessful) {
                    binding.washerHomeNumberCompletedOrder.text = ""+response.body()!!.results.size+" "+resources.getString(R.string.completed_orders)
                }else{
                    val gson = Gson()
                    val type = object : TypeToken<ErrorResponse>() {}.type //ErrorResponse is the data class that matches the error response
                    val errorResponse = gson.fromJson<ErrorResponse>(response.errorBody()!!.charStream(), type) // errorResponse is an instance of ErrorResponse that will contain details about the error
                    Toast.makeText(
                        courierMainActivity,
                        errorResponse.status.massage.toString(),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<OrderHistoryResponse>, t: Throwable) {
                Toast.makeText(courierMainActivity, resources.getString(R.string.internet_connection), Toast.LENGTH_SHORT).show()
            }

        })
    }

    private fun getNotifications(){
        notifications.clear()
        val userTypeObj = UserTypeObj("2")
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
        val notificationsCall: Call<Notifications> =
            retrofit.create(RetrofitAPIs::class.java).getNotifications(userTypeObj)
        notificationsCall.enqueue(object : Callback<Notifications> {
            override fun onResponse(call: Call<Notifications>, response: Response<Notifications>) {
                if (response.isSuccessful){
                    notifications = response.body()!!.results.notifications
                    checkNewNotifications()
                }else{
                    val gson = Gson()
                    val type = object : TypeToken<ErrorResponse>() {}.type //ErrorResponse is the data class that matches the error response
                    val errorResponse = gson.fromJson<ErrorResponse>(response.errorBody()!!.charStream(), type) // errorResponse is an instance of ErrorResponse that will contain details about the error
                    Toast.makeText(courierMainActivity, errorResponse.status.massage.toString(), Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Notifications>, t: Throwable) {
                Toast.makeText(courierMainActivity, resources.getString(R.string.internet_connection), Toast.LENGTH_SHORT).show()
            }

        })
    }

    private fun checkNewNotifications(){
        var counter = 0
        for (notification in notifications){
            if (notification.is_read == "0"){
                counter++
            }
        }
        if (counter>0){
            binding.toolbarLayout.toolbarNotifyBadge.visibility = View.VISIBLE
            binding.toolbarLayout.toolbarNotifyBadge.text = counter.toString()
        }else{
            binding.toolbarLayout.toolbarNotifyBadge.visibility = View.GONE
        }
    }

    private fun getToken() {
        FirebaseMessaging.getInstance().token
            .addOnCompleteListener { task: Task<String?> ->
                if (!task.isSuccessful) {
                    Log.w(
                        "FAILED",
                        "Fetching FCM registration token failed",
                        task.exception
                    )
                    return@addOnCompleteListener
                }
                token = task.result!!
                updateToken()
            }
    }

    private fun updateToken(){
        val token = Token(token, "1")
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
        val notificationsCall: Call<UserData> =
            retrofit.create(RetrofitAPIs::class.java).updateToken(token)
        notificationsCall.enqueue(object : Callback<UserData> {
            override fun onResponse(call: Call<UserData>, response: Response<UserData>) {
                if (response.isSuccessful){
                    AppDefs.user = response.body()!!
                    saveUserToSharedPreferences()
                }else{
                    val gson = Gson()
                    val type = object : TypeToken<ErrorResponse>() {}.type //ErrorResponse is the data class that matches the error response
                    val errorResponse = gson.fromJson<ErrorResponse>(response.errorBody()!!.charStream(), type) // errorResponse is an instance of ErrorResponse that will contain details about the error
                    Toast.makeText(courierMainActivity, errorResponse.status.massage.toString(), Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<UserData>, t: Throwable) {
                Toast.makeText(courierMainActivity, resources.getString(R.string.internet_connection), Toast.LENGTH_SHORT).show()
            }

        })
    }

    private fun getCurrentLocation(){
//        if (checkPermissions()){
//            if (isLocationEnabled()){
//                if (ActivityCompat.checkSelfPermission(
//                        washeeRegistrationActivity,
//                        Manifest.permission.ACCESS_FINE_LOCATION
//                    ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
//                        washeeRegistrationActivity,
//                        Manifest.permission.ACCESS_COARSE_LOCATION
//                    ) != PackageManager.PERMISSION_GRANTED
//                ) {
//                    requestPermission()
//                    return
//                }
//                fusedLocationProviderClient.lastLocation.addOnCompleteListener(washeeRegistrationActivity){ task ->
//                    val location: Location? = task.result
//                    if (location != null){
//                        latitude = ""+location.latitude
//                        longitude = ""+location.longitude
//                        getAddress(location.latitude, location.longitude)
//                    }else{
//                        Toast.makeText(washeeRegistrationActivity, "Null", Toast.LENGTH_LONG).show()
//                    }
//                }
//            }else{
//                Toast.makeText(washeeRegistrationActivity, resources.getString(R.string.turn_on_location), Toast.LENGTH_SHORT).show()
//                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
//                startActivity(intent)
//            }
//        }else{
//            requestPermission()
//        }
        if (ActivityCompat.checkSelfPermission(
                courierMainActivity,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                courierMainActivity,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermission()
            return
        }
        fusedLocationProviderClient.lastLocation.addOnCompleteListener(courierMainActivity){ task ->
            val location: Location? = task.result
            if (location != null){
                latitude = ""+location.latitude
                longitude = ""+location.longitude
                getAddress(location.latitude, location.longitude)
            }else{
                Toast.makeText(courierMainActivity, "Null", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun getAddress(latitude: Double, longitude: Double){
        val geocoder: Geocoder
        val addresses: List<Address>
        geocoder = Geocoder(courierMainActivity, Locale.getDefault())

        addresses = geocoder.getFromLocation(
            latitude,
            longitude,
            1
        ) as List<Address>// Here 1 represent max location result to returned, by documents it recommended 1 to 5


//        address = addresses[0].getAddressLine(0) // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
        if (addresses[0].postalCode != null){
            postalCode = addresses[0].postalCode
        }
    }

    private fun requestPermission(){
        ActivityCompat.requestPermissions(
            courierMainActivity, arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION),
            LOCATION_CODE
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_CODE){
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                getCurrentLocation()
            }
        }
    }

}