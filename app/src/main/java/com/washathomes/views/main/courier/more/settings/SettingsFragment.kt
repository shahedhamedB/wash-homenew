package com.washathomes.views.main.courier.more.settings

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.location.Address
import android.location.Geocoder
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.google.gson.Gson
import com.washathomes.R
import com.washathomes.apputils.appdefs.AppDefs
import com.washathomes.apputils.appdefs.Urls
import com.washathomes.apputils.modules.UpdateLocation
import com.washathomes.apputils.modules.UpdateUser
import com.washathomes.apputils.modules.UserData
import com.washathomes.apputils.remote.RetrofitAPIs
import com.washathomes.views.main.courier.CourierMainActivity
import com.washathomes.databinding.FragmentSettings2Binding
import com.washathomes.views.maps.MapsActivity
import com.washathomes.views.splash.SplashActivity
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
class SettingsFragment : Fragment() {

    lateinit var binding: FragmentSettings2Binding
    lateinit var courierMainActivity: CourierMainActivity
    lateinit var navController: NavController
    private val LOCATION_CODE = 100
    var latitude = ""
    var longitude = ""
    var zipCode = ""
    var address = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val layout = inflater.inflate(R.layout.fragment_settings2, container, false)
        binding = FragmentSettings2Binding.inflate(layoutInflater)
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
    }

    private fun onClick(){
        binding.toolbarBackIcon.setOnClickListener { navController.popBackStack() }
        binding.menuLineIdentificationUpload.setOnClickListener { navController.navigate(
            SettingsFragmentDirections.actionSettingsFragment2ToCourierIdentificationFragment()) }
        binding.menuLineDeliveryService.setOnClickListener { navController.navigate(
            SettingsFragmentDirections.actionSettingsFragment2ToDeliveryServiceFragment()) }
        binding.menuLineLocationUpdate.setOnClickListener {
            startMapsActivity()
        }
    }

    private fun startMapsActivity() {
        AppDefs.updateLocation = true
        val intent = Intent(context, MapsActivity::class.java)
        startActivityForResult(
            intent,
            LOCATION_CODE
        )
    }

    private fun getAddress(latitude: Double, longitude: Double){
        val geocoder: Geocoder
        val addresses: List<Address>
        geocoder = Geocoder(courierMainActivity, Locale.getDefault())

        addresses = geocoder.getFromLocation(
            latitude,
            longitude,
            1
        ) as List<Address>// Here 1 represent max location result to returned, by documents it recommended 1 to 5

        address = addresses[0].getAddressLine(0)

        if (addresses[0].postalCode != null){
            zipCode = addresses[0].postalCode
        }

        updateLocation()
    }

    private fun updateLocation(){
        val userParams = UpdateLocation(latitude, longitude, address, zipCode)
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
        val updateUserCall: Call<UserData> =
            retrofit.create(RetrofitAPIs::class.java).updateLocation(userParams)
        updateUserCall.enqueue(object : Callback<UserData> {
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
            courierMainActivity.getSharedPreferences(AppDefs.SHARED_PREF_KEY, Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString(AppDefs.ID_KEY, AppDefs.user.results!!.id)

        val gson = Gson()
        val json = gson.toJson(AppDefs.user)
        editor.putString(AppDefs.USER_KEY, json)
        editor.putString(AppDefs.TYPE_KEY, "3")
        editor.apply()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == LOCATION_CODE && resultCode == Activity.RESULT_OK) {
            latitude = data!!.getStringExtra("latitude")!!
            longitude = data.getStringExtra("longitude")!!
            getAddress(latitude.toDouble(), longitude.toDouble())
        }
    }
}