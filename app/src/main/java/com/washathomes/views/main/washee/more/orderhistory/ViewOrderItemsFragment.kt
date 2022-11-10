package com.washathomes.views.main.washee.more.orderhistory

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import com.washathomes.apputils.appdefs.AppDefs.Companion.order
import com.washathomes.R
import com.washathomes.views.main.washee.more.orderhistory.adapters.ItemsAdapter
import com.washathomes.views.main.washee.WasheeMainActivity
import com.washathomes.databinding.FragmentViewOrderItemsBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ViewOrderItemsFragment : Fragment() {

    lateinit var binding: FragmentViewOrderItemsBinding
    lateinit var navController: NavController
    lateinit var washeeMainActivity: WasheeMainActivity

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val layout = inflater.inflate(R.layout.fragment_view_order_items, container, false)
        binding = FragmentViewOrderItemsBinding.inflate(layoutInflater)
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
        initViews(view)
        onClick()
    }

    private fun initViews(view: View){
        navController = Navigation.findNavController(view)
        binding.reviewBasketSizeText.text = ""+ order.orders_items.size+" "+resources.getString(R.string.items)
        setItemAdapter()
    }

    private fun onClick() {
        binding.toolbarBackIcon.setOnClickListener { navController.popBackStack() }
    }

    private fun setItemAdapter(){
        val itemsAdapter = ItemsAdapter(this, order.orders_items)
        binding.itemsRV.adapter = itemsAdapter
        binding.itemsRV.layoutManager = LinearLayoutManager(washeeMainActivity)
    }

}