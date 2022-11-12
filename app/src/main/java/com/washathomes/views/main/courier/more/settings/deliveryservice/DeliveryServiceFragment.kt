package com.washathomes.views.main.courier.more.settings.deliveryservice

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
import com.washathomes.apputils.appdefs.AppDefs
import com.washathomes.apputils.appdefs.Urls
import com.washathomes.apputils.modules.UpdateDrivingData
import com.washathomes.apputils.modules.UserData
import com.washathomes.apputils.remote.RetrofitAPIs
import com.washathomes.R
import com.washathomes.views.main.courier.CourierMainActivity
import com.washathomes.databinding.FragmentDeliveryServiceBinding
import dagger.hilt.android.AndroidEntryPoint
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
@AndroidEntryPoint
class DeliveryServiceFragment : Fragment() {

    lateinit var binding: FragmentDeliveryServiceBinding
    lateinit var courierMainActivity: CourierMainActivity
    lateinit var navController: NavController
    var vehicleType = "1"
    var miles = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val layout = inflater.inflate(R.layout.fragment_delivery_service, container, false)
        binding = FragmentDeliveryServiceBinding.inflate(layoutInflater)
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
    }

    private fun onClick(){
        binding.toolbarBackIcon.setOnClickListener { navController.popBackStack() }
        binding.car.setOnClickListener {
            binding.carLayout.setBackgroundColor(resources.getColor(R.color.blue))
            Glide.with(courierMainActivity).load(R.drawable.white_car).into(binding.carIcon)
            binding.bikeLayout.setBackgroundColor(resources.getColor(R.color.light_grey))
            Glide.with(courierMainActivity).load(R.drawable.grey_bike).into(binding.bikeIcon)
            binding.bicycleLayout.setBackgroundColor(resources.getColor(R.color.light_grey))
            Glide.with(courierMainActivity).load(R.drawable.grey_bicycle).into(binding.bicycleIcon)
            vehicleType = "1"
        }
        binding.bike.setOnClickListener {
            binding.bikeLayout.setBackgroundColor(resources.getColor(R.color.blue))
            Glide.with(courierMainActivity).load(R.drawable.white_bike).into(binding.bikeIcon)
            binding.carLayout.setBackgroundColor(resources.getColor(R.color.light_grey))
            Glide.with(courierMainActivity).load(R.drawable.grey_car).into(binding.carIcon)
            binding.bicycleLayout.setBackgroundColor(resources.getColor(R.color.light_grey))
            Glide.with(courierMainActivity).load(R.drawable.grey_bicycle).into(binding.bicycleIcon)
            vehicleType = "2"
        }
        binding.bycicle.setOnClickListener {
            binding.bicycleLayout.setBackgroundColor(resources.getColor(R.color.blue))
            Glide.with(courierMainActivity).load(R.drawable.white_bycicle).into(binding.bicycleIcon)
            binding.bikeLayout.setBackgroundColor(resources.getColor(R.color.light_grey))
            Glide.with(courierMainActivity).load(R.drawable.grey_bike).into(binding.bikeIcon)
            binding.carLayout.setBackgroundColor(resources.getColor(R.color.light_grey))
            Glide.with(courierMainActivity).load(R.drawable.grey_car).into(binding.carIcon)
            vehicleType = "3"
        }
        binding.milesSB.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                val p = progress/20.0
                miles = ""+p
                binding.miles.text = ""+p+" "+resources.getString(R.string.miles)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
                // you can probably leave this empty
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                // you can probably leave this empty
            }
        })
        binding.nextBtn.setOnClickListener { updateUser() }
    }

    private fun setData(){
        when (AppDefs.user.results!!.dreiver_type) {
            "1" -> {
                binding.carLayout.setBackgroundColor(resources.getColor(R.color.blue))
                Glide.with(courierMainActivity).load(R.drawable.white_car).into(binding.carIcon)
                binding.bikeLayout.setBackgroundColor(resources.getColor(R.color.light_grey))
                Glide.with(courierMainActivity).load(R.drawable.grey_bike).into(binding.bikeIcon)
                binding.bicycleLayout.setBackgroundColor(resources.getColor(R.color.light_grey))
                Glide.with(courierMainActivity).load(R.drawable.grey_bicycle).into(binding.bicycleIcon)
                vehicleType = "1"
            }
            "2" -> {
                binding.bikeLayout.setBackgroundColor(resources.getColor(R.color.blue))
                Glide.with(courierMainActivity).load(R.drawable.white_bike).into(binding.bikeIcon)
                binding.carLayout.setBackgroundColor(resources.getColor(R.color.light_grey))
                Glide.with(courierMainActivity).load(R.drawable.grey_car).into(binding.carIcon)
                binding.bicycleLayout.setBackgroundColor(resources.getColor(R.color.light_grey))
                Glide.with(courierMainActivity).load(R.drawable.grey_bicycle).into(binding.bicycleIcon)
                vehicleType = "2"
            }
            "3" -> {
                binding.bicycleLayout.setBackgroundColor(resources.getColor(R.color.blue))
                Glide.with(courierMainActivity).load(R.drawable.white_bycicle).into(binding.bicycleIcon)
                binding.bikeLayout.setBackgroundColor(resources.getColor(R.color.light_grey))
                Glide.with(courierMainActivity).load(R.drawable.grey_bike).into(binding.bikeIcon)
                binding.carLayout.setBackgroundColor(resources.getColor(R.color.light_grey))
                Glide.with(courierMainActivity).load(R.drawable.grey_car).into(binding.carIcon)
                vehicleType = "3"
            }
        }
        var milesSelected = 5
        if (!AppDefs.user.results!!.dreiver_miles.isNullOrEmpty()) {
            val sub =  AppDefs.user.results!!.dreiver_miles!!.substring(0,1)
            milesSelected = sub.toInt()
        }
        binding.milesSB.progress = milesSelected*20
    }

    private fun updateUser(){
        val userParams = UpdateDrivingData(AppDefs.user.results!!.dreiver_available, vehicleType, miles, "", "", "")
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
            retrofit.create(RetrofitAPIs::class.java).updateDrivingData(userParams)
        updateDrivingDataCall.enqueue(object : Callback<UserData> {
            override fun onResponse(call: Call<UserData>, response: Response<UserData>) {
                AppDefs.user = response.body()!!
                binding.progressBar.visibility = View.GONE
                saveUserToSharedPreferences()
            }

            override fun onFailure(call: Call<UserData>, t: Throwable) {
                TODO("Not yet implemented")
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