package com.washathomes.views.main.washer.home.orderdelivered

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
import com.washathomes.views.main.washer.WasherMainActivity
import com.washathomes.databinding.FragmentOrderDeliveredBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class OrderDeliveredFragment : Fragment() {

    lateinit var binding: FragmentOrderDeliveredBinding
    lateinit var washerMainActivity: WasherMainActivity
    lateinit var navController: NavController

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val layout = inflater.inflate(R.layout.fragment_order_delivered, container, false)
        binding = FragmentOrderDeliveredBinding.inflate(layoutInflater)
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
        setData()
    }

    private fun initViews(view: View){
        navController = Navigation.findNavController(view)
    }

    private fun onClick(){
        binding.toolbarBackIcon.setOnClickListener { navController.popBackStack() }
        binding.reviewOrderBtn.setOnClickListener { navController.navigate(OrderDeliveredFragmentDirections.actionOrderDeliveredFragmentToOrderReviewFragment4()) }
    }

    private fun setData(){
        binding.orderCompletedCodeText.text = resources.getString(R.string.order)+" #"+ AppDefs.activeOrder.id
        binding.orderCompletedDate.text = resources.getString(R.string.order_placed_on)+" "+ AppDefs.activeOrder.time+" "+ AppDefs.activeOrder.date
    }

}