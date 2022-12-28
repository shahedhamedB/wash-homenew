package com.washathomes.views.introduction

import android.content.ActivityNotFoundException
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageInfo
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.content.ContextCompat
import com.google.firebase.FirebaseApp
import com.washathomes.R
import com.washathomes.apputils.appdefs.Urls
import com.washathomes.apputils.modules.Version
import com.washathomes.apputils.remote.RetrofitAPIs
import dagger.hilt.android.AndroidEntryPoint
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

@AndroidEntryPoint
class IntroductionActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_introduction)
        FirebaseApp.initializeApp(this)

        getVersion()
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
                goToMarket.addFlags(
                    Intent.FLAG_ACTIVITY_NO_HISTORY or
                        Intent.FLAG_ACTIVITY_NEW_DOCUMENT or
                        Intent.FLAG_ACTIVITY_MULTIPLE_TASK)
                try {
                    startActivity(goToMarket)
                } catch (e: ActivityNotFoundException) {
                    startActivity(
                        Intent(
                            Intent.ACTION_VIEW,
                        Uri.parse("https://play.google.com/store/apps/details?id=$packageName"))
                    )
                }
            }
            .create()
        alertDialog.setOnShowListener { dialog: DialogInterface? ->
            val color = ContextCompat.getColor(this, R.color.colorAccent)
            alertDialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE).setTextColor(color)
        }
        alertDialog.show()
    }
}