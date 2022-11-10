package com.washathomes.base.util

import android.content.SharedPreferences
import androidx.core.content.edit

import java.util.*
import javax.inject.Inject

class Prefs @Inject constructor(
    private val sharedPrefs: SharedPreferences,
) {

    var language: String?
        get() {
            return sharedPrefs.getString(AppConstants.defaultLanguageKey, Locale.getDefault().language)
                ?: Locale.getDefault().language
        }
        set(value) {
            sharedPrefs.edit { putString(AppConstants.defaultLanguageKey, value) }
        }


}