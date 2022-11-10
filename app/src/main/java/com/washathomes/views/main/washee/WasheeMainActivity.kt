package com.washathomes.views.main.washee

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Base64
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.navigation.Navigation
import androidx.navigation.ui.NavigationUI
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.washathomes.R
import com.washathomes.databinding.ActivityWasheeMainBinding
import dagger.hilt.android.AndroidEntryPoint
import java.io.ByteArrayOutputStream
import java.text.DecimalFormat
@AndroidEntryPoint
class WasheeMainActivity : AppCompatActivity() {

    lateinit var binding: ActivityWasheeMainBinding
    lateinit var bottomNavigationView: BottomNavigationView
    val formatter = DecimalFormat("#0.00")
    var broadcastReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_washee_main)

        val layout = R.layout.activity_washee_main

        val navController = Navigation.findNavController(this, R.id.nav_host_fragment)
        bottomNavigationView = findViewById(R.id.nav_view)
        NavigationUI.setupWithNavController(bottomNavigationView, navController)

        navController.addOnDestinationChangedListener { _, destination, _ ->
            if(destination.id == R.id.basketFragment || destination.id == R.id.deliveryInfoFragment || destination.id == R.id.overviewFragment || destination.id == R.id.viewItemsFragment || destination.id == R.id.paymentFragment) {
                bottomNavigationView.visibility = View.GONE
            } else {
                bottomNavigationView.visibility = View.VISIBLE
            }
        }
    }

    fun invisibleBottomBar(){
        bottomNavigationView.visibility = View.GONE
    }

    fun visibleBottomBar(){
        bottomNavigationView.visibility = View.VISIBLE
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

    companion object{
        fun getAppContext(): Application? {
            return Application()
        }
    }

    override fun onResume() {
        super.onResume()
        registerReceiver(broadcastReceiver, IntentFilter("Push"))
    }

    override fun onPause() {
        super.onPause()
        unregisterReceiver(broadcastReceiver)
    }
}