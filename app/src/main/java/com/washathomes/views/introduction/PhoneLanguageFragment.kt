package com.washathomes.views.introduction

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.bumptech.glide.Glide
import com.washathomes.apputils.appdefs.AppDefs
import com.washathomes.apputils.appdefs.AppDefs.Companion.lang
import com.washathomes.apputils.appdefs.AppDefs.Companion.languages
import com.washathomes.apputils.appdefs.Urls
import com.washathomes.apputils.helpers.LocaleHelper
import com.washathomes.apputils.remote.RetrofitAPIs
import com.washathomes.apputils.modules.Languages
import com.washathomes.R
import com.washathomes.databinding.FragmentPhoneLanguageBinding
import dagger.hilt.android.AndroidEntryPoint
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

@AndroidEntryPoint
class PhoneLanguageFragment : Fragment() {

    lateinit var binding: FragmentPhoneLanguageBinding
    lateinit var introductionActivity: IntroductionActivity
    lateinit var navController: NavController


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val layout = inflater.inflate(R.layout.fragment_phone_language, container, false)
        binding = FragmentPhoneLanguageBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is IntroductionActivity) {
            introductionActivity = context
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (languages.size == 0){
            getLanguages()
        }else{
            setSpinner()
        }
        initViews(view)
        onClick()

    }

    private fun initViews(view: View){
        navController = Navigation.findNavController(view)
        Glide.with(introductionActivity).load(AppDefs.whiteLogo).into(binding.logo)
        Glide.with(introductionActivity).load(AppDefs.background).into(binding.background)
    }

    private fun onClick(){
        binding.continueBtn.setOnClickListener {  checkValidation()}
        binding.languagesSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onNothingSelected(parent: AdapterView<*>?) {

            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (position == 1 && lang != "es") {
                    lang = "es"
                    LocaleHelper.setAppLocale(lang, context)
                    introductionActivity.recreate()
                    return
                } else if (position == 0 && lang != "en"){
                    lang = "en"
                    LocaleHelper.setAppLocale(lang, context)
                    introductionActivity.recreate()
                }
            }

        }
    }

    private fun getLanguages(){
        val retrofit: Retrofit = Retrofit.Builder().baseUrl(Urls.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create()).build()
        val languagesCall: Call<Languages> =
            retrofit.create(RetrofitAPIs::class.java).getLanguages()
        languagesCall.enqueue(object : Callback<Languages>{
            override fun onResponse(call: Call<Languages>, response: Response<Languages>) {
                if (response.isSuccessful){
                    for (i in response.body()?.results!!){
                        languages.add(i)
                    }
                    setSpinner()
                }
            }

            override fun onFailure(call: Call<Languages>, t: Throwable) {
                TODO("Not yet implemented")
            }

        })
    }

    private fun setSpinner() {
        val adapter = LanguagesAdapter(introductionActivity, languages)
        binding.languagesSpinner.adapter = adapter
    }

    private fun checkValidation(){
        var phoneNum = binding.phoneNumEdt.text.toString()
        if (phoneNum.isEmpty()){
            binding.phoneNumEdt.error = resources.getString(R.string.fill_feild)
        }else if (!binding.termsPrivacyCB.isChecked){
            Toast.makeText(introductionActivity, resources.getString(R.string.accept_terms), Toast.LENGTH_LONG).show()
        }else{
            phoneNum = binding.phoneCcp.selectedCountryCodeWithPlus+phoneNum
            navController.navigate(PhoneLanguageFragmentDirections.actionPhoneLanguageFragmentToVerificationFragment(phoneNum))
          //  findNavController().navigate(R.id.action_phoneLanguageFragment_to_verificationFragment)
        }
    }
}