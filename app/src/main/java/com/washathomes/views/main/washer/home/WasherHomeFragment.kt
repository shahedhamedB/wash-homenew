package com.washathomes.views.main.washer.home

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
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
import com.washathomes.views.main.washer.home.adapters.ActiveOrdersAdapter
import com.washathomes.views.main.washer.home.adapters.PendingOrdersAdapter
import com.washathomes.views.main.washer.WasherMainActivity
import com.washathomes.databinding.FragmentWasherHomeBinding
import com.washathomes.views.splash.SplashActivity
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
class WasherHomeFragment : Fragment() {

    lateinit var binding: FragmentWasherHomeBinding
    lateinit var washerMainActivity: WasherMainActivity
    lateinit var navController: NavController
    var activeOrders: ArrayList<ActiveOrder> = ArrayList()
    var pendingOrders: ArrayList<PendingOrder> = ArrayList()
    var notifications: ArrayList<Notification> = ArrayList()
    lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private val LOCATION_CODE = 100
    var type = "active"
    var latitude = ""
    var longitude = ""
    var postalCode = ""
    var address = ""
    var token = ""
    var isOptionsVisible = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentWasherHomeBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is WasherMainActivity) {
            washerMainActivity = context
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
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(washerMainActivity)
    }

    private fun setData(){
        if (AppDefs.user.results!!.image!!.isNotEmpty()){
            Glide.with(washerMainActivity).load(AppDefs.user.results!!.image).into(binding.washerHomeWasherImage)
        }
        binding.washerHomeWelcome.text = resources.getString(R.string.hey_there)+" "+AppDefs.user.results!!.name
        binding.washerHomeSwitchAvailability.isChecked = AppDefs.user.results!!.washer_available == "1"
        binding.washerHomeSwitchDeliveryAvailability.isChecked = AppDefs.user.results!!.dreiver_available == "1"

        getOrders("1")
    }

    private fun onClick(){
        binding.washerHomeSwitchAvailability.setOnClickListener { changeAvailability() }
        binding.washerHomeSwitchDeliveryAvailability.setOnClickListener { changeDeliveryAvailability() }
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
        binding.toolbarLayout.notifications.setOnClickListener { navController.navigate(WasherHomeFragmentDirections.actionWasherHomeFragmentToWasherNotificationsFragment()) }
        binding.toolbarLayout.toolbarNotifyBadge.setOnClickListener { navController.navigate(WasherHomeFragmentDirections.actionWasherHomeFragmentToWasherNotificationsFragment()) }
        binding.toolbarLayout.toolbarLeftIcon.setOnClickListener { navController.navigate(WasherHomeFragmentDirections.actionWasherHomeFragmentToWasherNotificationsFragment()) }
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
                        washerMainActivity,
                        errorResponse.status.massage.toString(),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<DeliveryInfoPrices>, t: Throwable) {
                Toast.makeText(washerMainActivity, resources.getString(R.string.internet_connection), Toast.LENGTH_SHORT).show()
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
            retrofit.create(RetrofitAPIs::class.java).getWasherAvailability()
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
                    Toast.makeText(washerMainActivity, errorResponse.status.massage.toString(), Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<UserData>, t: Throwable) {
                binding.progressBar.visibility = View.GONE
                Toast.makeText(washerMainActivity, resources.getString(R.string.internet_connection), Toast.LENGTH_SHORT).show()
            }

        })
    }

    private fun changeDeliveryAvailability(){
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
            retrofit.create(RetrofitAPIs::class.java).getWasherDeliveryAvailability()
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
                    Toast.makeText(washerMainActivity, errorResponse.status.massage.toString(), Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<UserData>, t: Throwable) {
                binding.progressBar.visibility = View.GONE
                Toast.makeText(washerMainActivity, resources.getString(R.string.internet_connection), Toast.LENGTH_SHORT).show()
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
            retrofit.create(RetrofitAPIs::class.java).getWasherActiveOrders()
        ordersCall.enqueue(object : Callback<ActiveOrders> {
            override fun onResponse(call: Call<ActiveOrders>, response: Response<ActiveOrders>) {
                binding.progressBar.visibility = View.GONE
                if (response.isSuccessful){
                    for (order in response.body()!!.results){
                        if (order.Orders.size != 0){
                            activeOrders.add(order)
                        }
                    }
                    setActiveOrdersAdapter()
                }
            }

            override fun onFailure(call: Call<ActiveOrders>, t: Throwable) {
                binding.progressBar.visibility = View.GONE
                Toast.makeText(washerMainActivity, resources.getString(R.string.internet_connection), Toast.LENGTH_SHORT).show()
            }

        })
    }

    private fun setActiveOrdersAdapter(){
        val adapter = ActiveOrdersAdapter(this, activeOrders)
        binding.ordersRV.adapter = adapter
        binding.ordersRV.layoutManager = LinearLayoutManager(washerMainActivity)
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
            retrofit.create(RetrofitAPIs::class.java).getWasherPendingOrders()
        ordersCall.enqueue(object : Callback<PendingOrders> {
            override fun onResponse(call: Call<PendingOrders>, response: Response<PendingOrders>) {
                binding.progressBar.visibility = View.GONE
                if (response.isSuccessful){
                    for (order in response.body()!!.results){
                        if (order.Orders.size != 0){
                            pendingOrders.add(order)
                        }
                    }
                    setPendingOrdersAdapter()
                }else{
                    val gson = Gson()
                    val type = object : TypeToken<ErrorResponse>() {}.type //ErrorResponse is the data class that matches the error response
                    val errorResponse = gson.fromJson<ErrorResponse>(response.errorBody()!!.charStream(), type) // errorResponse is an instance of ErrorResponse that will contain details about the error
                    Toast.makeText(washerMainActivity, errorResponse.status.massage.toString(), Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<PendingOrders>, t: Throwable) {
                binding.progressBar.visibility = View.GONE
                Toast.makeText(washerMainActivity, resources.getString(R.string.internet_connection), Toast.LENGTH_SHORT).show()
            }

        })
    }

    private fun setPendingOrdersAdapter(){
        val adapter = PendingOrdersAdapter(this, pendingOrders)
        binding.ordersRV.adapter = adapter
        binding.ordersRV.layoutManager = LinearLayoutManager(washerMainActivity)
    }

    private fun getOrders(status: String){
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
            retrofit.create(RetrofitAPIs::class.java).getWasherOrderHistory(statusStr)
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
                        washerMainActivity,
                        errorResponse.status.massage.toString(),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<OrderHistoryResponse>, t: Throwable) {
                Toast.makeText(washerMainActivity, resources.getString(R.string.internet_connection), Toast.LENGTH_SHORT).show()
            }

        })
    }

    fun pendingOrderDetails(order: PendingOrderObj){
        AppDefs.pendingOrder = order
        navController.navigate(WasherHomeFragmentDirections.actionWasherHomeFragmentToOrderPlacedOverviewFragment(true))
    }

    fun orderDetails(order: ActiveOrderObj){
        AppDefs.activeOrder = order
        when (order.status) {
            "0" -> {
                navController.navigate(WasherHomeFragmentDirections.actionWasherHomeFragmentToOrderPlacedFragment())
            }
            "1" -> {
                navController.navigate(WasherHomeFragmentDirections.actionWasherHomeFragmentToOrderPlacedOverviewFragment(false))
            }
            "2" -> {
                navController.navigate(WasherHomeFragmentDirections.actionWasherHomeFragmentToOrderAcceptedFragment())
            }
            "4" -> {
                navController.navigate(WasherHomeFragmentDirections.actionWasherHomeFragmentToOrderAcceptedFragment())
            }
            "5" -> {
                navController.navigate(WasherHomeFragmentDirections.actionWasherHomeFragmentToOrderInProgressFragment())
            }
            "6" -> {
                navController.navigate(WasherHomeFragmentDirections.actionWasherHomeFragmentToOrderInProgressFragment())
            }
            "7" -> {
                navController.navigate(WasherHomeFragmentDirections.actionWasherHomeFragmentToOrderInProgressFragment())
            }
            "8" -> {
                navController.navigate(WasherHomeFragmentDirections.actionWasherHomeFragmentToOrderDeliveredFragment())
            }
        }
    }

    private fun saveUserToSharedPreferences() {
        val sharedPreferences =
            washerMainActivity.getSharedPreferences(AppDefs.SHARED_PREF_KEY, Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString(AppDefs.ID_KEY, AppDefs.user.results!!.id)

        val gson = Gson()
        val json = gson.toJson(AppDefs.user)
        editor.putString(AppDefs.USER_KEY, json)
        editor.putString(AppDefs.TYPE_KEY, "2")
        editor.apply()
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
                    Toast.makeText(washerMainActivity, errorResponse.status.massage.toString(), Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Notifications>, t: Throwable) {
                Toast.makeText(washerMainActivity, resources.getString(R.string.internet_connection), Toast.LENGTH_SHORT).show()
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
                    return@addOnCompleteListener
                }
                token = task.result!!
                checkFCMToken()
            }
    }

    private fun checkFCMToken(){
        val userParams = FCMToken(token)
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
        val loginCall: Call<BooleanResponse> =
            retrofit.create(RetrofitAPIs::class.java).checkToken(userParams)
        loginCall.enqueue(object : Callback<BooleanResponse>{
            override fun onResponse(call: Call<BooleanResponse>, response: Response<BooleanResponse>) {
                if (response.isSuccessful){
                    if (!response.body()!!.results){
                        val preferences: SharedPreferences = washerMainActivity.getSharedPreferences(
                            AppDefs.SHARED_PREF_KEY,
                            Context.MODE_PRIVATE
                        )
                        val editor = preferences.edit()
                        editor.clear()
                        editor.apply()
                        val splashIntent = Intent(washerMainActivity, SplashActivity::class.java)
                        startActivity(splashIntent)
                        washerMainActivity.finish()
                    }
                } else{
                    val preferences: SharedPreferences = washerMainActivity.getSharedPreferences(
                        AppDefs.SHARED_PREF_KEY,
                        Context.MODE_PRIVATE
                    )
                    val editor = preferences.edit()
                    editor.clear()
                    editor.apply()
                    val splashIntent = Intent(washerMainActivity, SplashActivity::class.java)
                    startActivity(splashIntent)
                    washerMainActivity.finish()
                }
            }

            override fun onFailure(call: Call<BooleanResponse>, t: Throwable) {
                val preferences: SharedPreferences = washerMainActivity.getSharedPreferences(
                    AppDefs.SHARED_PREF_KEY,
                    Context.MODE_PRIVATE
                )
                val editor = preferences.edit()
                editor.clear()
                editor.apply()
                val splashIntent = Intent(washerMainActivity, SplashActivity::class.java)
                startActivity(splashIntent)
                washerMainActivity.finish()
            }

        })
    }

    private fun getCurrentLocation(){
        if (ActivityCompat.checkSelfPermission(
                washerMainActivity,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(
                washerMainActivity,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermission()
            return
        }
        fusedLocationProviderClient.lastLocation.addOnCompleteListener(washerMainActivity){ task ->
            val location: Location? = task.result
            if (location != null){
                latitude = ""+location.latitude
                longitude = ""+location.longitude
                getAddress(location.latitude, location.longitude)
            }else{
                washerMainActivity.locationEnabled()
            }
        }
    }

    private fun getAddress(latitude: Double, longitude: Double){
        val geocoder: Geocoder
        val addresses: List<Address>
        geocoder = Geocoder(washerMainActivity, Locale.getDefault())

        addresses = geocoder.getFromLocation(
            latitude,
            longitude,
            1
        ) as List<Address>// Here 1 represent max location result to returned, by documents it recommended 1 to 5

        address = addresses[0].getAddressLine(0)

//        address = addresses[0].getAddressLine(0) // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
        if (addresses[0].postalCode != null){
            postalCode = addresses[0].postalCode
        }

        if (AppDefs.user.results!!.latitude!!.isEmpty() || AppDefs.user.results!!.longitude!!.isEmpty()){
            updateLocation()
        }
    }

    private fun updateLocation(){
        val userParams = UpdateLocation(latitude, longitude, address, postalCode)
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
        val updateUserCall: Call<UserData> =
            retrofit.create(RetrofitAPIs::class.java).updateLocation(userParams)
        updateUserCall.enqueue(object : Callback<UserData> {
            override fun onResponse(call: Call<UserData>, response: Response<UserData>) {
                AppDefs.user = response.body()!!
                binding.progressBar.visibility = View.GONE
                saveUserToSharedPreferences()
            }

            override fun onFailure(call: Call<UserData>, t: Throwable) {
            }

        })
    }

    private fun requestPermission(){
        ActivityCompat.requestPermissions(
            washerMainActivity, arrayOf(
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
            }else if (grantResults[0] != PackageManager.PERMISSION_GRANTED){
                requestPermission()
            }
        }
    }
}