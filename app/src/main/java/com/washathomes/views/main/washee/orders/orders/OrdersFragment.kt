package com.washathomes.views.main.washee.orders.orders

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.washathomes.R
import com.washathomes.apputils.appdefs.AppDefs
import com.washathomes.apputils.appdefs.Urls
import com.washathomes.apputils.modules.ErrorResponse
import com.washathomes.apputils.modules.WasheeActiveOrder
import com.washathomes.apputils.modules.WasheeOrders
import com.washathomes.apputils.modules.chatmodel.Order
import com.washathomes.apputils.remote.RetrofitAPIs
import com.washathomes.databinding.FragmentOrdersBinding
import com.washathomes.retrofit.Resource
import com.washathomes.views.main.washee.WasheeMainActivity
import com.washathomes.views.main.washee.chats.WasheeInboxViewModel
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
import kotlin.collections.HashMap

@AndroidEntryPoint
class OrdersFragment : Fragment() {

    lateinit var binding: FragmentOrdersBinding
    lateinit var washeeMainActivity: WasheeMainActivity
    lateinit var navController: NavController
    var orders: ArrayList<WasheeActiveOrder> = ArrayList()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val layout = inflater.inflate(R.layout.fragment_orders, container, false)
        binding = FragmentOrdersBinding.inflate(layoutInflater)
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
        getOrders()
    }

    private fun initViews(view: View) {
        navController = Navigation.findNavController(view)

    }

    private fun onClick() {
        binding.toolbarLayout.clLeft.setOnClickListener {
            navController.navigate(
                OrdersFragmentDirections.actionNavigationOrdersToWasheeNotificationsFragment()
            )
        }
        binding.toolbarLayout.clRight.setOnClickListener {
            navController.navigate(
                OrdersFragmentDirections.actionNavigationOrdersToBasketFragment()
            )
        }
    }

    private fun getOrders() {
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
        val languagesCall: Call<WasheeOrders> =
            retrofit.create(RetrofitAPIs::class.java).getWasheeActiveOrders()
        languagesCall.enqueue(object : Callback<WasheeOrders> {
            override fun onResponse(call: Call<WasheeOrders>, response: Response<WasheeOrders>) {
                binding.progressBar.visibility = View.GONE
                if (response.isSuccessful) {
                    orders = response.body()!!.results
                    setOrdersRV()
                } else {
                    val gson = Gson()
                    val type = object :
                        TypeToken<ErrorResponse>() {}.type //ErrorResponse is the data class that matches the error response
                    val errorResponse = gson.fromJson<ErrorResponse>(
                        response.errorBody()!!.charStream(),
                        type
                    ) // errorResponse is an instance of ErrorResponse that will contain details about the error
                    Toast.makeText(
                        washeeMainActivity,
                        errorResponse.status.massage.toString(),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<WasheeOrders>, t: Throwable) {
                Toast.makeText(
                    washeeMainActivity,
                    resources.getString(R.string.internet_connection),
                    Toast.LENGTH_SHORT
                ).show()
            }

        })
    }

    private fun setOrdersRV() {
        val adapter = OrdersAdapter(this, orders)
        binding.ordersRV.adapter = adapter
        binding.ordersRV.layoutManager = LinearLayoutManager(washeeMainActivity)
    }

    fun orderDetails(order: WasheeActiveOrder) {
        AppDefs.washeeActiveOrder = order


        when (order.status) {
            "0" -> {
                navController.navigate(
                    OrdersFragmentDirections.actionNavigationOrdersToOrderPlacedFragment3(

                    )
                )


            }
            "1" -> {
                navController.navigate(OrdersFragmentDirections.actionNavigationOrdersToOrderPlacedFragment3())
            }
            "2" -> {
                navController.navigate(
                    OrdersFragmentDirections.actionNavigationOrdersToOrderConfirmedFragment(
                        toMapper(order)
                    )
                )
            }
            "4" -> {
                navController.navigate(OrdersFragmentDirections.actionNavigationOrdersToOrderInProgressFragment2(
                    toMapper(order)
                ))
            }
            "5" -> {
                navController.navigate(OrdersFragmentDirections.actionNavigationOrdersToOrderInProgressFragment2(
                    toMapper(order)
                ))
            }
            "6" -> {
                navController.navigate(OrdersFragmentDirections.actionNavigationOrdersToOrderInProgressFragment2(
                    toMapper(order)
                ))
            }
            "7" -> {
                navController.navigate(OrdersFragmentDirections.actionNavigationOrdersToOrderInProgressFragment2(
                    toMapper(order)
                ))
            }
            "8" -> {
                navController.navigate(OrdersFragmentDirections.actionNavigationOrdersToOrderCompletedFragment())
            }
        }
    }

    fun toMapper(data: WasheeActiveOrder): Order {
        /*  roomKey = this.id.toString(),
          buyerId = this.buyer_user_id.toString(),
          sellerId = this.seller_user_id.toString(),
          driverId = this.driver_user_id.toString(),*/
        return Order(
            data.id!!.toInt(),
            "",
            0.0,
            0.0,
            0.0,
            0.0,
            0.0,
            0.0,
            0.0,
            "",
            "",
            Date(),
            "",
            "",
            0,

            Date(),
            0,
            "",
            "",
            "",
            "",
            "",
            Date(),
            "",
            "",
            Date(),
            "",
            0,
            Date(),
            Date(),
            Date(),
            Date(),
            Date(),
            Date(),
            "",
            0.0,
            0,
            "",
            "",
            "",

            Date(),
            "",
            "",
            "",
            Date(),
            0,
            0.0,
            0,
            0,
            0,
            "",
            "",
            "",
            Date(),
            "",
            "",
            "",
            0,
            "",
            "",
            "",
            Date(),
            "",
            0,
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            Date(),
            "",
            0,
            0,
            "",
            Date(),
            0,
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            Date(),
            Date(),


            )


    }



}