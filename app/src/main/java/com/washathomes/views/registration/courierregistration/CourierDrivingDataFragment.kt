package com.washathomes.views.registration.courierregistration

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
import android.widget.Toast
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.bumptech.glide.Glide
import com.google.gson.Gson
import com.washathomes.apputils.appdefs.AppDefs
import com.washathomes.apputils.appdefs.Urls
import com.washathomes.apputils.helpers.FirebaseHelper
import com.washathomes.apputils.remote.RetrofitAPIs
import com.washathomes.apputils.modules.UpdateDrivingData
import com.washathomes.apputils.modules.UserData
import com.washathomes.R
import com.washathomes.views.main.courier.CourierMainActivity
import com.washathomes.databinding.FragmentCourierDrivingDataBinding
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
class CourierDrivingDataFragment : Fragment() {

    lateinit var binding: FragmentCourierDrivingDataBinding
    lateinit var courierRegistrationActivity: CourierRegistrationActivity
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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val layout = inflater.inflate(R.layout.fragment_courier_driving_data, container, false)
        binding = FragmentCourierDrivingDataBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews(view)
        onClick()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is CourierRegistrationActivity) {
            courierRegistrationActivity = context
        }
    }

    private fun initViews(view: View){
        navController = Navigation.findNavController(view)
    }

    private fun onClick(){
        binding.availabilitySwitch.setOnClickListener {
            if (drivingAvailable == "1"){
                vehicleType = ""
                drivingAvailable = "0"
                Glide.with(courierRegistrationActivity).load(R.drawable.switch_off).into(binding.availabilitySwitch)
            }else{
                drivingAvailable = "1"
                Glide.with(courierRegistrationActivity).load(R.drawable.switch_on).into(binding.availabilitySwitch)
            }
        }
        binding.car.setOnClickListener {
            binding.carLayout.setBackgroundColor(resources.getColor(R.color.blue))
            Glide.with(courierRegistrationActivity).load(R.drawable.white_car).into(binding.carIcon)
            binding.bikeLayout.setBackgroundColor(resources.getColor(R.color.light_grey))
            Glide.with(courierRegistrationActivity).load(R.drawable.grey_bike).into(binding.bikeIcon)
            binding.bicycleLayout.setBackgroundColor(resources.getColor(R.color.light_grey))
            Glide.with(courierRegistrationActivity).load(R.drawable.grey_bicycle).into(binding.bicycleIcon)
            vehicleType = "1"
            binding.uploadVehicleID.isEnabled = true
            binding.uploadDrivingLicense.isEnabled = true
            binding.uploadVehicleImage.isEnabled = true
        }
        binding.bike.setOnClickListener {
            binding.bikeLayout.setBackgroundColor(resources.getColor(R.color.blue))
            Glide.with(courierRegistrationActivity).load(R.drawable.white_bike).into(binding.bikeIcon)
            binding.carLayout.setBackgroundColor(resources.getColor(R.color.light_grey))
            Glide.with(courierRegistrationActivity).load(R.drawable.grey_car).into(binding.carIcon)
            binding.bicycleLayout.setBackgroundColor(resources.getColor(R.color.light_grey))
            Glide.with(courierRegistrationActivity).load(R.drawable.grey_bicycle).into(binding.bicycleIcon)
            vehicleType = "2"
            binding.uploadVehicleID.isEnabled = true
            binding.uploadDrivingLicense.isEnabled = true
            binding.uploadVehicleImage.isEnabled = true
        }
        binding.bycicle.setOnClickListener {
            binding.bicycleLayout.setBackgroundColor(resources.getColor(R.color.blue))
            Glide.with(courierRegistrationActivity).load(R.drawable.white_bycicle).into(binding.bicycleIcon)
            binding.bikeLayout.setBackgroundColor(resources.getColor(R.color.light_grey))
            Glide.with(courierRegistrationActivity).load(R.drawable.grey_bike).into(binding.bikeIcon)
            binding.carLayout.setBackgroundColor(resources.getColor(R.color.light_grey))
            Glide.with(courierRegistrationActivity).load(R.drawable.grey_car).into(binding.carIcon)
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
        binding.nextBtn.setOnClickListener { checkValidation() }
    }

    private fun imagePickerPopUp() {
        val alertView: View = LayoutInflater.from(context).inflate(R.layout.image_picker_dialog, null)
        val alertBuilder = AlertDialog.Builder(context).setView(alertView).show()
        alertBuilder.show()
        alertBuilder.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val camera: LinearLayoutCompat = alertView.findViewById(R.id.camera)
        val gallery: LinearLayoutCompat = alertView.findViewById(R.id.gallery)

        camera.setOnClickListener {
            if (ContextCompat.checkSelfPermission(courierRegistrationActivity, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_DENIED){
                ActivityCompat.requestPermissions(
                    courierRegistrationActivity, arrayOf(
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
        if (drivingAvailable == "1"){
            if (vehicleType == "3"){
                updateUser()
            }else{
                if (vehicleIdImage.isEmpty() || drivingLicenseImage.isEmpty() || vehicleImage.isEmpty()){
                    Toast.makeText(courierRegistrationActivity, resources.getString(R.string.upload_images), Toast.LENGTH_SHORT).show()
                }else{
                    updateUser()
                }
            }
        }else{
            Toast.makeText(courierRegistrationActivity, resources.getString(R.string.enable_availability), Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateUser(){
        val userParams = UpdateDrivingData(drivingAvailable, vehicleType, miles, vehicleIdImage, drivingLicenseImage, vehicleImage)
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
        FirebaseHelper.sendUserToDatabase(AppDefs.user.results!!.language_code!!)
        val sharedPreferences =
            courierRegistrationActivity.getSharedPreferences(AppDefs.SHARED_PREF_KEY, Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString(AppDefs.ID_KEY, AppDefs.user.results!!.id)

        val gson = Gson()
        val json = gson.toJson(AppDefs.user)
        editor.putString(AppDefs.USER_KEY, json)
        editor.putString(AppDefs.TYPE_KEY, "3")
        editor.apply()
        val registrationIntent = Intent(courierRegistrationActivity, CourierMainActivity::class.java)
        startActivity(registrationIntent)
        courierRegistrationActivity.finish()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
            when (imageType) {
                1 -> {
                    binding.uploadVehicleIDLayout.visibility = View.GONE
                    val imageBitmap = data?.extras?.get("data") as Bitmap
                    binding.vehicleIdImage.setImageBitmap(imageBitmap)
                    vehicleIdImage = courierRegistrationActivity.convertToBase64(imageBitmap)!!
                }
                2 -> {
                    binding.uploadDrivingLicenseLayout.visibility = View.GONE
                    val imageBitmap = data?.extras?.get("data") as Bitmap
                    binding.drivingLicenseImage.setImageBitmap(imageBitmap)
                    drivingLicenseImage = courierRegistrationActivity.convertToBase64(imageBitmap)!!
                }
                3 -> {
                    binding.uploadVehicleImageLayout.visibility = View.GONE
                    val imageBitmap = data?.extras?.get("data") as Bitmap
                    binding.vehicleImageImage.setImageBitmap(imageBitmap)
                    vehicleImage = courierRegistrationActivity.convertToBase64(imageBitmap)!!
                }
            }
        }else if (requestCode == REQUEST_IMAGE_GALLERY && resultCode == Activity.RESULT_OK){
            when (imageType) {
                1 -> {
                    binding.uploadVehicleIDLayout.visibility = View.GONE
                    val inputStream: InputStream = requireContext().contentResolver.openInputStream(data!!.data!!)!!
                    val imageBitmap = BitmapFactory.decodeStream(inputStream)
                    binding.vehicleIdImage.setImageBitmap(imageBitmap)
                    vehicleIdImage = courierRegistrationActivity.convertToBase64(imageBitmap)!!
                }
                2 -> {
                    binding.uploadDrivingLicenseLayout.visibility = View.GONE
                    val inputStream: InputStream = requireContext().contentResolver.openInputStream(data!!.data!!)!!
                    val imageBitmap = BitmapFactory.decodeStream(inputStream)
                    binding.drivingLicenseImage.setImageBitmap(imageBitmap)
                    drivingLicenseImage = courierRegistrationActivity.convertToBase64(imageBitmap)!!
                }
                3 -> {
                    binding.uploadVehicleImageLayout.visibility = View.GONE
                    val inputStream: InputStream = requireContext().contentResolver.openInputStream(data!!.data!!)!!
                    val imageBitmap = BitmapFactory.decodeStream(inputStream)
                    binding.vehicleImageImage.setImageBitmap(imageBitmap)
                    vehicleImage = courierRegistrationActivity.convertToBase64(imageBitmap)!!
                }
            }
        }else if (requestCode == REQUEST_CODE && resultCode == Activity.RESULT_OK){
            dispatchTakePictureIntent()
        }
    }

}