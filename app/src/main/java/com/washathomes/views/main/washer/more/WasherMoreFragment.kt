package com.washathomes.views.main.washer.more

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.util.Log
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
import com.washathomes.apputils.modules.BooleanResponse
import com.washathomes.apputils.modules.ErrorResponse
import com.washathomes.apputils.remote.RetrofitAPIs
import com.washathomes.BuildConfig
import com.washathomes.R
import com.washathomes.views.main.washer.WasherMainActivity
import com.washathomes.views.splash.SplashActivity
import com.washathomes.databinding.FragmentWasherMoreBinding
import dagger.hilt.android.AndroidEntryPoint
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
@AndroidEntryPoint
class WasherMoreFragment : Fragment() {

    lateinit var binding: FragmentWasherMoreBinding
    lateinit var navController: NavController
    lateinit var washerMainActivity: WasherMainActivity
    
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val layout = inflater.inflate(R.layout.fragment_washer_more, container, false)
        binding = FragmentWasherMoreBinding.inflate(layoutInflater)
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
        onClick()
    }

    private fun initViews(view: View){
        navController = Navigation.findNavController(view)
        binding.toolbarLayout.toolbarAppTitle.text = resources.getString(R.string.title_more)
    }

    private fun onClick(){
        binding.accountAvailability.setOnClickListener { navController.navigate(WasherMoreFragmentDirections.actionWasherMoreFragmentToWasherAvailabilityFragment()) }
        binding.accountProfile.setOnClickListener { navController.navigate(WasherMoreFragmentDirections.actionWasherMoreFragmentToWasherProfileFragment()) }
        binding.accountSignOut.setOnClickListener { showLogoutMessage() }
        binding.accountDeleteAccount.setOnClickListener { showDeleteAccountMessage() }
        binding.menuLineAccountSettings.setOnClickListener { navController.navigate(WasherMoreFragmentDirections.actionWasherMoreFragmentToSettingsFragment()) }
        binding.appShare.setOnClickListener { shareApp() }
        binding.appTerms.setOnClickListener { openWebPageInBrowser(resources.getString(R.string.terms_url)) }
        binding.appPolicy.setOnClickListener { openWebPageInBrowser(resources.getString(R.string.privacy_policy_url)) }
        binding.ordersOrderHistory.setOnClickListener { navController.navigate(WasherMoreFragmentDirections.actionWasherMoreFragmentToWasherOrderHistoryFragment()) }
        binding.helpHowItWorks.setOnClickListener { navController.navigate(WasherMoreFragmentDirections.actionWasherMoreFragmentToWasherWebViewFragment(resources.getString(R.string.faq_url))) }
        binding.helpFeedback.setOnClickListener { navController.navigate(WasherMoreFragmentDirections.actionWasherMoreFragmentToWasherWebViewFragment(resources.getString(R.string.feedback_url))) }
        binding.ordersDisputes.setOnClickListener { navController.navigate(WasherMoreFragmentDirections.actionWasherMoreFragmentToWasherWebViewFragment(resources.getString(R.string.dispute_url))) }
        binding.ordersTransaction.setOnClickListener { navController.navigate(WasherMoreFragmentDirections.actionWasherMoreFragmentToWasherWebViewFragment(resources.getString(R.string.transaction_url))) }
        binding.accountPayout.setOnClickListener { navController.navigate(WasherMoreFragmentDirections.actionWasherMoreFragmentToWasherWebViewFragment(resources.getString(R.string.payout_url))) }
        binding.ordersMyRatings.setOnClickListener { navController.navigate(WasherMoreFragmentDirections.actionWasherMoreFragmentToWasherWebViewFragment(resources.getString(R.string.ratings_url))) }
        binding.helpContact.setOnClickListener { navController.navigate(WasherMoreFragmentDirections.actionWasherMoreFragmentToWasherWebViewFragment(resources.getString(R.string.contact_url))) }
    }

    private fun showLogoutMessage() {
        val msgDialog = AlertDialog.Builder(washerMainActivity)
        msgDialog.setTitle(washerMainActivity.resources.getString(R.string.log_out))
        msgDialog.setMessage(washerMainActivity.resources.getString(R.string.log_out_desc))
        msgDialog.setPositiveButton(
            washerMainActivity.resources.getString(R.string.yes)
        ) { dialogInterface, i ->
            val preferences: SharedPreferences = washerMainActivity.getSharedPreferences(
                AppDefs.SHARED_PREF_KEY,
                Context.MODE_PRIVATE
            )
            val editor = preferences.edit()
            editor.clear()
            editor.apply()
            val splashIntent = Intent(washerMainActivity, SplashActivity::class.java)
            startActivity(splashIntent)
            washerMainActivity.finish()
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
        val msgDialog = AlertDialog.Builder(washerMainActivity)
        msgDialog.setTitle(washerMainActivity.resources.getString(R.string.delete_account))
        msgDialog.setMessage(washerMainActivity.resources.getString(R.string.delete_account_desc))
        msgDialog.setPositiveButton(
            washerMainActivity.resources.getString(R.string.yes)
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
                    val preferences: SharedPreferences = washerMainActivity.getSharedPreferences(
                        AppDefs.SHARED_PREF_KEY,
                        Context.MODE_PRIVATE
                    )
                    val editor = preferences.edit()
                    editor.clear()
                    editor.apply()
                    val splashIntent = Intent(washerMainActivity, SplashActivity::class.java)
                    startActivity(splashIntent)
                    washerMainActivity.finish()
                }else{
                    val gson = Gson()
                    val type = object : TypeToken<ErrorResponse>() {}.type //ErrorResponse is the data class that matches the error response
                    val errorResponse = gson.fromJson<ErrorResponse>(response.errorBody()!!.charStream(), type) // errorResponse is an instance of ErrorResponse that will contain details about the error
                    Toast.makeText(washerMainActivity, errorResponse.status.massage.toString(), Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<BooleanResponse>, t: Throwable) {
                Toast.makeText(washerMainActivity, resources.getString(R.string.internet_connection), Toast.LENGTH_SHORT).show()
            }

        })
    }
}