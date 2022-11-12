package com.washathomes.views.registration.washerregistration

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.washathomes.apputils.appdefs.AppDefs
import com.washathomes.apputils.appdefs.Urls
import com.washathomes.apputils.helpers.Helpers
import com.washathomes.apputils.remote.RetrofitAPIs
import com.washathomes.apputils.modules.AccountTypeIntro
import com.washathomes.apputils.modules.AccountTypeIntros
import com.washathomes.R
import com.washathomes.databinding.FragmentWasherIntroBinding
import dagger.hilt.android.AndroidEntryPoint
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
@AndroidEntryPoint
class WasherIntroFragment : Fragment() {

    lateinit var binding: FragmentWasherIntroBinding
    lateinit var washerRegistrationActivity: WasherRegistrationActivity
    lateinit var navController: NavController
    var items: ArrayList<AccountTypeIntro> = ArrayList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val layout = inflater.inflate(R.layout.fragment_washer_intro, container, false)
        binding = FragmentWasherIntroBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is WasherRegistrationActivity) {
            washerRegistrationActivity = context
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews(view)
        onClick()
        getWasherIntro()
    }

    private fun initViews(view: View){
        navController = Navigation.findNavController(view)
    }

    private fun onClick(){
        binding.nextBtn.setOnClickListener { navController.navigate(WasherIntroFragmentDirections.actionWasherIntroFragmentToWasherSignUpFragment()) }
    }

    private fun getWasherIntro(){
        binding.progressBar.visibility = View.VISIBLE
        binding.introLayout.visibility = View.GONE
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
        val washerIntroCall: Call<AccountTypeIntros> =
            retrofit.create(RetrofitAPIs::class.java).getWasherIntro()
        washerIntroCall.enqueue(object : Callback<AccountTypeIntros> {
            override fun onResponse(
                call: Call<AccountTypeIntros>,
                response: Response<AccountTypeIntros>
            ) {
                if (response.isSuccessful){
                    items = response.body()!!.results
                    initSlider()
                    binding.progressBar.visibility = View.GONE
                    binding.introLayout.visibility = View.VISIBLE
                }
            }

            override fun onFailure(call: Call<AccountTypeIntros>, t: Throwable) {
                TODO("Not yet implemented")
            }

        })
    }

    private fun initSlider() {
        binding.viewPager.visibility = View.VISIBLE
        binding.tabDots.setupWithViewPager(binding.viewPager, true)
        val mAdapter = IntroAdapter(items, washerRegistrationActivity)
        mAdapter.notifyDataSetChanged()
        binding.viewPager.offscreenPageLimit = items.size
        binding.viewPager.adapter = mAdapter
        Helpers.setSliderTimer(3000, binding.viewPager, mAdapter)
    }

}