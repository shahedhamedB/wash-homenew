package com.washathomes.views.main.washer.more.profile

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.washathomes.R
import com.washathomes.views.main.washee.WasheeMainActivity
import com.washathomes.databinding.FragmentWasherProfileBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class WasherProfileFragment : Fragment() {

    lateinit var binding: FragmentWasherProfileBinding
    lateinit var navController: NavController
    lateinit var washeeMainActivity: WasheeMainActivity

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val layout = inflater.inflate(R.layout.fragment_washer_profile, container, false)
        binding = FragmentWasherProfileBinding.inflate(layoutInflater)
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
    }

    private fun onClick(){
        binding.toolbarBackIcon.setOnClickListener { navController.popBackStack() }
        binding.switchAccount.setOnClickListener { navController.navigate(WasherProfileFragmentDirections.actionWasherProfileFragmentToWasherSwitchAccountsFragment()) }
        binding.profileDetails.setOnClickListener { navController.navigate(WasherProfileFragmentDirections.actionWasherProfileFragmentToWasherProfileDetailsFragment()) }
        binding.languageSelection.setOnClickListener { navController.navigate(WasherProfileFragmentDirections.actionWasherProfileFragmentToWasherSwitchLanguageFragment()) }
    }

}