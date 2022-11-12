package com.washathomes.views.main.washee.orders.placed

import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.washathomes.apputils.appdefs.AppDefs
import com.washathomes.R
import com.washathomes.apputils.appdefs.Urls
import com.washathomes.apputils.modules.BooleanResponse
import com.washathomes.apputils.modules.DeleteOrder
import com.washathomes.apputils.modules.ErrorResponse
import com.washathomes.apputils.remote.RetrofitAPIs
import com.washathomes.views.main.washee.WasheeMainActivity
import com.washathomes.databinding.FragmentOrderPlacedBinding
import dagger.hilt.android.AndroidEntryPoint
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

@AndroidEntryPoint
class OrderPlacedFragment : Fragment() {

    lateinit var binding: FragmentOrderPlacedBinding
    lateinit var washeeMainActivity: WasheeMainActivity
    lateinit var navController: NavController

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val layout = inflater.inflate(R.layout.fragment_order_placed, container, false)
        binding = FragmentOrderPlacedBinding.inflate(layoutInflater)
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
        setData()
    }

    private fun initViews(view: View){
        navController = Navigation.findNavController(view)

    }

    private fun onClick(){
        binding.toolbarBackIcon.setOnClickListener { navController.popBackStack() }
        binding.cancelOrder.setOnClickListener { cancelOrderPopUp() }
    }

    private fun setData(){
        binding.orderPlacedOrderNo.text = "#"+AppDefs.washeeActiveOrder.id
        binding.orderPlacedStatus.text = resources.getString(R.string.order_placed_on)+" "+AppDefs.washeeActiveOrder.time+" "+AppDefs.washeeActiveOrder.date
    }

    private fun cancelOrderPopUp(){
        val alertView: View =
            LayoutInflater.from(context).inflate(R.layout.delete_order_popup, null)
        val alertBuilder = AlertDialog.Builder(context).setView(alertView).show()
        alertBuilder.show()
        alertBuilder.setCancelable(false)

        alertBuilder.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val feedbackEdt: EditText = alertView.findViewById(R.id.feedback_dialog_input)
        val cancel: Button = alertView.findViewById(R.id.feedback_dialog_cancel)
        val submit: Button = alertView.findViewById(R.id.feedback_dialog_submit)

        cancel.setOnClickListener { alertBuilder.dismiss() }
        submit.setOnClickListener {
            val feedback = feedbackEdt.text.toString()
            deleteOrder(AppDefs.washeeActiveOrder.id, feedback)
            alertBuilder.dismiss()
        }
    }

    private fun deleteOrder(orderId: String, feedback: String){
        val deleteOrder = DeleteOrder(orderId, feedback)
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
        val deleteOrderCall: Call<BooleanResponse> =
            retrofit.create(RetrofitAPIs::class.java).cancelOrder(deleteOrder)
        deleteOrderCall.enqueue(object : Callback<BooleanResponse> {
            override fun onResponse(call: Call<BooleanResponse>, response: Response<BooleanResponse>) {
                if (response.isSuccessful) {
                    Toast.makeText(washeeMainActivity, resources.getString(R.string.order_deleted_successfully), Toast.LENGTH_SHORT).show()
                    navController.popBackStack()
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

}