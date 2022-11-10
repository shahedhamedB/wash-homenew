package com.washathomes.views.main.courier.wallet

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
import com.washathomes.apputils.modules.ErrorResponse
import com.washathomes.apputils.modules.Wallet
import com.washathomes.apputils.modules.WalletObj
import com.washathomes.apputils.remote.RetrofitAPIs
import com.washathomes.R
import com.washathomes.views.main.courier.CourierMainActivity
import com.washathomes.views.main.washer.wallet.WalletAdapter
import com.washathomes.databinding.FragmentCourierWalletBinding
import dagger.hilt.android.AndroidEntryPoint
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
@AndroidEntryPoint
class CourierWalletFragment : Fragment() {

    lateinit var binding: FragmentCourierWalletBinding
    lateinit var courierMainActivity: CourierMainActivity
    lateinit var navController: NavController
    lateinit var wallet: Wallet

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val layout = inflater.inflate(R.layout.fragment_courier_wallet, container, false)
        binding = FragmentCourierWalletBinding.inflate(layoutInflater)
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
        getWallet()
    }

    private fun initViews(view: View){
        navController = Navigation.findNavController(view)
        binding.toolbarLayout.toolbarAppTitle.text = resources.getString(R.string.wallet)
    }

    private fun getWallet(){
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
        val availabilityCall: Call<WalletObj> =
            retrofit.create(RetrofitAPIs::class.java).driverWallet()
        availabilityCall.enqueue(object : Callback<WalletObj> {
            override fun onResponse(call: Call<WalletObj>, response: Response<WalletObj>) {
                binding.progressBar.visibility = View.GONE
                if (response.isSuccessful){
                    wallet = response.body()!!.results
                    setData()
                }else{
                    val gson = Gson()
                    val type = object : TypeToken<ErrorResponse>() {}.type //ErrorResponse is the data class that matches the error response
                    val errorResponse = gson.fromJson<ErrorResponse>(response.errorBody()!!.charStream(), type) // errorResponse is an instance of ErrorResponse that will contain details about the error
                    Toast.makeText(courierMainActivity, errorResponse.status.massage.toString(), Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<WalletObj>, t: Throwable) {
                binding.progressBar.visibility = View.GONE
                Toast.makeText(courierMainActivity, resources.getString(R.string.internet_connection), Toast.LENGTH_SHORT).show()
            }

        })
    }

    private fun setData(){
        binding.totalAll.text = wallet.total_all
        binding.totalPaid.text = wallet.total_paid
        binding.paymentNext.text = wallet.paymnt_next_total
        binding.nextPaymentDate.text = resources.getString(R.string.next_redeem_date)+" "+wallet.next_payment

        val adapter = WalletAdapter(wallet.trans_list)
        binding.incomeRecyclerView.adapter = adapter
        binding.incomeRecyclerView.layoutManager = LinearLayoutManager(courierMainActivity)

        if (wallet.trans_list.size == 0){
            binding.noItems.visibility = View.VISIBLE
        }else{
            binding.noItems.visibility = View.GONE
        }
    }

}