package com.washathomes.views.introduction

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.bumptech.glide.Glide
import com.google.gson.Gson
import com.washathomes.apputils.appdefs.AppDefs
import com.washathomes.apputils.appdefs.Urls
import com.washathomes.apputils.helpers.FirebaseHelper
import com.washathomes.apputils.remote.RetrofitAPIs
import com.washathomes.apputils.modules.AccountTypes
import com.washathomes.apputils.modules.UserData
import com.washathomes.apputils.modules.UserTypeObj
import com.washathomes.R
import com.washathomes.views.main.courier.CourierMainActivity
import com.washathomes.views.main.washee.WasheeMainActivity
import com.washathomes.views.main.washer.WasherMainActivity
import com.washathomes.views.registration.courierregistration.CourierRegistrationActivity
import com.washathomes.views.registration.washeeregistration.WasheeRegistrationActivity
import com.washathomes.views.registration.washerregistration.WasherRegistrationActivity
import com.washathomes.databinding.FragmentAccountTypesBinding
import dagger.hilt.android.AndroidEntryPoint
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

@AndroidEntryPoint
class AccountTypesFragment : Fragment() {

    lateinit var binding: FragmentAccountTypesBinding
    lateinit var introductionActivity: IntroductionActivity
    lateinit var navController: NavController
    var accountType = "1"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val layout = inflater.inflate(R.layout.fragment_account_types, container, false)
        binding = FragmentAccountTypesBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is IntroductionActivity) {
            introductionActivity = context
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews(view)
        onClick()
        getUserTypes()
    }

    private fun initViews(view: View){
        navController = Navigation.findNavController(view)
    }

    private fun onClick(){
        binding.washeeCV.setOnClickListener {
            accountType = "1"
            binding.washeeLayout.background = requireContext().resources.getDrawable(R.drawable.account_selected)
            binding.washerLayout.background = requireContext().resources.getDrawable(R.drawable.white_radius)
            binding.courierLayout.background = requireContext().resources.getDrawable(R.drawable.white_radius)
        }
        binding.washerCV.setOnClickListener {
            accountType = "2"
            binding.washeeLayout.background = requireContext().resources.getDrawable(R.drawable.white_radius)
            binding.washerLayout.background = requireContext().resources.getDrawable(R.drawable.account_selected)
            binding.courierLayout.background = requireContext().resources.getDrawable(R.drawable.white_radius)
        }
        binding.courierCV.setOnClickListener {
            accountType = "3"
            binding.washeeLayout.background = requireContext().resources.getDrawable(R.drawable.white_radius)
            binding.washerLayout.background = requireContext().resources.getDrawable(R.drawable.white_radius)
            binding.courierLayout.background = requireContext().resources.getDrawable(R.drawable.account_selected)
        }
        binding.nextBtn.setOnClickListener {
            checkAccount()
//            if (accountType == "1"){
//                val registrationIntent = Intent(introductionActivity, WasheeRegistrationActivity::class.java)
//                startActivity(registrationIntent)
//                introductionActivity.finish()
//            }else if (accountType == "2"){
//                val registrationIntent = Intent(introductionActivity, WasherRegistrationActivity::class.java)
//                startActivity(registrationIntent)
//                introductionActivity.finish()
//            }
        }
    }

    private fun getUserTypes(){
        binding.progressBar.visibility = View.VISIBLE
        binding.accountTypesLayout.visibility = View.GONE
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
        val accountTypesCall: Call<AccountTypes> =
            retrofit.create(RetrofitAPIs::class.java).getAccountTypes()
        accountTypesCall.enqueue(object : Callback<AccountTypes>{
            override fun onResponse(call: Call<AccountTypes>, response: Response<AccountTypes>) {
                if (response.isSuccessful){
                    if (response.body()?.results!!.size > 0){
                        Glide.with(introductionActivity).load(response.body()?.results?.get(0)?.icons).into(binding.washeeIcon)
                        binding.washeeTitle.text = response.body()?.results?.get(0)?.title
                        binding.washeeDescription.text = response.body()?.results?.get(0)?.description

                        Glide.with(introductionActivity).load(response.body()?.results?.get(1)?.icons).into(binding.washerIcon)
                        binding.washerTitle.text = response.body()?.results?.get(1)?.title
                        binding.washerDescription.text = response.body()?.results?.get(1)?.description

                        Glide.with(introductionActivity).load(response.body()?.results?.get(2)?.icons).into(binding.courierIcon)
                        binding.courierTitle.text = response.body()?.results?.get(2)?.title
                        binding.courierDescription.text = response.body()?.results?.get(2)?.description

                        binding.progressBar.visibility = View.GONE
                        binding.accountTypesLayout.visibility = View.VISIBLE
                    }
                }
            }

            override fun onFailure(call: Call<AccountTypes>, t: Throwable) {
                TODO("Not yet implemented")
            }

        })
    }

    private fun checkAccount(){
        if (accountType == "1" && AppDefs.user.results?.washee_status == "1"){
            updateSignIn()
        }else if (accountType == "1" && AppDefs.user.results?.washee_status == "0"){
            val registrationIntent = Intent(introductionActivity, WasheeRegistrationActivity::class.java)
            startActivity(registrationIntent)
            introductionActivity.finish()
        }else if (accountType == "2" && AppDefs.user.results?.washer_status == "1"){
            updateSignIn()
        }else if (accountType == "2" && AppDefs.user.results?.washer_status == "0"){
            val registrationIntent = Intent(introductionActivity, WasherRegistrationActivity::class.java)
            startActivity(registrationIntent)
            introductionActivity.finish()
        }else if (accountType == "3" && AppDefs.user.results?.courier_status == "1"){
            updateSignIn()
        }else if (accountType == "3" && AppDefs.user.results?.courier_status == "0"){
            val registrationIntent = Intent(introductionActivity, CourierRegistrationActivity::class.java)
            startActivity(registrationIntent)
            introductionActivity.finish()
        }
    }

    private fun updateSignIn(){
        binding.progressBar.visibility = View.VISIBLE
        val userTypeObj = UserTypeObj(accountType)
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
        val updateSignInCall: Call<UserData> =
            retrofit.create(RetrofitAPIs::class.java).updateSignIn(userTypeObj)
        updateSignInCall.enqueue(object : Callback<UserData> {
            override fun onResponse(call: Call<UserData>, response: Response<UserData>) {
                binding.progressBar.visibility = View.GONE
                saveUserToSharedPreferences()
            }

            override fun onFailure(call: Call<UserData>, t: Throwable) {
            }

        })
    }

    private fun saveUserToSharedPreferences() {
        FirebaseHelper.sendUserToDatabase(AppDefs.user.results!!.language_code!!)
        val sharedPreferences =
            introductionActivity.getSharedPreferences(AppDefs.SHARED_PREF_KEY, Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString(AppDefs.ID_KEY, AppDefs.user.results!!.id)

        val gson = Gson()
        val json = gson.toJson(AppDefs.user)
        editor.putString(AppDefs.USER_KEY, json)
        editor.putString(AppDefs.TYPE_KEY, accountType)
        editor.apply()
        when (accountType) {
            "1" -> {
                val mainIntent = Intent(introductionActivity, WasheeMainActivity::class.java)
                startActivity(mainIntent)
            }
            "2" -> {
                val mainIntent = Intent(introductionActivity, WasherMainActivity::class.java)
                startActivity(mainIntent)
            }
            "3" -> {
                val mainIntent = Intent(introductionActivity, CourierMainActivity::class.java)
                startActivity(mainIntent)
            }
        }
        introductionActivity.finish()
    }

}