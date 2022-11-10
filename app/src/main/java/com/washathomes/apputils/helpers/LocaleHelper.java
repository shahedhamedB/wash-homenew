package com.washathomes.apputils.helpers;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.util.DisplayMetrics;

import java.util.Locale;

public class LocaleHelper {

    @SuppressLint("NewApi")
    public static void setAppLocale(String localeCode, Context context){
        Resources resources = context.getResources();
        DisplayMetrics displayMetrics = resources.getDisplayMetrics();
        Configuration configuration = resources.getConfiguration();
        configuration.setLocale(new Locale(localeCode.toLowerCase()));
        resources.updateConfiguration(configuration, displayMetrics);
        configuration.locale = new Locale(localeCode.toLowerCase());
        resources.updateConfiguration(configuration, displayMetrics);
    }
}
