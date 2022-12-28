package com.washathomes.views.main.washer.more.settings.pickupreturn

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.bumptech.glide.Glide
import com.google.gson.Gson
import com.washathomes.R
import com.washathomes.apputils.appdefs.AppDefs
import com.washathomes.apputils.appdefs.Urls
import com.washathomes.apputils.modules.UpdateDrivingData
import com.washathomes.apputils.modules.UpdatePickupReturn
import com.washathomes.apputils.modules.UserData
import com.washathomes.apputils.remote.RetrofitAPIs
import com.washathomes.databinding.FragmentPickupReturnBinding
import com.washathomes.views.main.washer.WasherMainActivity
import com.washathomes.views.registration.washerregistration.WasherRegistrationActivity
import dagger.hilt.android.AndroidEntryPoint
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import kotlin.math.roundToInt

@AndroidEntryPoint
class PickupReturnFragment : Fragment() {

    lateinit var binding: FragmentPickupReturnBinding
    lateinit var washerMainActivity: WasherMainActivity
    lateinit var navController: NavController
    var drivingAvailable = ""
    var miles = ""
    var express = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentPickupReturnBinding.inflate(layoutInflater)
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
    }

    private fun initViews(view: View){
        navController = Navigation.findNavController(view)
    }

    private fun onClick(){
        binding.toolbarBackIcon.setOnClickListener { navController.popBackStack() }
        binding.washerPickupAndReturnItemSwitchButton.setOnCheckedChangeListener { buttonView, isChecked ->
            drivingAvailable = if (isChecked){
                "1"
            }else{
                "0"
            }
        }
        binding.washerPickupAndReturnExpressSwitchButton.setOnCheckedChangeListener { buttonView, isChecked ->
            express = if (isChecked){
                "1"
            }else{
                "0"
            }
        }
        binding.washerPickupAndReturnSeekbar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                val p = progress/20.0
                miles = ""+p
                binding.washerPickupAndReturnDistanceMilNumber.text = ""+p
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
                // you can probably leave this empty
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                // you can probably leave this empty
            }
        })
        binding.washerPickupReturnNext.setOnClickListener { updateUser() }
    }

    private fun setData(){
        binding.washerPickupAndReturnItemSwitchButton.isChecked = AppDefs.user.results!!.dreiver_available == "1"
        binding.washerPickupAndReturnExpressSwitchButton.isChecked = AppDefs.user.results!!.express == "1"
        var milesSelected = 5
        if (!AppDefs.user.results!!.dreiver_miles.isNullOrEmpty()) {
            val sub =  AppDefs.user.results!!.dreiver_miles!!
            milesSelected = sub.toFloat().roundToInt()
        }
        binding.washerPickupAndReturnSeekbar.progress = milesSelected*20
        binding.washerPickupAndReturnDistanceMilNumber.text = AppDefs.user.results!!.dreiver_miles!!
        drivingAvailable = AppDefs.user.results!!.dreiver_available!!
        express = AppDefs.user.results!!.express!!
        miles = milesSelected.toString()
    }

    private fun updateUser(){
        val userParams = UpdatePickupReturn(drivingAvailable, miles, express)
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
        val updateDrivingDataCall: Call<UserData> =
            retrofit.create(RetrofitAPIs::class.java).updatePickupReturn(userParams)
        updateDrivingDataCall.enqueue(object : Callback<UserData> {
            override fun onResponse(call: Call<UserData>, response: Response<UserData>) {
                AppDefs.user = response.body()!!
                binding.progressBar.visibility = View.GONE
                saveUserToSharedPreferences()
            }

            override fun onFailure(call: Call<UserData>, t: Throwable) {
            }

        })
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
        navController.popBackStack()
    }

}