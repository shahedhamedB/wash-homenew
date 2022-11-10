package com.washathomes.views.main.washee.chats

import android.os.Bundle
import androidx.lifecycle.MutableLiveData
import com.google.firebase.database.*
import com.washathomes.apputils.appdefs.AppDefs
import com.washathomes.apputils.base.BaseViewModel
import com.washathomes.apputils.modules.chatmodel.ChatMessage
import com.washathomes.apputils.modules.chatmodel.ChatRoom

import com.washathomes.retrofit.remote.APIManager
import dagger.hilt.android.lifecycle.HiltViewModel
import timber.log.Timber
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList
import kotlin.collections.set
@HiltViewModel
class WasheeChatViewModel @Inject constructor(val apiManager: APIManager) : BaseViewModel() {

    var chatRoom: ChatRoom? = null
    val liveData = MutableLiveData<ModelState>()
    lateinit var database: DatabaseReference
    lateinit var chatDataReference: DatabaseReference

    override fun handleIntent(extras: Bundle) {
        super.handleIntent(extras)
        chatRoom = extras.getParcelable("chat")
    }

    fun getData() {
        Timber.d("getChatRoom for order : ${chatRoom?.orderId}")
        listenData()
        stateLiveData.value = State.ShowContent
    }

    private fun listenData() {
        val listener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                Timber.d("ValueEventListener.dataSnapshot : $dataSnapshot")
                try {
                    chatRoom = dataSnapshot.getValue(ChatRoom::class.java)!!

                    chatRoom?.roomKey = dataSnapshot.key ?: ""
                    chatRoom?.let {
                        it.messages = ArrayList()
                        val messageRef = dataSnapshot.child("messages")
                        for (msgSnapshot: DataSnapshot in messageRef.children) {
                            it.messages.add(msgSnapshot.getValue(ChatMessage::class.java)!!)
                        }
                    }
                    liveData.value = ModelState.OnDataUpdate
                } catch (e: Exception) {
                    Timber.e(e)
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
            }
        }

        chatDataReference.addValueEventListener(listener)
    }

    fun sendMessage(msg: String) {
        chatRoom?.let {
            it.messages.add(
                ChatMessage(
                    message = msg,
                    senderId = AppDefs.user.results!!.id!!.toInt(),
                    createTime = System.currentTimeMillis()
                )
            )
            val childUpdates = HashMap<String, Any?>()
            childUpdates["${AppDefs.INBOX_PATH}/${it.roomKey}"] = it.toMap()
            database.updateChildren(childUpdates)
        }
    }

    sealed class ModelState {
        object OnDataUpdate : ModelState()
    }
}