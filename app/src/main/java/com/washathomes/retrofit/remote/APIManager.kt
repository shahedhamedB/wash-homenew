package com.washathomes.retrofit.remote

import android.app.Application
import com.google.gson.Gson
import com.washathomes.apputils.modules.chatmodel.Order
import com.washathomes.retrofit.BaseDataSource

import javax.inject.Inject
import io.reactivex.Observable

class APIManager @Inject constructor(
    private val mApi: WashAPI,

    ) : BaseDataSource() {


    suspend fun getBuyerOrdersChat(token: String) = getResult {

        mApi.getBuyerOrdersChat(token)
    }

    suspend fun getSellerOrdersChat(token: String) = getResult {
        mApi.getSellerOrdersChat(token)
    }
    suspend fun getDriverOrdersChat(token: String) = getResult {

        mApi.getDriverOrdersChat(token)
    }


}