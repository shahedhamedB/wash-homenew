package com.washathomes.views.registration.washerregistration

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.gson.Gson
import com.washathomes.apputils.appdefs.AppDefs
import com.washathomes.apputils.appdefs.Urls
import com.washathomes.apputils.helpers.FirebaseHelper
import com.washathomes.apputils.remote.RetrofitAPIs
import com.washathomes.apputils.modules.*
import com.washathomes.R
import com.washathomes.views.main.washer.WasherMainActivity
import com.washathomes.databinding.FragmentWasherServicesDataBinding
import dagger.hilt.android.AndroidEntryPoint
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.InputStream
@AndroidEntryPoint
class WasherServicesDataFragment : Fragment() {

    lateinit var binding: FragmentWasherServicesDataBinding
    lateinit var washerRegistrationActivity: WasherRegistrationActivity
    lateinit var navController: NavController
    var services: ArrayList<Service> = ArrayList()
    private val REQUEST_IMAGE_GALLERY = 101
    private val REQUEST_IMAGE_CAPTURE = 111
    private val REQUEST_CODE = 110
    var serviceAvailable = "1"
    var expressAvailable = "0"
    var imageType = 1
    var washingMachineImage = ""
    var dryerImage = ""
    var extraImage = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val layout = inflater.inflate(R.layout.fragment_washer_services_data, container, false)
        binding = FragmentWasherServicesDataBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews(view)
        onClick()
        getServices()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is WasherRegistrationActivity) {
            washerRegistrationActivity = context
        }
    }

    private fun initViews(view: View){
        navController = Navigation.findNavController(view)
    }

    private fun onClick(){
        binding.availabilitySwitch.setOnClickListener {
            if (serviceAvailable == "1"){
                serviceAvailable = "0"
                Glide.with(washerRegistrationActivity).load(R.drawable.switch_off).into(binding.availabilitySwitch)
            }else{
                serviceAvailable = "1"
                Glide.with(washerRegistrationActivity).load(R.drawable.switch_on).into(binding.availabilitySwitch)
            }
        }
        binding.expressSwitch.setOnClickListener {
            if (expressAvailable == "1"){
                expressAvailable = "0"
                Glide.with(washerRegistrationActivity).load(R.drawable.switch_off).into(binding.expressSwitch)
            }else{
                expressAvailable = "1"
                Glide.with(washerRegistrationActivity).load(R.drawable.switch_on).into(binding.expressSwitch)
            }
        }
        binding.uploadWashingMachineImage.setOnClickListener {
            imageType = 1
            imagePickerPopUp()
        }
        binding.uploadDryerImage.setOnClickListener {
            imageType = 2
            imagePickerPopUp()
        }
        binding.uploadExtraImage.setOnClickListener {
            imageType = 3
            imagePickerPopUp()
        }
        binding.doneBtn.setOnClickListener { checkValidation() }
    }

    private fun getServices(){
        binding.progressBar.visibility = View.VISIBLE
        binding.servicesLayout.visibility = View.GONE
        val okHttpClient = OkHttpClient.Builder().apply {
            addInterceptor(
                Interceptor { chain ->
                    val builder = chain.request().newBuilder()
                    builder.header("Content-Type", "application/json; charset=UTF-8")
                    builder.header("Authorization", AppDefs.user.token!!)
                    return@Interceptor chain.proceed(builder.build())
                }
            )
        }.build()
        val retrofit: Retrofit = Retrofit.Builder().baseUrl(Urls.BASE_URL).client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create()).build()
        val servicesCall: Call<Services> =
            retrofit.create(RetrofitAPIs::class.java).getServices()
        servicesCall.enqueue(object : Callback<Services>{
            override fun onResponse(call: Call<Services>, response: Response<Services>) {
                if (response.isSuccessful){
                    for (result in response.body()!!.results) {
                        result.isSelected = true
                        services.add(result)
                    }
                    setServicesAdapter()
                    binding.progressBar.visibility = View.GONE
                    binding.servicesLayout.visibility = View.VISIBLE
                }
            }

            override fun onFailure(call: Call<Services>, t: Throwable) {
                TODO("Not yet implemented")
            }

        })
    }

    private fun setServicesAdapter() {
        val servicesAdapter = ServicesAdapter(this, (services))
        binding.servicesRV.adapter = servicesAdapter
        binding.servicesRV.layoutManager = LinearLayoutManager(washerRegistrationActivity, RecyclerView.HORIZONTAL, false)
    }

    private fun imagePickerPopUp() {
        val alertView: View = LayoutInflater.from(context).inflate(R.layout.image_picker_dialog, null)
        val alertBuilder = AlertDialog.Builder(context).setView(alertView).show()
        alertBuilder.show()
        alertBuilder.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val camera: LinearLayoutCompat = alertView.findViewById(R.id.camera)
        val gallery: LinearLayoutCompat = alertView.findViewById(R.id.gallery)

        camera.setOnClickListener {
            if (ContextCompat.checkSelfPermission(washerRegistrationActivity, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_DENIED){
                ActivityCompat.requestPermissions(
                    washerRegistrationActivity, arrayOf(
                        Manifest.permission.CAMERA),
                    REQUEST_CODE
                )
            }else{
                dispatchTakePictureIntent()
            }
            alertBuilder.dismiss()
        }
        gallery.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, REQUEST_IMAGE_GALLERY)
        }

    }

    private fun dispatchTakePictureIntent() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        try {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
        } catch (e: ActivityNotFoundException) {
            // display error state to the user
        }
    }

    private fun checkValidation(){
        if (serviceAvailable == "1"){
            if (washingMachineImage.isEmpty() || dryerImage.isEmpty()){
                Toast.makeText(washerRegistrationActivity, resources.getString(R.string.upload_images), Toast.LENGTH_SHORT).show()
            }else{
                updateUser()
            }
        }else{
            Toast.makeText(washerRegistrationActivity, resources.getString(R.string.enable_services_availability), Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateUser(){
        val servicesAvailable: ArrayList<ServiceAvailable> = ArrayList()
        for (service in services){
            if (service.isSelected!!){
                servicesAvailable.add(ServiceAvailable(service.id))
            }
        }
        val userParams = UpdateServicesData(serviceAvailable, expressAvailable, servicesAvailable, washingMachineImage, dryerImage, extraImage)
        binding.progressBar.visibility = View.VISIBLE
        val okHttpClient = OkHttpClient.Builder().apply {
            addInterceptor(
                Interceptor { chain ->
                    val builder = chain.request().newBuilder()
                    builder.header("Content-Type", "application/json; charset=UTF-8")
                    builder.header("Authorization", AppDefs.user.token!!)
                    return@Interceptor chain.proceed(builder.build())
                }
            )
        }.build()
        val retrofit: Retrofit = Retrofit.Builder().baseUrl(Urls.BASE_URL).client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create()).build()
        val updateServicesDataCall: Call<UserData> =
            retrofit.create(RetrofitAPIs::class.java).updateServicesData(userParams)
        updateServicesDataCall.enqueue(object : Callback<UserData> {
            override fun onResponse(call: Call<UserData>, response: Response<UserData>) {
                AppDefs.user = response.body()!!
                binding.progressBar.visibility = View.GONE
                saveUserToSharedPreferences()
            }

            override fun onFailure(call: Call<UserData>, t: Throwable) {
                TODO("Not yet implemented")
            }

        })
    }

    private fun saveUserToSharedPreferences() {
        FirebaseHelper.sendUserToDatabase(AppDefs.user.results!!.language_code!!)
        val sharedPreferences =
            washerRegistrationActivity.getSharedPreferences(AppDefs.SHARED_PREF_KEY, Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString(AppDefs.ID_KEY, AppDefs.user.results!!.id)

        val gson = Gson()
        val json = gson.toJson(AppDefs.user)
        editor.putString(AppDefs.USER_KEY, json)
        editor.putString(AppDefs.TYPE_KEY, "2")
        editor.apply()
        val registrationIntent = Intent(washerRegistrationActivity, WasherMainActivity::class.java)
        startActivity(registrationIntent)
        washerRegistrationActivity.finish()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
            when (imageType) {
                1 -> {
                    binding.uploadWashingMachineImageLayout.visibility = View.GONE
                    val imageBitmap = data?.extras?.get("data") as Bitmap
                    binding.washingMachineImage.setImageBitmap(imageBitmap)
                    washingMachineImage = washerRegistrationActivity.convertToBase64(imageBitmap)!!
                }
                2 -> {
                    binding.uploadDryerImageLayout.visibility = View.GONE
                    val imageBitmap = data?.extras?.get("data") as Bitmap
                    binding.dryerImage.setImageBitmap(imageBitmap)
                    dryerImage = washerRegistrationActivity.convertToBase64(imageBitmap)!!
                }
                3 -> {
                    binding.uploadExtraImageLayout.visibility = View.GONE
                    val imageBitmap = data?.extras?.get("data") as Bitmap
                    binding.extraImage.setImageBitmap(imageBitmap)
                    extraImage = washerRegistrationActivity.convertToBase64(imageBitmap)!!
                }
            }
        }else if (requestCode == REQUEST_IMAGE_GALLERY && resultCode == Activity.RESULT_OK){
            when (imageType) {
                1 -> {
                    binding.uploadWashingMachineImageLayout.visibility = View.GONE
                    val inputStream: InputStream = requireContext().contentResolver.openInputStream(data!!.data!!)!!
                    val imageBitmap = BitmapFactory.decodeStream(inputStream)
                    binding.washingMachineImage.setImageBitmap(imageBitmap)
                    washingMachineImage = washerRegistrationActivity.convertToBase64(imageBitmap)!!
                }
                2 -> {
                    binding.uploadDryerImageLayout.visibility = View.GONE
                    val inputStream: InputStream = requireContext().contentResolver.openInputStream(data!!.data!!)!!
                    val imageBitmap = BitmapFactory.decodeStream(inputStream)
                    binding.dryerImage.setImageBitmap(imageBitmap)
                    dryerImage = washerRegistrationActivity.convertToBase64(imageBitmap)!!
                }
                3 -> {
                    binding.uploadExtraImageLayout.visibility = View.GONE
                    val inputStream: InputStream = requireContext().contentResolver.openInputStream(data!!.data!!)!!
                    val imageBitmap = BitmapFactory.decodeStream(inputStream)
                    binding.extraImage.setImageBitmap(imageBitmap)
                    extraImage = washerRegistrationActivity.convertToBase64(imageBitmap)!!
                }
            }
        }else if (requestCode == REQUEST_CODE && resultCode == Activity.RESULT_OK){
            dispatchTakePictureIntent()
        }
    }

}