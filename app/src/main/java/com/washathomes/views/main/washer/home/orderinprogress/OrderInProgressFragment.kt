package com.washathomes.views.main.washer.home.orderinprogress

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.location.Address
import android.location.Geocoder
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.washathomes.apputils.appdefs.AppDefs
import com.washathomes.apputils.appdefs.Urls
import com.washathomes.apputils.modules.BooleanResponse
import com.washathomes.apputils.modules.ErrorResponse
import com.washathomes.apputils.modules.OrderObj
import com.washathomes.apputils.remote.RetrofitAPIs
import com.washathomes.R
import com.washathomes.views.main.washer.WasherMainActivity
import com.washathomes.databinding.FragmentOrderInProgress2Binding
import dagger.hilt.android.AndroidEntryPoint
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.lang.String
import java.util.*
@AndroidEntryPoint
class OrderInProgressFragment : Fragment() {

    lateinit var binding: FragmentOrderInProgress2Binding
    lateinit var washerMainActivity: WasherMainActivity
    lateinit var navController: NavController
    var deliveryAddress = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val layout = inflater.inflate(R.layout.fragment_order_in_progress2, container, false)
        binding = FragmentOrderInProgress2Binding.inflate(layoutInflater)
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
        onClick()
        setData()
    }

    private fun initViews(view: View){
        navController = Navigation.findNavController(view)
        when (AppDefs.activeOrder.status) {
            "5" -> {
                binding.directionsBtn.visibility = View.GONE
                binding.collect.text = resources.getString(R.string.finished_laundry)
            }
            "6" -> {
                if (AppDefs.activeOrder.washer_is_delevery == "1"){
                    binding.directionsActionBtn.visibility = View.VISIBLE
                    binding.directionsBtn.visibility = View.VISIBLE
                }else{
                    binding.directionsActionBtn.visibility = View.GONE
                    binding.directionsBtn.visibility = View.GONE
                }
                binding.collect.text = resources.getString(R.string.dropped_laundry_items)
            }
            "7" ->{
                binding.directionsActionBtn.visibility = View.GONE
                binding.directionsBtn.visibility = View.GONE
            }
        }
    }

    private fun onClick(){
        binding.toolbarBackIcon.setOnClickListener { navController.popBackStack() }
        binding.viewItemsLayout.setOnClickListener { navController.navigate(
            OrderInProgressFragmentDirections.actionOrderInProgressFragmentToWasherViewItemsFragment(false)) }
        binding.directionsActionBtn.setOnClickListener {
            when (AppDefs.activeOrder.status) {
                "5" -> {
                    finishOrderPopUp()
                }
                "6" -> {
                    droppedOffOrderPopUp()
                }
            }
        }
        binding.directionsBtn.setOnClickListener { openGoogleMaps() }
    }

    private fun setData(){
        getAddress(AppDefs.activeOrder.dropoff_lat.toDouble(), AppDefs.activeOrder.dropoff_long.toDouble())
        if (AppDefs.activeOrder.is_delivery_pickup == "1"){
            binding.overviewDeliveryOptionText.text = resources.getString(R.string.washer_driver_to_collect_amp_return)
        }else{
            binding.overviewDeliveryOptionText.text = resources.getString(R.string.self_drop_off_amp_pickup)
        }
        if (AppDefs.activeOrder.is_express == "1"){
            binding.overviewServiceSpeedText.text = resources.getString(R.string.express_delivery)+" "+resources.getString(R.string.hours24)
        }else{
            binding.overviewServiceSpeedText.text = resources.getString(R.string.normal_delivery)+" "+resources.getString(R.string.hours48)
        }
        binding.orderPlacedOrderNo.text = resources.getString(R.string.order)+" #"+ AppDefs.activeOrder.id
        binding.orderPlacedStatus.text = resources.getString(R.string.order_placed_on)+" "+ AppDefs.activeOrder.time+" "+ AppDefs.activeOrder.date
        binding.overviewDeliveryAddressText.text = deliveryAddress
        binding.overviewCollectionTimeDay.text = AppDefs.activeOrder.pickup_date
        binding.overviewReturnTimeDay.text = AppDefs.activeOrder.delivery_date
        binding.overviewCollectionTime.text = AppDefs.activeOrder.pickup_time
        binding.overviewReturnTime.text = AppDefs.activeOrder.delivery_time
        binding.viewItems.text = resources.getString(R.string.view_items)+" ("+ AppDefs.activeOrder.orders_items.size+")"
    }

    private fun getAddress(latitude: Double, longitude: Double){
        val geocoder: Geocoder
        val addresses: List<Address>
        geocoder = Geocoder(washerMainActivity, Locale.getDefault())

        addresses = geocoder.getFromLocation(
            latitude,
            longitude,
            1
        ) // Here 1 represent max location result to returned, by documents it recommended 1 to 5

        // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
        deliveryAddress = addresses[0].getAddressLine(0)

    }

    private fun finishOrderPopUp(){
        val alertView: View =
            LayoutInflater.from(context).inflate(R.layout.finish_order_popup, null)
        val alertBuilder = AlertDialog.Builder(context).setView(alertView).show()
        alertBuilder.show()
        alertBuilder.setCancelable(false)

        alertBuilder.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val cancel: TextView = alertView.findViewById(R.id.cancel)
        val finishedLaundry: TextView = alertView.findViewById(R.id.finished_laundry)

        cancel.setOnClickListener { alertBuilder.dismiss() }
        finishedLaundry.setOnClickListener {
            finishOrder()
            alertBuilder.dismiss()
        }
    }

    private fun finishOrder(){
        binding.progressBar.visibility = View.VISIBLE
        val orderObj = OrderObj(AppDefs.activeOrder.id)
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
        val ordersCall: Call<BooleanResponse> =
            retrofit.create(RetrofitAPIs::class.java).washerFinishOrder(orderObj)
        ordersCall.enqueue(object : Callback<BooleanResponse> {
            override fun onResponse(call: Call<BooleanResponse>, response: Response<BooleanResponse>) {
                binding.progressBar.visibility = View.GONE
                if (response.isSuccessful){
                    Toast.makeText(washerMainActivity, resources.getString(R.string.finished_laundry), Toast.LENGTH_SHORT).show()
                    navController.popBackStack()
                }else{
                    val gson = Gson()
                    val type = object : TypeToken<ErrorResponse>() {}.type //ErrorResponse is the data class that matches the error response
                    val errorResponse = gson.fromJson<ErrorResponse>(response.errorBody()!!.charStream(), type) // errorResponse is an instance of ErrorResponse that will contain details about the error
                    Toast.makeText(washerMainActivity, errorResponse.status.massage.toString(), Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<BooleanResponse>, t: Throwable) {
                binding.progressBar.visibility = View.GONE
                Toast.makeText(washerMainActivity, resources.getString(R.string.internet_connection), Toast.LENGTH_SHORT).show()
            }

        })
    }

    private fun droppedOffOrderPopUp(){
        val alertView: View =
            LayoutInflater.from(context).inflate(R.layout.dropped_off_order_popup, null)
        val alertBuilder = AlertDialog.Builder(context).setView(alertView).show()
        alertBuilder.show()
        alertBuilder.setCancelable(false)

        alertBuilder.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val cancel: TextView = alertView.findViewById(R.id.cancel)
        val droppedOff: TextView = alertView.findViewById(R.id.dropped_off)

        cancel.setOnClickListener { alertBuilder.dismiss() }
        droppedOff.setOnClickListener {
            droppedOffOrder()
            alertBuilder.dismiss()
        }
    }

    private fun droppedOffOrder(){
        binding.progressBar.visibility = View.VISIBLE
        val orderObj = OrderObj(AppDefs.activeOrder.id)
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
        val ordersCall: Call<BooleanResponse> =
            retrofit.create(RetrofitAPIs::class.java).washerDropOffOrder(orderObj)
        ordersCall.enqueue(object : Callback<BooleanResponse> {
            override fun onResponse(call: Call<BooleanResponse>, response: Response<BooleanResponse>) {
                binding.progressBar.visibility = View.GONE
                if (response.isSuccessful){
                    Toast.makeText(washerMainActivity, resources.getString(R.string.order_dropped_off), Toast.LENGTH_SHORT).show()
                    navController.popBackStack()
                }else{
                    val gson = Gson()
                    val type = object : TypeToken<ErrorResponse>() {}.type //ErrorResponse is the data class that matches the error response
                    val errorResponse = gson.fromJson<ErrorResponse>(response.errorBody()!!.charStream(), type) // errorResponse is an instance of ErrorResponse that will contain details about the error
                    Toast.makeText(washerMainActivity, errorResponse.status.massage.toString(), Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<BooleanResponse>, t: Throwable) {
                binding.progressBar.visibility = View.GONE
                Toast.makeText(washerMainActivity, resources.getString(R.string.internet_connection), Toast.LENGTH_SHORT).show()
            }

        })
    }

    private fun openGoogleMaps(){
        val uri = String.format(Locale.ENGLISH, "geo:%f,%f", AppDefs.activeOrder.lat.toFloat(), AppDefs.activeOrder.long.toFloat())
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
        startActivity(intent)
    }

}