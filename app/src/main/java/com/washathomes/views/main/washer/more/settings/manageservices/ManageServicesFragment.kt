package com.washathomes.views.main.washer.more.settings.manageservices

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
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.gson.Gson
import com.washathomes.apputils.appdefs.AppDefs
import com.washathomes.apputils.appdefs.Urls
import com.washathomes.apputils.modules.*
import com.washathomes.apputils.remote.RetrofitAPIs
import com.washathomes.R
import com.washathomes.views.main.washer.WasherMainActivity
import com.washathomes.databinding.FragmentManageServicesBinding
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
class ManageServicesFragment : Fragment() {

    lateinit var binding: FragmentManageServicesBinding
    lateinit var washerMainActivity: WasherMainActivity
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
    lateinit var userDocuments: UserDocuments
    
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val layout = inflater.inflate(R.layout.fragment_manage_services, container, false)
        binding = FragmentManageServicesBinding.inflate(layoutInflater)
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
        getServices()
    }

    private fun initViews(view: View){
        navController = Navigation.findNavController(view)
    }

    private fun onClick(){
        binding.toolbarBackIcon.setOnClickListener { navController.popBackStack() }
        binding.uploadWashingMachineImage.setOnClickListener {
            imageType = 1
            imagePickerPopUp()
        }
        binding.uploadExtraImage2.setOnClickListener {
            imageType = 2
            imagePickerPopUp()
        }
        binding.uploadExtraImage.setOnClickListener {
            imageType = 3
            imagePickerPopUp()
        }
        binding.doneBtn.setOnClickListener {
            val servicesAvailable: ArrayList<ServiceAvailable> = ArrayList()
            for (service in services){
                if (service.status == "1"){
                    servicesAvailable.add(ServiceAvailable(service.id))
                }
            }
            if (servicesAvailable.isEmpty()){
                Toast.makeText(washerMainActivity, resources.getString(R.string.choose_services), Toast.LENGTH_SHORT).show()
            }else{
                updateUser(servicesAvailable)
            }
        }
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
        servicesCall.enqueue(object : Callback<Services> {
            override fun onResponse(call: Call<Services>, response: Response<Services>) {
                if (response.isSuccessful){
                    for (result in response.body()!!.results) {
                        result.isSelected = true
                        services.add(result)
                    }
                    setServicesAdapter()
                    binding.progressBar.visibility = View.GONE
                    binding.servicesLayout.visibility = View.VISIBLE
                    getUserDocuments()
                }
            }

            override fun onFailure(call: Call<Services>, t: Throwable) {
                TODO("Not yet implemented")
            }

        })
    }

    private fun setServicesAdapter() {
        val servicesAdapter = ServicesAdapter(this, services)
        binding.servicesRV.adapter = servicesAdapter
        binding.servicesRV.layoutManager = LinearLayoutManager(washerMainActivity, RecyclerView.HORIZONTAL, false)

    }

    private fun getUserDocuments(){
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
        val servicesCall: Call<UserDocs> =
            retrofit.create(RetrofitAPIs::class.java).getUserDocs()
        servicesCall.enqueue(object : Callback<UserDocs> {
            override fun onResponse(call: Call<UserDocs>, response: Response<UserDocs>) {
                if (response.isSuccessful){
                    if (response.body()!!.results != null){
                        userDocuments = response.body()!!.results!!
                        if (userDocuments.machine_image.isNotEmpty()){
                            binding.uploadWashingMachineImageLayout.visibility = View.GONE
                            Glide.with(washerMainActivity).load(userDocuments.machine_image).into(binding.washingMachineImage)
                        }
                        if (userDocuments.dryer_image.isNotEmpty()){
                            binding.uploadExtraImageLayout2.visibility = View.GONE
                            Glide.with(washerMainActivity).load(userDocuments.dryer_image).into(binding.extraImage2)
                        }
                        if (userDocuments.extra_image.isNotEmpty()){
                            binding.uploadExtraImageLayout.visibility = View.GONE
                            Glide.with(washerMainActivity).load(userDocuments.extra_image).into(binding.extraImage)
                        }
                    }
                }
            }

            override fun onFailure(call: Call<UserDocs>, t: Throwable) {
                TODO("Not yet implemented")
            }

        })
    }

    private fun imagePickerPopUp() {
        val alertView: View = LayoutInflater.from(context).inflate(R.layout.image_picker_dialog, null)
        val alertBuilder = AlertDialog.Builder(context).setView(alertView).show()
        alertBuilder.show()
        alertBuilder.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val camera: LinearLayoutCompat = alertView.findViewById(R.id.camera)
        val gallery: LinearLayoutCompat = alertView.findViewById(R.id.gallery)

        camera.setOnClickListener {
            if (ContextCompat.checkSelfPermission(washerMainActivity, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_DENIED){
                ActivityCompat.requestPermissions(
                    washerMainActivity, arrayOf(
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
            alertBuilder.dismiss()
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
    
    private fun updateUser(servicesAvailable: ArrayList<ServiceAvailable>){
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
        val sharedPreferences =
            washerMainActivity.getSharedPreferences(AppDefs.SHARED_PREF_KEY, Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString(AppDefs.ID_KEY, AppDefs.user.results!!.id)

        val gson = Gson()
        val json = gson.toJson(AppDefs.user)
        editor.putString(AppDefs.USER_KEY, json)
        editor.putString(AppDefs.TYPE_KEY, "2")
        editor.apply()
        navController.popBackStack()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
            when (imageType) {
                1 -> {
                    binding.uploadWashingMachineImageLayout.visibility = View.GONE
                    val imageBitmap = data?.extras?.get("data") as Bitmap
                    binding.washingMachineImage.setImageBitmap(imageBitmap)
                    washingMachineImage = washerMainActivity.convertToBase64(imageBitmap)!!
                }
                2 -> {
                    binding.uploadExtraImageLayout2.visibility = View.GONE
                    val imageBitmap = data?.extras?.get("data") as Bitmap
                    binding.extraImage2.setImageBitmap(imageBitmap)
                    dryerImage = washerMainActivity.convertToBase64(imageBitmap)!!
                }
                3 -> {
                    binding.uploadExtraImageLayout.visibility = View.GONE
                    val imageBitmap = data?.extras?.get("data") as Bitmap
                    binding.extraImage.setImageBitmap(imageBitmap)
                    extraImage = washerMainActivity.convertToBase64(imageBitmap)!!
                }
            }
        }else if (requestCode == REQUEST_IMAGE_GALLERY && resultCode == Activity.RESULT_OK){
            when (imageType) {
                1 -> {
                    binding.uploadWashingMachineImageLayout.visibility = View.GONE
                    val inputStream: InputStream = requireContext().contentResolver.openInputStream(data!!.data!!)!!
                    val imageBitmap = BitmapFactory.decodeStream(inputStream)
                    binding.washingMachineImage.setImageBitmap(imageBitmap)
                    washingMachineImage = washerMainActivity.convertToBase64(imageBitmap)!!
                }
                2 -> {
                    binding.uploadExtraImageLayout2.visibility = View.GONE
                    val inputStream: InputStream = requireContext().contentResolver.openInputStream(data!!.data!!)!!
                    val imageBitmap = BitmapFactory.decodeStream(inputStream)
                    binding.extraImage2.setImageBitmap(imageBitmap)
                    dryerImage = washerMainActivity.convertToBase64(imageBitmap)!!
                }
                3 -> {
                    binding.uploadExtraImageLayout.visibility = View.GONE
                    val inputStream: InputStream = requireContext().contentResolver.openInputStream(data!!.data!!)!!
                    val imageBitmap = BitmapFactory.decodeStream(inputStream)
                    binding.extraImage.setImageBitmap(imageBitmap)
                    extraImage = washerMainActivity.convertToBase64(imageBitmap)!!
                }
            }
        }else if (requestCode == REQUEST_CODE && resultCode == Activity.RESULT_OK){
            dispatchTakePictureIntent()
        }
    }

}