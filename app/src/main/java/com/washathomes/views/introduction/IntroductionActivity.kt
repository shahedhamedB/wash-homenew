package com.washathomes.views.introduction

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.FirebaseApp
import com.washathomes.R
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class IntroductionActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_introduction)
        FirebaseApp.initializeApp(this)
    }
}