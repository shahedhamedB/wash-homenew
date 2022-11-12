package com.washathomes.views.main.courier.more.profile

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.washathomes.R
import com.washathomes.views.main.courier.CourierMainActivity
import com.washathomes.databinding.FragmentCourierProfileBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CourierProfileFragment : Fragment() {

    lateinit var binding: FragmentCourierProfileBinding
    lateinit var navController: NavController
    lateinit var courierMainActivity: CourierMainActivity

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val layout = inflater.inflate(R.layout.fragment_courier_profile, container, false)
        binding = FragmentCourierProfileBinding.inflate(layoutInflater)
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
    }

    private fun initViews(view: View){
        navController = Navigation.findNavController(view)
    }

    private fun onClick(){
        binding.toolbarBackIcon.setOnClickListener {  navController.popBackStack()}
        binding.languageSelection.setOnClickListener { navController.navigate(CourierProfileFragmentDirections.actionCourierProfileFragmentToCourierSwitchLanguageFragment()) }
        binding.profileDetails.setOnClickListener { navController.navigate(CourierProfileFragmentDirections.actionCourierProfileFragmentToCourierProfileDetailsFragment()) }
        binding.switchAccount.setOnClickListener { navController.navigate(CourierProfileFragmentDirections.actionCourierProfileFragmentToCourierSwitchAccountFragment()) }
    }

}