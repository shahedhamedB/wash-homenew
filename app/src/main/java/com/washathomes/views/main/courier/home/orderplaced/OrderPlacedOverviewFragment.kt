package com.washathomes.views.main.courier.home.orderplaced

import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.location.Address
import android.location.Geocoder
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.fragment.navArgs
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.washathomes.apputils.appdefs.AppDefs
import com.washathomes.apputils.appdefs.Urls
import com.washathomes.apputils.modules.BooleanResponse
import com.washathomes.apputils.modules.ErrorResponse
import com.washathomes.apputils.modules.OrderObj
import com.washathomes.apputils.remote.RetrofitAPIs
import com.washathomes.R
import com.washathomes.views.main.courier.CourierMainActivity
import com.washathomes.databinding.FragmentOrderPlacedOverview2Binding
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
class OrderPlacedOverviewFragment : Fragment() {

    lateinit var binding: FragmentOrderPlacedOverview2Binding
    lateinit var courierMainActivity: CourierMainActivity
    lateinit var navController: NavController
    val args: OrderPlacedOverviewFragmentArgs by navArgs()
    var washerPending = false
    var deliveryAddress = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val layout = inflater.inflate(R.layout.fragment_order_placed_overview2, container, false)
        binding = FragmentOrderPlacedOverview2Binding.inflate(layoutInflater)
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
        onClick()
        setData()
    }

    private fun initViews(view: View){
        navController = Navigation.findNavController(view)
        washerPending = args.isPending
    }

    private fun onClick(){
        binding.toolbarBackIcon.setOnClickListener { navController.popBackStack() }
        binding.pendingOverviewAcceptBtn.setOnClickListener { acceptOrderPopUp() }
        binding.pendingOverviewRejectBtn.setOnClickListener { rejectOrderPopUp() }
        binding.viewItemsLayout.setOnClickListener { navController.navigate(OrderPlacedOverviewFragmentDirections.actionOrderPlacedOverviewFragment2ToViewItemsFragment2(true)) }
    }

    private fun setData(){
        if (washerPending){
            getAddress(AppDefs.pendingOrder.pickup_lat.toDouble(), AppDefs.pendingOrder.picku_long.toDouble())
            binding.pendingOverviewCurrentStatus.visibility = View.GONE
            binding.actionButtonsLayout.visibility = View.VISIBLE
            binding.completeBuyerNameText.text = AppDefs.pendingOrder.washee_name!!.name
            if (AppDefs.pendingOrder.is_delivery_pickup == "1"){
                binding.overviewDeliveryOptionText.text = resources.getString(R.string.washer_driver_to_collect_amp_return)
            }else{
                binding.overviewDeliveryOptionText.text = resources.getString(R.string.self_drop_off_amp_pickup)
            }
            if (AppDefs.pendingOrder.is_express == "1"){
                binding.overviewServiceSpeedText.text = resources.getString(R.string.express_delivery)+" "+resources.getString(R.string.hours24)
            }else{
                binding.overviewServiceSpeedText.text = resources.getString(R.string.normal_delivery)+" "+resources.getString(R.string.hours48)
            }
            binding.orderPlacedOrderNo.text = resources.getString(R.string.order)+" #"+ AppDefs.pendingOrder.id
            binding.orderPlacedStatus.text = resources.getString(R.string.order_placed_on)+" "+ AppDefs.pendingOrder.time+" "+ AppDefs.pendingOrder.date
            binding.overviewDeliveryAddressText.text = deliveryAddress
            binding.overviewCollectionTimeDay.text = AppDefs.pendingOrder.pickup_date
            binding.overviewReturnTimeDay.text = AppDefs.pendingOrder.delivery_date
            binding.overviewCollectionTime.text = AppDefs.pendingOrder.pickup_time
            binding.overviewReturnTime.text = AppDefs.pendingOrder.delivery_time
            binding.viewItems.text = resources.getString(R.string.view_items)+" ("+ AppDefs.pendingOrder.orders_items.size+")"
            binding.pendingOverviewTotalEarningsValue.text = ""+ AppDefs.pendingOrder.total_amount
            binding.pendingOverviewOrderAmountValue.text = ""+ AppDefs.pendingOrder.sub_total
            binding.pendingOverviewDeliveryPaymentValue.text = ""+ AppDefs.pendingOrder.delivery_amount
        }else{
            getAddress(AppDefs.activeOrder.dropoff_lat.toDouble(), AppDefs.activeOrder.dropoff_long.toDouble())
            binding.pendingOverviewCurrentStatus.visibility = View.VISIBLE
            binding.actionButtonsLayout.visibility = View.GONE
            binding.completeBuyerNameText.text = AppDefs.activeOrder.washee_name!!.name
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
            binding.pendingOverviewTotalEarningsValue.text = ""+ AppDefs.activeOrder.total_amount
            binding.pendingOverviewOrderAmountValue.text = ""+ AppDefs.activeOrder.sub_total
            binding.pendingOverviewDeliveryPaymentValue.text = ""+ AppDefs.activeOrder.delivery_amount
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
        ) // Here 1 represent max location result to returned, by documents it recommended 1 to 5

        // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
        deliveryAddress = addresses[0].getAddressLine(0)

    }

    private fun acceptOrderPopUp(){
        val alertView: View =
            LayoutInflater.from(context).inflate(R.layout.accept_order_popup, null)
        val alertBuilder = AlertDialog.Builder(context).setView(alertView).show()
        alertBuilder.show()
        alertBuilder.setCancelable(false)

        alertBuilder.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val cancel: TextView = alertView.findViewById(R.id.cancel)
        val accept: TextView = alertView.findViewById(R.id.accept)

        cancel.setOnClickListener { alertBuilder.dismiss() }
        accept.setOnClickListener {
            acceptOrder()
            alertBuilder.dismiss()
        }
    }

    private fun rejectOrderPopUp(){
        val alertView: View =
            LayoutInflater.from(context).inflate(R.layout.reject_order_popup, null)
        val alertBuilder = AlertDialog.Builder(context).setView(alertView).show()
        alertBuilder.show()
        alertBuilder.setCancelable(false)

        alertBuilder.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val cancel: TextView = alertView.findViewById(R.id.cancel)
        val reject: TextView = alertView.findViewById(R.id.reject)

        cancel.setOnClickListener { alertBuilder.dismiss() }
        reject.setOnClickListener {
            rejectOrder()
            alertBuilder.dismiss()}
    }

    private fun acceptOrder(){
        binding.progressBar.visibility = View.VISIBLE
        val orderObj = OrderObj(AppDefs.pendingOrder.id)
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
            retrofit.create(RetrofitAPIs::class.java).courierAcceptOrder(orderObj)
        ordersCall.enqueue(object : Callback<BooleanResponse> {
            override fun onResponse(call: Call<BooleanResponse>, response: Response<BooleanResponse>) {
                binding.progressBar.visibility = View.GONE
                if (response.isSuccessful){
                    Toast.makeText(courierMainActivity, resources.getString(R.string.order_accepted), Toast.LENGTH_SHORT).show()
                    navController.popBackStack()
                }else{
                    val gson = Gson()
                    val type = object : TypeToken<ErrorResponse>() {}.type //ErrorResponse is the data class that matches the error response
                    val errorResponse = gson.fromJson<ErrorResponse>(response.errorBody()!!.charStream(), type) // errorResponse is an instance of ErrorResponse that will contain details about the error
                    Toast.makeText(courierMainActivity, errorResponse.status.massage.toString(), Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<BooleanResponse>, t: Throwable) {
                binding.progressBar.visibility = View.GONE
                Toast.makeText(courierMainActivity, resources.getString(R.string.internet_connection), Toast.LENGTH_SHORT).show()
            }

        })
    }

    private fun rejectOrder(){
        binding.progressBar.visibility = View.VISIBLE
        val orderObj = OrderObj(AppDefs.pendingOrder.id)
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
            retrofit.create(RetrofitAPIs::class.java).courierRejectOrder(orderObj)
        ordersCall.enqueue(object : Callback<BooleanResponse> {
            override fun onResponse(call: Call<BooleanResponse>, response: Response<BooleanResponse>) {
                binding.progressBar.visibility = View.GONE
                if (response.isSuccessful){
                    Toast.makeText(courierMainActivity, resources.getString(R.string.order_rejected), Toast.LENGTH_SHORT).show()
                    navController.popBackStack()
                }else{
                    val gson = Gson()
                    val type = object : TypeToken<ErrorResponse>() {}.type //ErrorResponse is the data class that matches the error response
                    val errorResponse = gson.fromJson<ErrorResponse>(response.errorBody()!!.charStream(), type) // errorResponse is an instance of ErrorResponse that will contain details about the error
                    Toast.makeText(courierMainActivity, errorResponse.status.massage.toString(), Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<BooleanResponse>, t: Throwable) {
                binding.progressBar.visibility = View.GONE
                Toast.makeText(courierMainActivity, resources.getString(R.string.internet_connection), Toast.LENGTH_SHORT).show()
            }

        })
    }

}