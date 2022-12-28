package com.washathomes.views.main.washer

import android.app.AlertDialog
import android.content.*
import android.content.pm.PackageInfo
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.location.LocationManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.navigation.Navigation
import androidx.navigation.ui.NavigationUI
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.washathomes.R
import com.washathomes.apputils.appdefs.AppDefs
import com.washathomes.apputils.appdefs.Urls
import com.washathomes.apputils.modules.UserData
import com.washathomes.apputils.modules.UserLogin
import com.washathomes.apputils.modules.Version
import com.washathomes.apputils.remote.RetrofitAPIs
import com.washathomes.views.splash.SplashActivity
import dagger.hilt.android.AndroidEntryPoint
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.ByteArrayOutputStream
import java.text.DecimalFormat
@AndroidEntryPoint
class WasherMainActivity : AppCompatActivity() {

    lateinit var bottomNavigationView: BottomNavigationView
    val formatter = DecimalFormat("#0.00")
    var broadcastReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_washer_main)

        val navController = Navigation.findNavController(this, R.id.nav_host_fragment)
        bottomNavigationView = findViewById(R.id.nav_view)
        NavigationUI.setupWithNavController(bottomNavigationView, navController)
        login()
        getVersion()
    }

    fun convertToBase64(bitmap: Bitmap): String? {
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
        val byteArray = byteArrayOutputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.DEFAULT)
    }

    fun hideKeyboard() {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        //Find the currently focused view, so we can grab the correct window token from it.
        var view = currentFocus
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = View(this)
        }
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    override fun onResume() {
        super.onResume()
        registerReceiver(broadcastReceiver, IntentFilter("Push"))
    }

    override fun onPause() {
        super.onPause()
        unregisterReceiver(broadcastReceiver)
    }

    private fun login(){
        val userParams = UserLogin(AppDefs.user.results!!.phone, AppDefs.lang, "", "1")
        val retrofit: Retrofit = Retrofit.Builder().baseUrl(Urls.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create()).build()
        val loginCall: Call<UserData> =
            retrofit.create(RetrofitAPIs::class.java).login(userParams)
        loginCall.enqueue(object : Callback<UserData> {
            override fun onResponse(call: Call<UserData>, response: Response<UserData>) {
                if (response.isSuccessful){
                    AppDefs.user = response.body()!!
                    if (response.body()!!.results!!.status != "1"){
                        logoutPopUp()
                    }
                }
            }

            override fun onFailure(call: Call<UserData>, t: Throwable) {
            }

        })
    }

    private fun logoutPopUp(){
        val alertView: View =
            LayoutInflater.from(this).inflate(R.layout.blocked_popup, null)
        val alertBuilder = AlertDialog.Builder(this).setView(alertView).show()
        alertBuilder.show()
        alertBuilder.setCancelable(false)

        alertBuilder.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val logout: TextView = alertView.findViewById(R.id.logout)

        logout.setOnClickListener {
            alertBuilder.dismiss()
            val preferences: SharedPreferences = this.getSharedPreferences(
                AppDefs.SHARED_PREF_KEY,
                Context.MODE_PRIVATE
            )
            val editor = preferences.edit()
            editor.clear()
            editor.apply()
            val splashIntent = Intent(this, SplashActivity::class.java)
            startActivity(splashIntent)
            finish()

        }
    }

    private fun getVersion(){
        val retrofit: Retrofit = Retrofit.Builder().baseUrl(Urls.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create()).build()
        val updateUserCall: Call<Version> =
            retrofit.create(RetrofitAPIs::class.java).getVersion()
        updateUserCall.enqueue(object : Callback<Version> {
            override fun onResponse(call: Call<Version>, response: Response<Version>) {
                val pInfo: PackageInfo = packageManager.getPackageInfo(packageName, 0)
                val versionCode = pInfo.versionCode.toString()
                if (response.isSuccessful){
                    if (response.body()!!.results.version_android != versionCode){
                        showUpdateDialog()
                    }
                }
            }

            override fun onFailure(call: Call<Version>, t: Throwable) {
            }

        })
    }

    private fun showUpdateDialog() {
        val alertDialog = androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle(getString(R.string.update_version))
            .setMessage(getString(R.string.update_version_description))
            .setCancelable(false)
            .setPositiveButton(getString(R.string.update)) { dialog: DialogInterface?, which: Int ->
                val uri = Uri.parse("market://details?id=$packageName")
                val goToMarket = Intent(Intent.ACTION_VIEW, uri)
                goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY or
                        Intent.FLAG_ACTIVITY_NEW_DOCUMENT or
                        Intent.FLAG_ACTIVITY_MULTIPLE_TASK)
                try {
                    startActivity(goToMarket)
                } catch (e: ActivityNotFoundException) {
                    startActivity(Intent(Intent.ACTION_VIEW,
                        Uri.parse("https://play.google.com/store/apps/details?id=$packageName")))
                }
            }
            .create()
        alertDialog.setOnShowListener { dialog: DialogInterface? ->
            val color = ContextCompat.getColor(this, R.color.colorAccent)
            alertDialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE).setTextColor(color)
        }
        alertDialog.show()
    }

    fun locationEnabled() {
        val lm = getSystemService(LOCATION_SERVICE) as LocationManager
        var gps_enabled = false
        var network_enabled = false
        try {
            gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        try {
            network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        if (!gps_enabled && !network_enabled) {
            AlertDialog.Builder(this)
                .setMessage(R.string.gps_network_not_enabled)
                .setPositiveButton(
                    R.string.open_location_settings
                ) { paramDialogInterface, paramInt -> startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                    finish()}
                .show()
                .setCancelable(false)
        }
    }
}