package com.washathomes.views.main.washee.more

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.washathomes.apputils.appdefs.AppDefs
import com.washathomes.apputils.appdefs.Urls
import com.washathomes.apputils.modules.BooleanResponse
import com.washathomes.apputils.modules.ErrorResponse
import com.washathomes.apputils.remote.RetrofitAPIs
import com.washathomes.BuildConfig
import com.washathomes.R
import com.washathomes.views.main.washee.WasheeMainActivity
import com.washathomes.views.splash.SplashActivity
import com.washathomes.databinding.FragmentMoreBinding
import dagger.hilt.android.AndroidEntryPoint
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
@AndroidEntryPoint
class MoreFragment : Fragment() {

    lateinit var binding: FragmentMoreBinding
    lateinit var navController: NavController
    lateinit var washeeMainActivity: WasheeMainActivity

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val layout = inflater.inflate(R.layout.fragment_more, container, false)
        binding = FragmentMoreBinding.inflate(layoutInflater)
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
    }

    private fun initViews(view: View){
        navController = Navigation.findNavController(view)
        binding.toolbarLayout.toolbarAppTitle.text = resources.getString(R.string.title_more)
    }

    private fun onClick(){
        binding.accountProfile.setOnClickListener { navController.navigate(MoreFragmentDirections.actionNavigationMoreToProfileFragment()) }
        binding.toolbarLayout.clRight.setOnClickListener { navController.navigate(MoreFragmentDirections.actionNavigationMoreToBasketFragment()) }
        binding.toolbarLayout.clLeft.setOnClickListener { navController.navigate(MoreFragmentDirections.actionNavigationMoreToWasheeNotificationsFragment()) }
        binding.accountSignOut.setOnClickListener { showLogoutMessage() }
        binding.accountDeleteAccount.setOnClickListener { showDeleteAccountMessage() }
        binding.appShare.setOnClickListener { shareApp() }
        binding.appTerms.setOnClickListener { openWebPageInBrowser(resources.getString(R.string.terms_url)) }
        binding.appPolicy.setOnClickListener { openWebPageInBrowser(resources.getString(R.string.privacy_policy_url)) }
        binding.ordersOrderHistory.setOnClickListener { navController.navigate(MoreFragmentDirections.actionNavigationMoreToOrderHistoryFragment()) }
        binding.helpHowItWorks.setOnClickListener { navController.navigate(MoreFragmentDirections.actionNavigationMoreToWebViewFragment(resources.getString(R.string.faq_url))) }
        binding.helpFeedback.setOnClickListener { navController.navigate(MoreFragmentDirections.actionNavigationMoreToWebViewFragment(resources.getString(R.string.feedback_url))) }
        binding.ordersDisputes.setOnClickListener { navController.navigate(MoreFragmentDirections.actionNavigationMoreToWebViewFragment(resources.getString(R.string.dispute_url))) }
        binding.ordersMyRatings.setOnClickListener { navController.navigate(MoreFragmentDirections.actionNavigationMoreToWebViewFragment(resources.getString(R.string.ratings_url))) }
        binding.helpContact.setOnClickListener { navController.navigate(MoreFragmentDirections.actionNavigationMoreToWebViewFragment(resources.getString(R.string.contact_url))) }
    }

    private fun showLogoutMessage() {
        val msgDialog = AlertDialog.Builder(washeeMainActivity)
        msgDialog.setTitle(washeeMainActivity.resources.getString(R.string.log_out))
        msgDialog.setMessage(washeeMainActivity.resources.getString(R.string.log_out_desc))
        msgDialog.setPositiveButton(
            washeeMainActivity.resources.getString(R.string.yes)
        ) { dialogInterface, i ->
            val preferences: SharedPreferences = washeeMainActivity.getSharedPreferences(
                AppDefs.SHARED_PREF_KEY,
                Context.MODE_PRIVATE
            )
            val editor = preferences.edit()
            editor.clear()
            editor.apply()
            val splashIntent = Intent(washeeMainActivity, SplashActivity::class.java)
            startActivity(splashIntent)
            washeeMainActivity.finish()
        }
        msgDialog.setNegativeButton(
            resources.getString(R.string.cancel)
        ) { dialogInterface: DialogInterface, i: Int -> dialogInterface.dismiss() }
        msgDialog.show()
    }

    private fun shareApp() {
        try {
            val shareIntent = Intent(Intent.ACTION_SEND)
            shareIntent.type = "text/plain"
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Wash @ Home")
            var shareMessage = ""
            shareMessage = """${shareMessage}https://play.google.com/store/apps/details?id=${BuildConfig.APPLICATION_ID}""".trimIndent()
            shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage)
            startActivity(Intent.createChooser(shareIntent, "choose one"))
        } catch (e: Exception) {
            Log.d("er", e.message!!)
        }
    }

    private fun openWebPageInBrowser(url: String?) {
        val browserIntent =
            Intent(Intent.ACTION_VIEW, Uri.parse(url))
        startActivity(browserIntent)
    }

    private fun showDeleteAccountMessage() {
        val msgDialog = AlertDialog.Builder(washeeMainActivity)
        msgDialog.setTitle(washeeMainActivity.resources.getString(R.string.delete_account))
        msgDialog.setMessage(washeeMainActivity.resources.getString(R.string.delete_account_desc))
        msgDialog.setPositiveButton(
            washeeMainActivity.resources.getString(R.string.yes)
        ) { dialogInterface, i ->
            deleteAccount()
        }
        msgDialog.setNegativeButton(
            resources.getString(R.string.cancel)
        ) { dialogInterface: DialogInterface, i: Int -> dialogInterface.dismiss() }
        msgDialog.show()
    }

    private fun deleteAccount(){
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
            retrofit.create(RetrofitAPIs::class.java).deleteAccount()
        notificationsCall.enqueue(object : Callback<BooleanResponse> {
            override fun onResponse(call: Call<BooleanResponse>, response: Response<BooleanResponse>) {
                if (response.isSuccessful){
                    val preferences: SharedPreferences = washeeMainActivity.getSharedPreferences(
                        AppDefs.SHARED_PREF_KEY,
                        Context.MODE_PRIVATE
                    )
                    val editor = preferences.edit()
                    editor.clear()
                    editor.apply()
                    val splashIntent = Intent(washeeMainActivity, SplashActivity::class.java)
                    startActivity(splashIntent)
                    washeeMainActivity.finish()
                }else{
                    val gson = Gson()
                    val type = object : TypeToken<ErrorResponse>() {}.type //ErrorResponse is the data class that matches the error response
                    val errorResponse = gson.fromJson<ErrorResponse>(response.errorBody()!!.charStream(), type) // errorResponse is an instance of ErrorResponse that will contain details about the error
                    Toast.makeText(washeeMainActivity, errorResponse.status.massage.toString(), Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<BooleanResponse>, t: Throwable) {
                Toast.makeText(washeeMainActivity, resources.getString(R.string.internet_connection), Toast.LENGTH_SHORT).show()
            }

        })
    }

}