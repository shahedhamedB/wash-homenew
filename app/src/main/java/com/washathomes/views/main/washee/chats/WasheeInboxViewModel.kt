package com.washathomes.views.main.washee.chats

import android.annotation.SuppressLint
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.washathomes.apputils.appdefs.AppDefs
import com.washathomes.apputils.base.BaseViewModel

import com.washathomes.apputils.modules.chatmodel.ChatMessage
import com.washathomes.apputils.modules.chatmodel.ChatRoom
import com.washathomes.apputils.modules.chatmodel.Order
import com.washathomes.apputils.modules.chatnew.OrderListResponse
import com.washathomes.apputils.modules.chatnew.OrderModel
import com.washathomes.retrofit.Resource


import com.washathomes.retrofit.remote.APIManager
import dagger.hilt.android.lifecycle.HiltViewModel


import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.Observables
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject
@HiltViewModel
class WasheeInboxViewModel @Inject constructor(private val apiManager: APIManager) :
    BaseViewModel() {

    var chatList: ArrayList<ChatRoom> = ArrayList()
    var orders: List<OrderModel> = ArrayList()

    val getBuyerOrderChatStatus = MutableLiveData<Resource<OrderListResponse>?>()
  /*  val getSellerOrdersChatStatus = MutableLiveData<Resource<OrderListResponse>?>()
    val getDriverOrdersChatStatus = MutableLiveData<Resource<OrderListResponse>?>()*/
    val database = FirebaseDatabase.getInstance()
    val myRef = database.reference
    fun getBuyerOrdersChat(token: String) {
        viewModelScope.launch {
            val response = apiManager.getBuyerOrdersChat(token)
            getBuyerOrderChatStatus.postValue(response)
        }
    }

    fun getSellerOrdersChat(token: String) {
        viewModelScope.launch {
            val response = apiManager.getSellerOrdersChat(token)
            getBuyerOrderChatStatus.postValue(response)
        }
    }
    fun getDriverOrdersChat(token: String) {
        viewModelScope.launch {
            val response = apiManager.getDriverOrdersChat(token)
            getBuyerOrderChatStatus.postValue(response)
        }
    }





}