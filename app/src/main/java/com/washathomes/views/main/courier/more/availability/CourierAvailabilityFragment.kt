package com.washathomes.views.main.courier.more.availability

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.washathomes.apputils.appdefs.AppDefs
import com.washathomes.apputils.appdefs.Urls
import com.washathomes.apputils.modules.ErrorResponse
import com.washathomes.apputils.modules.UserData
import com.washathomes.apputils.remote.RetrofitAPIs
import com.washathomes.R
import com.washathomes.views.main.courier.CourierMainActivity
import com.washathomes.databinding.FragmentCourierAvailabilityBinding
import dagger.hilt.android.AndroidEntryPoint
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
@AndroidEntryPoint
class CourierAvailabilityFragment : Fragment() {

    lateinit var binding: FragmentCourierAvailabilityBinding
    lateinit var navController: NavController
    lateinit var courierMainActivity: CourierMainActivity

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val layout = inflater.inflate(R.layout.fragment_courier_availability, container, false)
        binding = FragmentCourierAvailabilityBinding.inflate(layoutInflater)
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
    }

    private fun initViews(view: View){
        navController = Navigation.findNavController(view)
        binding.washerAvailabilitySwitchButton.isChecked = AppDefs.user.results!!.dreiver_available == "1"
    }

    private fun onClick(){
        binding.toolbarBackIcon.setOnClickListener { navController.popBackStack() }
        binding.washerAvailabilityNext.setOnClickListener { changeAvailability() }
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

        navController.popBackStack()
    }

}