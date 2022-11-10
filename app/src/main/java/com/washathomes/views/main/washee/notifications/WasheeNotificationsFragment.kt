package com.washathomes.views.main.washee.notifications

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import com.washathomes.views.main.washee.WasheeMainActivity
import com.washathomes.databinding.FragmentWasheeNotificationsBinding
import dagger.hilt.android.AndroidEntryPoint
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.ArrayList
@AndroidEntryPoint
class WasheeNotificationsFragment : Fragment() {

    lateinit var binding: FragmentWasheeNotificationsBinding
    lateinit var navController: NavController
    lateinit var washeeMainActivity: WasheeMainActivity
    var notifications: ArrayList<Notification> = ArrayList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val layout = inflater.inflate(R.layout.fragment_washee_notifications, container, false)
        binding = FragmentWasheeNotificationsBinding.inflate(layoutInflater)
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
        getNotifications()
    }

    private fun initViews(view: View){
        navController = Navigation.findNavController(view)
    }

    private fun onClick(){
        binding.toolbarBackIcon.setOnClickListener { navController.popBackStack() }
    }

    private fun getNotifications(){
        binding.progressBar.visibility = View.VISIBLE
        notifications.clear()
        val userTypeObj = UserTypeObj("1")
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
                binding.progressBar.visibility = View.GONE
                if (response.isSuccessful){
                    notifications = response.body()!!.results.notifications
                    setNotificationsRV()
                }else{
                    val gson = Gson()
                    val type = object : TypeToken<ErrorResponse>() {}.type //ErrorResponse is the data class that matches the error response
                    val errorResponse = gson.fromJson<ErrorResponse>(response.errorBody()!!.charStream(), type) // errorResponse is an instance of ErrorResponse that will contain details about the error
                    Toast.makeText(washeeMainActivity, errorResponse.status.massage.toString(), Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Notifications>, t: Throwable) {
                binding.progressBar.visibility = View.GONE
                Toast.makeText(washeeMainActivity, resources.getString(R.string.internet_connection), Toast.LENGTH_SHORT).show()
            }

        })
    }

    private fun setNotificationsRV(){
        val adapter = NotificationsAdapter(this, notifications)
        binding.notificationRecyclerview.adapter = adapter
        binding.notificationRecyclerview.layoutManager = LinearLayoutManager(washeeMainActivity)
    }

    fun deleteNotification(notification: Notification){
        binding.progressBar.visibility = View.VISIBLE
        val notificationId = NotificationId(notification.id)
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
        val notificationsCall: Call<BooleanResponse> =
            retrofit.create(RetrofitAPIs::class.java).deleteNotification(notificationId)
        notificationsCall.enqueue(object : Callback<BooleanResponse> {
            override fun onResponse(call: Call<BooleanResponse>, response: Response<BooleanResponse>) {
                binding.progressBar.visibility = View.GONE
                if (response.isSuccessful){
                    getNotifications()
                }else{
                    val gson = Gson()
                    val type = object : TypeToken<ErrorResponse>() {}.type //ErrorResponse is the data class that matches the error response
                    val errorResponse = gson.fromJson<ErrorResponse>(response.errorBody()!!.charStream(), type) // errorResponse is an instance of ErrorResponse that will contain details about the error
                    Toast.makeText(washeeMainActivity, errorResponse.status.massage.toString(), Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<BooleanResponse>, t: Throwable) {
                binding.progressBar.visibility = View.GONE
                Toast.makeText(washeeMainActivity, resources.getString(R.string.internet_connection), Toast.LENGTH_SHORT).show()
            }

        })
    }

    fun readNotification(notification: Notification){
        binding.progressBar.visibility = View.VISIBLE
        val notificationId = NotificationId(notification.id)
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
        val notificationsCall: Call<BooleanResponse> =
            retrofit.create(RetrofitAPIs::class.java).readNotification(notificationId)
        notificationsCall.enqueue(object : Callback<BooleanResponse> {
            override fun onResponse(call: Call<BooleanResponse>, response: Response<BooleanResponse>) {
                binding.progressBar.visibility = View.GONE
                if (response.isSuccessful){
                    getNotifications()
                }else{
                    val gson = Gson()
                    val type = object : TypeToken<ErrorResponse>() {}.type //ErrorResponse is the data class that matches the error response
                    val errorResponse = gson.fromJson<ErrorResponse>(response.errorBody()!!.charStream(), type) // errorResponse is an instance of ErrorResponse that will contain details about the error
                    Toast.makeText(washeeMainActivity, errorResponse.status.massage.toString(), Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<BooleanResponse>, t: Throwable) {
                binding.progressBar.visibility = View.GONE
                Toast.makeText(washeeMainActivity, resources.getString(R.string.internet_connection), Toast.LENGTH_SHORT).show()
            }

        })
    }

}