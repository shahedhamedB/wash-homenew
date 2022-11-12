package com.washathomes.views.main.washer.more.orderhistory

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import com.washathomes.apputils.appdefs.AppDefs
import com.washathomes.R
import com.washathomes.views.main.washer.more.orderhistory.adapters.ItemsAdapter
import com.washathomes.views.main.washer.WasherMainActivity
import com.washathomes.databinding.FragmentWasherViewOrderItemsBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class WasherViewOrderItemsFragment : Fragment() {

    lateinit var binding: FragmentWasherViewOrderItemsBinding
    lateinit var washerMainActivity: WasherMainActivity
    lateinit var navController: NavController

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val layout = inflater.inflate(R.layout.fragment_washer_view_order_items, container, false)
        binding = FragmentWasherViewOrderItemsBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is WasherMainActivity) {
            washerMainActivity = context
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews(view)
        onClick()
    }

    private fun initViews(view: View){
        navController = Navigation.findNavController(view)
        binding.reviewBasketSizeText.text = ""+ AppDefs.orderHistory.orders_items.size+" "+resources.getString(R.string.items)
        setItemAdapter()
    }

    private fun onClick() {
        binding.toolbarBackIcon.setOnClickListener { navController.popBackStack() }
    }

    private fun setItemAdapter(){
        val itemsAdapter = ItemsAdapter(this, AppDefs.orderHistory.orders_items)
        binding.itemsRV.adapter = itemsAdapter
        binding.itemsRV.layoutManager = LinearLayoutManager(washerMainActivity)
    }
}