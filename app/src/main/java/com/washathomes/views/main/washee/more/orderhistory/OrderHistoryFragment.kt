package com.washathomes.views.main.washee.more.orderhistory

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.washathomes.apputils.appdefs.AppDefs
import com.washathomes.apputils.appdefs.Urls
import com.washathomes.apputils.modules.*
import com.washathomes.apputils.remote.RetrofitAPIs
import com.washathomes.R
import com.washathomes.views.main.washee.more.orderhistory.adapters.OrdersAdapter
import com.washathomes.views.main.washee.WasheeMainActivity
import com.washathomes.databinding.FragmentOrderHistoryBinding
import dagger.hilt.android.AndroidEntryPoint
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import kotlin.collections.ArrayList
@AndroidEntryPoint
class OrderHistoryFragment : Fragment() {

    lateinit var binding: FragmentOrderHistoryBinding
    lateinit var washeeMainActivity: WasheeMainActivity
    lateinit var navController: NavController
    var completedOrders: ArrayList<Order> = ArrayList()
    var page = 1

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val layout = inflater.inflate(R.layout.fragment_order_history, container, false)
        binding = FragmentOrderHistoryBinding.inflate(layoutInflater)
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
        getOrders("1")
        setSpinner(binding.filterSpinner)
    }

    private fun initViews(view: View){
        navController = Navigation.findNavController(view)

    }

    private fun onClick(){
        binding.toolbarBackIcon.setOnClickListener { navController.popBackStack() }
        binding.completedOrders.setOnClickListener {
            binding.completedOrders.setTextColor(resources.getColor(R.color.blue))
            binding.deletedOrders.setTextColor(resources.getColor(R.color.mid_grey))
            getOrders("1")
        }
        binding.deletedOrders.setOnClickListener {
            binding.completedOrders.setTextColor(resources.getColor(R.color.mid_grey))
            binding.deletedOrders.setTextColor(resources.getColor(R.color.blue))
            getOrders("3")
        }
    }

    private fun getOrders(status: String){
        binding.progressBar.visibility = View.VISIBLE
        val orderHistoryObj = OrderHistoryObj(status, page.toString())
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
        val languagesCall: Call<OrderHistory> =
            retrofit.create(RetrofitAPIs::class.java).getOrderHistory(orderHistoryObj)
        languagesCall.enqueue(object : Callback<OrderHistory> {
            override fun onResponse(call: Call<OrderHistory>, response: Response<OrderHistory>) {
                binding.progressBar.visibility = View.GONE
                if (response.isSuccessful) {
                    completedOrders = response.body()!!.results
                    setOrdersRV(completedOrders)
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

            override fun onFailure(call: Call<OrderHistory>, t: Throwable) {
                Toast.makeText(washeeMainActivity, resources.getString(R.string.internet_connection), Toast.LENGTH_SHORT).show()
            }

        })
    }

    private fun setOrdersRV(orders: ArrayList<Order>){
        val adapter = OrdersAdapter(this, orders)
        binding.orderHistoryRV.adapter = adapter
        binding.orderHistoryRV.layoutManager = LinearLayoutManager(washeeMainActivity)
    }

    private fun setSpinner(spinner: Spinner) {
        val items = ArrayList<String?>()
        items.add(resources.getString(R.string.all))
        items.add(resources.getString(R.string.today))
        items.add(resources.getString(R.string.week))
        items.add(resources.getString(R.string.month))
        items.add(resources.getString(R.string.year))
        val sortAdapter: ArrayAdapter<*> = ArrayAdapter<Any?>(requireContext(), android.R.layout.simple_spinner_dropdown_item, items as List<Any?>)
        sortAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = sortAdapter

//        getTodayOrders()
    }

    fun navigateToDetails(order: Order){
        AppDefs.order = order
        navController.navigate(OrderHistoryFragmentDirections.actionOrderHistoryFragmentToOrderDetailsFragment())
    }

}