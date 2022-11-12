package com.washathomes.views.main.washee.orders.completed

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.washathomes.apputils.appdefs.AppDefs
import com.washathomes.R
import com.washathomes.views.main.washee.WasheeMainActivity
import com.washathomes.databinding.FragmentOrderCompletedBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class OrderCompletedFragment : Fragment() {

    lateinit var binding: FragmentOrderCompletedBinding
    lateinit var washeeMainActivity: WasheeMainActivity
    lateinit var navController: NavController

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val layout = inflater.inflate(R.layout.fragment_order_completed, container, false)
        binding = FragmentOrderCompletedBinding.inflate(layoutInflater)
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
        setData()
    }

    private fun initViews(view: View){
        navController = Navigation.findNavController(view)

    }

    private fun onClick(){
        binding.toolbarBackIcon.setOnClickListener { navController.popBackStack() }
        binding.reviewOrderBtn.setOnClickListener { navController.navigate(OrderCompletedFragmentDirections.actionOrderCompletedFragmentToOrderReviewFragment()) }
    }

    private fun setData(){
        binding.orderPlacedOrderNo.text = resources.getString(R.string.order) + "#" + AppDefs.washeeActiveOrder.id
        binding.orderPlacedStatus.text = resources.getString(R.string.order_confirmed_on)+" "+ AppDefs.washeeActiveOrder.time+" "+ AppDefs.washeeActiveOrder.date
    }

}