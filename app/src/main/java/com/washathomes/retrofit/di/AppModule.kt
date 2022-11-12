package com.washathomes.retrofit.di

import android.app.Application
import android.content.SharedPreferences
import android.preference.PreferenceManager
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.washathomes.apputils.appdefs.Urls
import com.washathomes.base.util.Prefs
import com.washathomes.retrofit.interceptors.LanguageInterceptor
import com.washathomes.retrofit.remote.WashAPI
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Singleton
    @Provides
    fun provideRetrofit(gson: Gson, prefs: Prefs): Retrofit = Retrofit.Builder()
        .baseUrl(Urls.BASE_URL)
        .client(getLanguageHeaderOkHttpClient(prefs))
        .addConverterFactory(GsonConverterFactory.create(gson))
        .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
        .build()

    @Provides
    fun provideGson(): Gson = GsonBuilder().create()


    @Provides
    @Singleton
    fun provideSharedPreferences(context: Application): SharedPreferences {
        return PreferenceManager.getDefaultSharedPreferences(context)
    }
    @Provides
    fun provideNetworkService(retrofit: Retrofit): WashAPI = retrofit.create(WashAPI::class.java)

    @Provides
    @Singleton
    fun getLanguageHeaderOkHttpClient(prefs: Prefs): OkHttpClient {
        val builder = OkHttpClient.Builder()
        builder.addInterceptor(HttpLoggingInterceptor().apply {
            this.level = HttpLoggingInterceptor.Level.HEADERS
            this.level = HttpLoggingInterceptor.Level.BODY
        })
            .addInterceptor(LanguageInterceptor(prefs))
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
        return builder.build()
    }

}

