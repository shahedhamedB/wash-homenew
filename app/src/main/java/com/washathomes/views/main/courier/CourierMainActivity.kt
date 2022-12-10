package com.washathomes.views.main.courier

import android.app.AlertDialog
import android.content.*
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import androidx.navigation.Navigation
import androidx.navigation.ui.NavigationUI
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.washathomes.R
import com.washathomes.apputils.appdefs.AppDefs
import com.washathomes.apputils.appdefs.Urls
import com.washathomes.apputils.modules.UserData
import com.washathomes.apputils.modules.UserLogin
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
class CourierMainActivity : AppCompatActivity() {

    lateinit var bottomNavigationView: BottomNavigationView
    val formatter = DecimalFormat("#0.00")
    var broadcastReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_courier_main)

        val navController = Navigation.findNavController(this, R.id.nav_host_fragment)
        bottomNavigationView = findViewById(R.id.nav_view)
        NavigationUI.setupWithNavController(bottomNavigationView, navController)

        login()
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
        val userParams = UserLogin(AppDefs.user.results!!.phone, AppDefs.lang, AppDefs.user.token, "1")
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
                TODO("Not yet implemented")
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
}