package com.washathomes.views.main.courier.home.viewitems

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.washathomes.apputils.appdefs.AppDefs
import com.washathomes.R
import com.washathomes.views.main.courier.CourierMainActivity
import com.washathomes.views.main.washer.home.viewitems.ItemsAdapter
import com.washathomes.databinding.FragmentViewItems2Binding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ViewItemsFragment : Fragment() {

    lateinit var binding: FragmentViewItems2Binding
    lateinit var courierMainActivity: CourierMainActivity
    lateinit var navController: NavController
    val args : ViewItemsFragmentArgs by navArgs()
    var isPending = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val layout = inflater.inflate(R.layout.fragment_view_items2, container, false)
        binding = FragmentViewItems2Binding.inflate(layoutInflater)
        return binding.root
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is CourierMainActivity) {
            courierMainActivity = context
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews(view)
        onClick()
        setItemAdapter()
    }

    private fun initViews(view: View){
        navController = Navigation.findNavController(view)
        isPending = args.isPending
        if (isPending){
            binding.reviewBasketSizeText.text = ""+ AppDefs.pendingOrder.orders_items.size+" "+resources.getString(R.string.items)
        }else{
            binding.reviewBasketSizeText.text = ""+ AppDefs.activeOrder.orders_items.size+" "+resources.getString(R.string.items)
        }
    }

    private fun onClick(){
        binding.toolbarBackIcon.setOnClickListener { navController.popBackStack() }
    }

    private fun setItemAdapter(){
        if (isPending){
            val itemsAdapter = ItemsAdapter(AppDefs.pendingOrder.orders_items)
            binding.itemsRV.adapter = itemsAdapter
            binding.itemsRV.layoutManager = LinearLayoutManager(courierMainActivity)
        }else{
            val itemsAdapter = ItemsAdapter(AppDefs.activeOrder.orders_items)
            binding.itemsRV.adapter = itemsAdapter
            binding.itemsRV.layoutManager = LinearLayoutManager(courierMainActivity)
        }
    }

}