package com.washathomes.views.main.washee.chats

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.washathomes.R
import com.washathomes.apputils.appdefs.AppDefs
import com.washathomes.base.extension.observe
import com.washathomes.views.main.washee.WasheeMainActivity
import com.washathomes.databinding.FragmentWasheeChatBinding
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@AndroidEntryPoint
class WasheeChatFragment : Fragment() {

    lateinit var binding: FragmentWasheeChatBinding
    lateinit var washeeMainActivity: WasheeMainActivity
    lateinit var navController: NavController
    private val viewModel by viewModels<WasheeChatViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val layout = inflater.inflate(R.layout.fragment_washee_chat, container, false)
        binding = FragmentWasheeChatBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is WasheeMainActivity) {
            washeeMainActivity = context
        }
    }

    private lateinit var adapter: ChatAdapter
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.database= FirebaseDatabase.getInstance().reference
        viewModel.chatRoom = arguments?.getParcelable("chat")!!
        viewModel.chatDataReference = viewModel.database.child("${AppDefs.INBOX_PATH}/${viewModel.chatRoom?.roomKey}")

        viewModel.getData()
        initFragment()
        binding.toolbarBackIcon.setOnClickListener {
            findNavController().popBackStack()
        }
    }

     fun initFragment() {

        Timber.d("WasheeChatFragment init")
        adapter = ChatAdapter(viewModel.chatRoom?.order)
        adapter.setHasStableIds(true)
        binding.chatList.setHasFixedSize(true)
        binding.chatList.layoutManager = LinearLayoutManager(context)
        binding.chatList.smoothScrollToPosition(View.FOCUS_DOWN)
        binding.chatList.adapter = adapter
        observe(viewModel.liveData, ::onStateChanged)
        adapter.items = viewModel.chatRoom?.messages
        binding.chatSendBtn.setOnClickListener { sendMessage() }

    }

    private fun sendMessage() {
        val msg = binding.chatMessageEdit.text.toString()
        viewModel.sendMessage(msg)
        binding.chatMessageEdit.text?.clear()
    }

    private fun onStateChanged(state: WasheeChatViewModel.ModelState) {
        when (state) {
            is WasheeChatViewModel.ModelState.OnDataUpdate -> {
                adapter.items = viewModel.chatRoom?.messages
                binding.chatList.smoothScrollToPosition(View.FOCUS_DOWN)
            }
        }
    }



}