package com.washathomes.views.main.washer.home

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
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
import dagger.hilt.android.AndroidEntryPoint
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

@AndroidEntryPoint
class WasherHomeFragment : Fragment() {

    lateinit var binding: FragmentWasherHomeBinding
    lateinit var washerMainActivity: WasherMainActivity
    lateinit var navController: NavController
    var activeOrders: ArrayList<ActiveOrder> = ArrayList()
    var pendingOrders: ArrayList<PendingOrder> = ArrayList()
    var type = "active"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val layout = inflater.inflate(R.layout.fragment_washer_home, container, false)
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
        getActiveOrders()
    }

    private fun initViews(view: View){
        navController = Navigation.findNavController(view)
    }

    private fun setData(){
        if (AppDefs.user.results!!.image!!.isNotEmpty()){
            Glide.with(washerMainActivity).load(AppDefs.user.results!!.image).into(binding.washerHomeWasherImage)
        }
        binding.washerHomeWelcome.text = resources.getString(R.string.hey_there)+" "+AppDefs.user.results!!.name
        binding.washerHomeSwitchAvailability.isChecked = AppDefs.user.results!!.washer_available == "1"
        binding.washerHomeSwitchDeliveryAvailability.isChecked = AppDefs.user.results!!.dreiver_available == "1"
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
        binding.toolbarLayout.toolbarLeftIcon.setOnClickListener { navController.navigate(WasherHomeFragmentDirections.actionWasherHomeFragmentToWasherNotificationsFragment()) }
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
                    activeOrders = response.body()!!.results
                    setActiveOrdersAdapter()
                }else{
//                    val gson = Gson()
//                    val type = object : TypeToken<ErrorResponse>() {}.type //ErrorResponse is the data class that matches the error response
//                    val errorResponse = gson.fromJson<ErrorResponse>(response.errorBody()!!.charStream(), type) // errorResponse is an instance of ErrorResponse that will contain details about the error
//                    Toast.makeText(washerMainActivity, errorResponse.status.massage.toString(), Toast.LENGTH_SHORT).show()
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
                    pendingOrders = response.body()!!.results
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
}