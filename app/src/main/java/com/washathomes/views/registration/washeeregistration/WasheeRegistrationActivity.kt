package com.washathomes.views.registration.washeeregistration

import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Base64
import com.washathomes.R
import dagger.hilt.android.AndroidEntryPoint
import java.io.ByteArrayOutputStream
@AndroidEntryPoint
class WasheeRegistrationActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_washee_registration)
    }

    fun convertToBase64(bitmap: Bitmap): String? {
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
        val byteArray = byteArrayOutputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.DEFAULT)
    }
}