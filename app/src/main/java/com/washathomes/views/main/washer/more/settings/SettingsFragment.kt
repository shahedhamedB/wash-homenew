package com.washathomes.views.main.washer.more.settings

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.washathomes.R
import com.washathomes.views.main.washer.WasherMainActivity
import com.washathomes.databinding.FragmentSettingsBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SettingsFragment : Fragment() {

    lateinit var binding: FragmentSettingsBinding
    lateinit var navController: NavController
    lateinit var washerMainActivity: WasherMainActivity

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val layout = inflater.inflate(R.layout.fragment_settings, container, false)
        binding = FragmentSettingsBinding.inflate(layoutInflater)
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
    }

    private fun onClick(){
        binding.toolbarBackIcon.setOnClickListener { navController.popBackStack() }
        binding.menuLinePickupReturn.setOnClickListener { navController.navigate(SettingsFragmentDirections.actionSettingsFragmentToPickupReturnFragment()) }
        binding.menuLineIdentificationUpload.setOnClickListener { navController.navigate(SettingsFragmentDirections.actionSettingsFragmentToIdentificationFragment()) }
        binding.menuLineManageService.setOnClickListener { navController.navigate(SettingsFragmentDirections.actionSettingsFragmentToManageServicesFragment()) }
    }



}