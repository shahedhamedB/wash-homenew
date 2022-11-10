package com.washathomes.views.main.washee.chats

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.Query
import com.google.firebase.database.ValueEventListener
import com.washathomes.R
import com.washathomes.apputils.appdefs.AppDefs
import com.washathomes.apputils.base.BaseViewModel
import com.washathomes.apputils.modules.chatmodel.ChatMessage
import com.washathomes.apputils.modules.chatmodel.ChatRoom
import com.washathomes.apputils.modules.chatmodel.Order
import com.washathomes.apputils.modules.chatnew.OrderModel
import com.washathomes.base.ui.MultiStateView
import com.washathomes.databinding.FragmentWasheeChatsBinding
import com.washathomes.retrofit.Resource
import com.washathomes.views.main.washee.WasheeMainActivity
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import java.util.*
import kotlin.collections.ArrayList


@AndroidEntryPoint
class WasheeChatsFragment : Fragment() {

    lateinit var binding: FragmentWasheeChatsBinding
    lateinit var washeeMainActivity: WasheeMainActivity
    lateinit var navController: NavController

    private lateinit var adapter: WasheeInboxListAdapter
    private var dataCalled = false
    private var hasRequestSend = false
    private val viewModel by viewModels<WasheeInboxViewModel>()
     var multiStateView: MultiStateView? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentWasheeChatsBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is WasheeMainActivity) {
            washeeMainActivity = context
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Timber.d("WasheeInboxFragment init")
        hasRequestSend = savedInstanceState?.getBoolean(REQUEST_SEND, false) ?: false
        if (!hasRequestSend) {
            initStartRequest()
            hasRequestSend = true
        }
        adapter = WasheeInboxListAdapter()
        adapter.setHasStableIds(true)
        adapter.onClickListener = ::onClickListener
        multiStateView = binding.multiStateView
        binding.inboxList.setHasFixedSize(true)
        binding.inboxList.layoutManager = LinearLayoutManager(context)
        binding.inboxList.adapter = adapter





    }
    private fun setInbox() {

        if (viewModel.chatList.isNullOrEmpty()) {

            setCustomEmptyView(
                R.drawable.ic_empty_inbox,
                R.string.empty_inbox_title,
                R.string.empty_inbox_description,
                -1
            )
        } else {
            adapter.items = viewModel.chatList

        }
    }





    private fun onClickListener(inboxItem: ChatRoom?) {
        Timber.d("Clicked Message belongs to Order with id : ${inboxItem?.orderId}")
        if (inboxItem != null)
            if (inboxItem.order != null) {
              //  viewModel.getBuyerOrder(inboxItem)
                getBuyer(inboxItem.order!!)
            }else{
                Toast.makeText(
                    context,
                    getString(R.string.chat_no_available),
                    Toast.LENGTH_SHORT
                ).show()
            }
    }



     fun initStartRequest() {
        // viewModel.getData(AppDefs.user.token!!)
         getInfo()

         dataCalled = true
    }

    private fun getbuyerfromfirebas() {
        if (AppDefs.user.results!!.sigup_type == "1"){
            checkRes(viewModel.myRef.child(AppDefs.INBOX_PATH).orderByChild("buyerId")
                .equalTo(AppDefs.user.results!!.id.toString()))
        }else if (AppDefs.user.results!!.sigup_type == "2"){
            checkRes(viewModel.myRef.child(AppDefs.INBOX_PATH).orderByChild("sellerId")
                .equalTo(AppDefs.user.results!!.id.toString()))
        }else if (AppDefs.user.results!!.sigup_type == "3"){
            checkRes(viewModel.myRef.child(AppDefs.INBOX_PATH).orderByChild("driverId")
                .equalTo(AppDefs.user.results!!.id.toString()))
        }


    }
    fun checkRes(query:Query){
        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                Timber.d("WasheeInboxViewModel.chatsValueEventListener.onCancelled")
            }

            override fun onDataChange(dataSnapshot: DataSnapshot) {
//                        gson.fromJson(dataSnapshot.toString(), ChatRoom.class)
                Timber.d("dataSnapshot : $dataSnapshot")
                viewModel.chatList = ArrayList()
                for (roomSnapshot: DataSnapshot in dataSnapshot.children) {
                    val chatRoom: ChatRoom = roomSnapshot.getValue(ChatRoom::class.java)!!
                    chatRoom.roomKey = roomSnapshot.key ?: ""
                    Log.d("keyroom", chatRoom.roomKey)

                    chatRoom.let {
                        it.messages = ArrayList()
                        val messageRef = roomSnapshot.child("messages")
                        for (msgSnapshot: DataSnapshot in messageRef.children) {
                            it.messages.add(msgSnapshot.getValue(ChatMessage::class.java)!!)
                        }
                    }
                    if (chatRoom.orderId.isNotEmpty()) {
                        //chatRoom.order = viewModel.orders.find { order -> order.id == chatRoom.orderId }


                        chatRoom.order=
                            viewModel.orders.find { order -> order.id == chatRoom.orderId }
                                ?.let { toMapper(it) }
                        viewModel.chatList.add(chatRoom)
                        setInbox()

                    }
                }
                viewModel.stateLiveData.value = BaseViewModel.State.ShowContent
            }
        })
    }

    fun setCustomEmptyView(emptyImage: Int, emptyTitle: Int, emptyDescription: Int, actionButtonText: Int, buttonClick: (() -> Unit)? = null) {
       if (multiStateView?.getView(MultiStateView.ViewState.EMPTY) == null) {
         //   val viewEmpty = LayoutInflater.from(context).inflate(R.layout.view_empty, binding.root)
           val viewEmpty: View = layoutInflater.inflate(R.layout.view_empty, null)

           viewEmpty.findViewById<ImageView>(R.id.empty_image).setImageResource(emptyImage)
            viewEmpty.findViewById<TextView>(R.id.empty_title).setText(emptyTitle)
            viewEmpty.findViewById<TextView>(R.id.empty_description).setText(emptyDescription)
            val button = viewEmpty.findViewById<TextView>(R.id.empty_action_button)
            if (actionButtonText > 0)
                button.setText(actionButtonText)
            else
                button.visibility = View.GONE
            button.setOnClickListener {
                if (buttonClick != null) {
                    buttonClick()
                }
            }
            multiStateView?.setViewForState(viewEmpty, MultiStateView.ViewState.EMPTY, true)
        } else
            multiStateView?.viewState = MultiStateView.ViewState.EMPTY
    }
    companion object {
        private const val REQUEST_SEND = "REQUEST_SEND"
    }
    fun getInfo(){
        if (AppDefs.user.results!!.sigup_type == "1"){
            viewModel.getBuyerOrdersChat(AppDefs.user.token!!)
        }else if (AppDefs.user.results!!.sigup_type == "2"){
            viewModel.getSellerOrdersChat(AppDefs.user.token!!)
        }else if (AppDefs.user.results!!.sigup_type == "3"){
            viewModel.getDriverOrdersChat(AppDefs.user.token!!)
        }
        viewModel.getBuyerOrderChatStatus.observe(viewLifecycleOwner, Observer {
            when (it!!.status) {
                Resource.Status.SUCCESS -> {

                    viewModel.orders=it.data!!.results
                    getbuyerfromfirebas()

                }
                Resource.Status.ERROR -> {

                }
            }
        })



    }

    fun getBuyer(order:Order){
        if (AppDefs.user.results!!.sigup_type == "1"){
            findNavController().navigate(R.id.action_navigation_inbox_to_washeeChatFragment, bundleOf(Pair("chat", order.getChatRoom())))

        } else  if (AppDefs.user.results!!.sigup_type == "2"){
            findNavController().navigate(R.id.action_washerInboxFragment_to_washeeChatFragment2, bundleOf(Pair("chat", order.getChatRoom())))

        } else  if (AppDefs.user.results!!.sigup_type == "3"){
            findNavController().navigate(R.id.action_courierInboxFragment_to_washeeChatFragment3, bundleOf(Pair("chat", order.getChatRoom())))

        }
    }

    fun toMapper(data:  OrderModel): Order {

        return Order(
            data.id!!.toInt(),
            "",
            0.0,
            0.0,
            0.0,
            0.0,
            0.0,
            0.0,
            0.0,
            "",
            "",
            Date(),
            "",
            "",
            0,

            Date(),
            0,
            "",
            "",
            "",
            "",
            "",
            Date(),
            "",
            "",
            Date(),
            "",
            0,
            Date(),
            Date(),
            Date(),
            Date(),
            Date(),
            Date(),
            "",
            0.0,
            0,
            "",
            "",
            "",

            Date(),
            "",
            "",
            "",
            Date(),
            0,
            0.0,
            0,
            0,
            0,
            "",
            "",
            "",
            Date(),
            "",
            "",
            "",
            0,
            "",
            "",
            "",
            Date(),
            "",
            0,
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            Date(),
            "",
            0,
            0,
            "",
            Date(),
            0,
            "",
            "",
            "",
            "",
            "",
            "",
            "" ,
            "",
            Date(),
            Date(),

            )


    }

}