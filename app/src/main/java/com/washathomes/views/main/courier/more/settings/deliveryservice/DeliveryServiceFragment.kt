package com.washathomes.views.main.courier.more.settings.deliveryservice

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
import android.widget.SeekBar
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.bumptech.glide.Glide
import com.google.gson.Gson
import com.washathomes.apputils.appdefs.AppDefs
import com.washathomes.apputils.appdefs.Urls
import com.washathomes.apputils.modules.UpdateDrivingData
import com.washathomes.apputils.modules.UserData
import com.washathomes.apputils.remote.RetrofitAPIs
import com.washathomes.R
import com.washathomes.apputils.modules.UserDocs
import com.washathomes.apputils.modules.UserDocuments
import com.washathomes.views.main.courier.CourierMainActivity
import com.washathomes.databinding.FragmentDeliveryServiceBinding
import dagger.hilt.android.AndroidEntryPoint
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.InputStream
import kotlin.math.roundToInt

@AndroidEntryPoint
class DeliveryServiceFragment : Fragment() {

    lateinit var binding: FragmentDeliveryServiceBinding
    lateinit var courierMainActivity: CourierMainActivity
    lateinit var navController: NavController
    private val REQUEST_IMAGE_GALLERY = 101
    private val REQUEST_IMAGE_CAPTURE = 111
    private val REQUEST_CODE = 110
    var drivingAvailable = "1"
    var vehicleType = "1"
    var imageType = 1
    var vehicleIdImage = ""
    var drivingLicenseImage = ""
    var vehicleImage = ""
    var miles = ""
    lateinit var userDocuments: UserDocuments

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val layout = inflater.inflate(R.layout.fragment_delivery_service, container, false)
        binding = FragmentDeliveryServiceBinding.inflate(layoutInflater)
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
        setData()
    }

    private fun initViews(view: View){
        navController = Navigation.findNavController(view)
    }

    private fun onClick(){
        binding.toolbarBackIcon.setOnClickListener { navController.popBackStack() }
        binding.car.setOnClickListener {
            binding.carLayout.setBackgroundColor(resources.getColor(R.color.blue))
            Glide.with(courierMainActivity).load(R.drawable.white_car).into(binding.carIcon)
            binding.bikeLayout.setBackgroundColor(resources.getColor(R.color.light_grey))
            Glide.with(courierMainActivity).load(R.drawable.grey_bike).into(binding.bikeIcon)
            binding.bicycleLayout.setBackgroundColor(resources.getColor(R.color.light_grey))
            Glide.with(courierMainActivity).load(R.drawable.grey_bicycle).into(binding.bicycleIcon)
            vehicleType = "1"
            binding.uploadVehicleID.isEnabled = true
            binding.uploadDrivingLicense.isEnabled = true
            binding.uploadVehicleImage.isEnabled = true
        }
        binding.bike.setOnClickListener {
            binding.bikeLayout.setBackgroundColor(resources.getColor(R.color.blue))
            Glide.with(courierMainActivity).load(R.drawable.white_bike).into(binding.bikeIcon)
            binding.carLayout.setBackgroundColor(resources.getColor(R.color.light_grey))
            Glide.with(courierMainActivity).load(R.drawable.grey_car).into(binding.carIcon)
            binding.bicycleLayout.setBackgroundColor(resources.getColor(R.color.light_grey))
            Glide.with(courierMainActivity).load(R.drawable.grey_bicycle).into(binding.bicycleIcon)
            vehicleType = "2"
            binding.uploadVehicleID.isEnabled = true
            binding.uploadDrivingLicense.isEnabled = true
            binding.uploadVehicleImage.isEnabled = true
        }
        binding.bycicle.setOnClickListener {
            binding.bicycleLayout.setBackgroundColor(resources.getColor(R.color.blue))
            Glide.with(courierMainActivity).load(R.drawable.white_bycicle).into(binding.bicycleIcon)
            binding.bikeLayout.setBackgroundColor(resources.getColor(R.color.light_grey))
            Glide.with(courierMainActivity).load(R.drawable.grey_bike).into(binding.bikeIcon)
            binding.carLayout.setBackgroundColor(resources.getColor(R.color.light_grey))
            Glide.with(courierMainActivity).load(R.drawable.grey_car).into(binding.carIcon)
            vehicleType = "3"
            binding.uploadVehicleIDLayout.visibility = View.VISIBLE
            binding.uploadDrivingLicenseLayout.visibility= View.VISIBLE
            binding.uploadVehicleImageLayout.visibility= View.VISIBLE
            vehicleIdImage = ""
            drivingLicenseImage = ""
            vehicleImage = ""
            binding.uploadVehicleID.isEnabled = false
            binding.uploadDrivingLicense.isEnabled = false
            binding.uploadVehicleImage.isEnabled = false
        }
        binding.milesSB.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                val p = progress/20.0
                miles = ""+p
                binding.miles.text = ""+p+" "+resources.getString(R.string.miles)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
                // you can probably leave this empty
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                // you can probably leave this empty
            }
        })
        binding.uploadVehicleID.setOnClickListener {
            imageType = 1
            imagePickerPopUp()
        }
        binding.uploadDrivingLicense.setOnClickListener {
            imageType = 2
            imagePickerPopUp()
        }
        binding.uploadVehicleImage.setOnClickListener {
            imageType = 3
            imagePickerPopUp()
        }
        binding.nextBtn.setOnClickListener { updateUser() }
    }

    private fun imagePickerPopUp() {
        val alertView: View = LayoutInflater.from(context).inflate(R.layout.image_picker_dialog, null)
        val alertBuilder = AlertDialog.Builder(context).setView(alertView).show()
        alertBuilder.show()
        alertBuilder.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val camera: LinearLayoutCompat = alertView.findViewById(R.id.camera)
        val gallery: LinearLayoutCompat = alertView.findViewById(R.id.gallery)

        camera.setOnClickListener {
            if (ContextCompat.checkSelfPermission(courierMainActivity, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_DENIED){
                ActivityCompat.requestPermissions(
                    courierMainActivity, arrayOf(
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

    private fun setData(){
        when (AppDefs.user.results!!.dreiver_type) {
            "1" -> {
                binding.carLayout.setBackgroundColor(resources.getColor(R.color.blue))
                Glide.with(courierMainActivity).load(R.drawable.white_car).into(binding.carIcon)
                binding.bikeLayout.setBackgroundColor(resources.getColor(R.color.light_grey))
                Glide.with(courierMainActivity).load(R.drawable.grey_bike).into(binding.bikeIcon)
                binding.bicycleLayout.setBackgroundColor(resources.getColor(R.color.light_grey))
                Glide.with(courierMainActivity).load(R.drawable.grey_bicycle).into(binding.bicycleIcon)
                vehicleType = "1"
            }
            "2" -> {
                binding.bikeLayout.setBackgroundColor(resources.getColor(R.color.blue))
                Glide.with(courierMainActivity).load(R.drawable.white_bike).into(binding.bikeIcon)
                binding.carLayout.setBackgroundColor(resources.getColor(R.color.light_grey))
                Glide.with(courierMainActivity).load(R.drawable.grey_car).into(binding.carIcon)
                binding.bicycleLayout.setBackgroundColor(resources.getColor(R.color.light_grey))
                Glide.with(courierMainActivity).load(R.drawable.grey_bicycle).into(binding.bicycleIcon)
                vehicleType = "2"
            }
            "3" -> {
                binding.bicycleLayout.setBackgroundColor(resources.getColor(R.color.blue))
                Glide.with(courierMainActivity).load(R.drawable.white_bycicle).into(binding.bicycleIcon)
                binding.bikeLayout.setBackgroundColor(resources.getColor(R.color.light_grey))
                Glide.with(courierMainActivity).load(R.drawable.grey_bike).into(binding.bikeIcon)
                binding.carLayout.setBackgroundColor(resources.getColor(R.color.light_grey))
                Glide.with(courierMainActivity).load(R.drawable.grey_car).into(binding.carIcon)
                vehicleType = "3"
            }
        }
        var milesSelected = 5
        if (!AppDefs.user.results!!.dreiver_miles.isNullOrEmpty()) {
            val sub =  AppDefs.user.results!!.dreiver_miles!!
            milesSelected = sub.toFloat().roundToInt()
        }
        binding.milesSB.progress = milesSelected*20
        binding.miles.text = AppDefs.user.results!!.dreiver_miles!!+" "+resources.getString(R.string.miles)
        getUserDocuments()
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
                    userDocuments = response.body()!!.results
                    if (userDocuments.vehicle_image_id.isNotEmpty()){
                        binding.uploadVehicleIDLayout.visibility = View.GONE
                        Glide.with(courierMainActivity).load(userDocuments.vehicle_image_id).into(binding.vehicleIdImage)
                    }
                    if (userDocuments.vehicle_image.isNotEmpty()){
                        binding.uploadVehicleImageLayout.visibility = View.GONE
                        Glide.with(courierMainActivity).load(userDocuments.vehicle_image).into(binding.vehicleImageImage)
                    }
                    if (userDocuments.vehicle_licenses.isNotEmpty()){
                        binding.uploadDrivingLicenseLayout.visibility = View.GONE
                        Glide.with(courierMainActivity).load(userDocuments.vehicle_licenses).into(binding.drivingLicenseImage)
                    }
                }
            }

            override fun onFailure(call: Call<UserDocs>, t: Throwable) {
                TODO("Not yet implemented")
            }

        })
    }

    private fun updateUser(){
        val userParams = UpdateDrivingData(AppDefs.user.results!!.dreiver_available, vehicleType, miles, vehicleIdImage, drivingLicenseImage, vehicleImage)
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
        val updateDrivingDataCall: Call<UserData> =
            retrofit.create(RetrofitAPIs::class.java).updateDrivingData(userParams)
        updateDrivingDataCall.enqueue(object : Callback<UserData> {
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
            courierMainActivity.getSharedPreferences(AppDefs.SHARED_PREF_KEY, Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString(AppDefs.ID_KEY, AppDefs.user.results!!.id)

        val gson = Gson()
        val json = gson.toJson(AppDefs.user)
        editor.putString(AppDefs.USER_KEY, json)
        editor.putString(AppDefs.TYPE_KEY, "3")
        editor.apply()
        navController.popBackStack()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
            when (imageType) {
                1 -> {
                    binding.uploadVehicleIDLayout.visibility = View.GONE
                    val imageBitmap = data?.extras?.get("data") as Bitmap
                    binding.vehicleIdImage.setImageBitmap(imageBitmap)
                    vehicleIdImage = courierMainActivity.convertToBase64(imageBitmap)!!
                }
                2 -> {
                    binding.uploadDrivingLicenseLayout.visibility = View.GONE
                    val imageBitmap = data?.extras?.get("data") as Bitmap
                    binding.drivingLicenseImage.setImageBitmap(imageBitmap)
                    drivingLicenseImage = courierMainActivity.convertToBase64(imageBitmap)!!
                }
                3 -> {
                    binding.uploadVehicleImageLayout.visibility = View.GONE
                    val imageBitmap = data?.extras?.get("data") as Bitmap
                    binding.vehicleImageImage.setImageBitmap(imageBitmap)
                    vehicleImage = courierMainActivity.convertToBase64(imageBitmap)!!
                }
            }
        }else if (requestCode == REQUEST_IMAGE_GALLERY && resultCode == Activity.RESULT_OK){
            when (imageType) {
                1 -> {
                    binding.uploadVehicleIDLayout.visibility = View.GONE
                    val inputStream: InputStream = requireContext().contentResolver.openInputStream(data!!.data!!)!!
                    val imageBitmap = BitmapFactory.decodeStream(inputStream)
                    binding.vehicleIdImage.setImageBitmap(imageBitmap)
                    vehicleIdImage = courierMainActivity.convertToBase64(imageBitmap)!!
                }
                2 -> {
                    binding.uploadDrivingLicenseLayout.visibility = View.GONE
                    val inputStream: InputStream = requireContext().contentResolver.openInputStream(data!!.data!!)!!
                    val imageBitmap = BitmapFactory.decodeStream(inputStream)
                    binding.drivingLicenseImage.setImageBitmap(imageBitmap)
                    drivingLicenseImage = courierMainActivity.convertToBase64(imageBitmap)!!
                }
                3 -> {
                    binding.uploadVehicleImageLayout.visibility = View.GONE
                    val inputStream: InputStream = requireContext().contentResolver.openInputStream(data!!.data!!)!!
                    val imageBitmap = BitmapFactory.decodeStream(inputStream)
                    binding.vehicleImageImage.setImageBitmap(imageBitmap)
                    vehicleImage = courierMainActivity.convertToBase64(imageBitmap)!!
                }
            }
        }else if (requestCode == REQUEST_CODE && resultCode == Activity.RESULT_OK){
            dispatchTakePictureIntent()
        }
    }


}