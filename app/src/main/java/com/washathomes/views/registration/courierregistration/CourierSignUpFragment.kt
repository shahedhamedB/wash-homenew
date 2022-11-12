package com.washathomes.views.registration.courierregistration

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.bumptech.glide.Glide
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.washathomes.apputils.appdefs.AppDefs
import com.washathomes.apputils.appdefs.Urls
import com.washathomes.apputils.remote.RetrofitAPIs
import com.washathomes.apputils.modules.UpdateUser
import com.washathomes.apputils.modules.UserData
import com.washathomes.R
import com.washathomes.databinding.FragmentCourierSignUpBinding
import dagger.hilt.android.AndroidEntryPoint
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.InputStream
import java.util.*
@AndroidEntryPoint
class CourierSignUpFragment : Fragment() {

    lateinit var binding: FragmentCourierSignUpBinding
    lateinit var courierRegistrationActivity: CourierRegistrationActivity
    lateinit var navController: NavController
    private val REQUEST_IMAGE_GALLERY = 101
    private val REQUEST_IMAGE_CAPTURE = 111
    private val LOCATION_CODE = 100
    private val REQUEST_CODE = 120
    lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    var imageType = 1
    var latitude = ""
    var longitude = ""
    var address = ""
    var postalCode = ""
    var profileImage = ""
    var idImage = ""
    var fullName = ""
    var email = ""
    var gender = ""
    var birthdate = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val layout = inflater.inflate(R.layout.fragment_courier_sign_up, container, false)
        binding = FragmentCourierSignUpBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews(view)
        onClick()
        setSpinner()
        getCurrentLocation()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is CourierRegistrationActivity) {
            courierRegistrationActivity = context
        }
    }

    private fun initViews(view: View){
        navController = Navigation.findNavController(view)
        binding.phoneNumber.text = AppDefs.user.results!!.phone
        binding.fullNameEdt.setText(AppDefs.user.results!!.name)
        binding.emailAddressEdt.setText(AppDefs.user.results!!.email)
        binding.birthdate.text = AppDefs.user.results!!.birthdate
        if (AppDefs.user.results!!.image!!.isNotEmpty()){
            Glide.with(courierRegistrationActivity).load(AppDefs.user.results!!.image).into(binding.profileImage)
        }
        if (AppDefs.user.results!!.id_image!!.isNotEmpty()){
            binding.uploadIDLayout.visibility = View.GONE
            idImage = AppDefs.user.results!!.id_image!!
            Glide.with(courierRegistrationActivity).load(AppDefs.user.results!!.id_image).into(binding.idImage)
        }
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(courierRegistrationActivity)
    }

    private fun onClick(){
        binding.chooseImage.setOnClickListener {
            imageType = 1
            imagePickerPopUp()
        }
        binding.uploadID.setOnClickListener {
            imageType = 2
            imagePickerPopUp()
        }
        binding.birthdate.setOnClickListener { openDatePicker() }
        binding.gendersSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onNothingSelected(parent: AdapterView<*>?) {

            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                gender = ""+(position+1)
            }

        }
        binding.doneBtn.setOnClickListener { checkValidation() }
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

    private fun openDatePicker(){
        val c = Calendar.getInstance()
        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH)
        val day = c.get(Calendar.DAY_OF_MONTH)


        val dpd = DatePickerDialog(courierRegistrationActivity, { view, year, monthOfYear, dayOfMonth ->
            var m :String = "" + (monthOfYear+1)
            var d :String = "" + (dayOfMonth)
            if (m.length == 1){
                m = "0$m"
            }
            if (d.length == 1){
                d = "0$d"
            }
            birthdate = "$year/$m/$d"
            binding.birthdate.text = birthdate

        }, year, month, day)

        dpd.show()
    }

    private fun setSpinner() {
        val items: ArrayList<String?> = ArrayList()
        items.add(resources.getString(R.string.male))
        items.add(resources.getString(R.string.female))
        val genderAdapter: ArrayAdapter<*> =
            ArrayAdapter<String?>(courierRegistrationActivity, R.layout.gender_spinner_item, items)
        genderAdapter.setDropDownViewResource(R.layout.gender_spinner_item)
        binding.gendersSpinner.adapter = genderAdapter
        if (AppDefs.user.results!!.gender == "2"){
            binding.gendersSpinner.setSelection(1)
        }else{
            binding.gendersSpinner.setSelection(0)
        }
    }

    private fun getCurrentLocation(){
//        if (checkPermissions()){
//            if (isLocationEnabled()){
//                if (ActivityCompat.checkSelfPermission(
//                        washeeRegistrationActivity,
//                        Manifest.permission.ACCESS_FINE_LOCATION
//                    ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
//                        washeeRegistrationActivity,
//                        Manifest.permission.ACCESS_COARSE_LOCATION
//                    ) != PackageManager.PERMISSION_GRANTED
//                ) {
//                    requestPermission()
//                    return
//                }
//                fusedLocationProviderClient.lastLocation.addOnCompleteListener(washeeRegistrationActivity){ task ->
//                    val location: Location? = task.result
//                    if (location != null){
//                        latitude = ""+location.latitude
//                        longitude = ""+location.longitude
//                        getAddress(location.latitude, location.longitude)
//                    }else{
//                        Toast.makeText(washeeRegistrationActivity, "Null", Toast.LENGTH_LONG).show()
//                    }
//                }
//            }else{
//                Toast.makeText(washeeRegistrationActivity, resources.getString(R.string.turn_on_location), Toast.LENGTH_SHORT).show()
//                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
//                startActivity(intent)
//            }
//        }else{
//            requestPermission()
//        }
        if (ActivityCompat.checkSelfPermission(
                courierRegistrationActivity,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                courierRegistrationActivity,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermission()
            return
        }
        fusedLocationProviderClient.lastLocation.addOnCompleteListener(courierRegistrationActivity){ task ->
            val location: Location? = task.result
            if (location != null){
                latitude = ""+location.latitude
                longitude = ""+location.longitude
                getAddress(location.latitude, location.longitude)
            }else{
                Toast.makeText(courierRegistrationActivity, "Null", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun getAddress(latitude: Double, longitude: Double){
        val geocoder: Geocoder
        val addresses: List<Address>
        geocoder = Geocoder(courierRegistrationActivity, Locale.getDefault())

        addresses = geocoder.getFromLocation(
            latitude,
            longitude,
            1
        ) // Here 1 represent max location result to returned, by documents it recommended 1 to 5


        address = addresses[0].getAddressLine(0) // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
        if (addresses[0].postalCode != null){
            postalCode = addresses[0].postalCode
        }
//        postalCode = addresses[0].postalCode
//        Log.d("latitude", ""+latitude)
//        Log.d("longitude", ""+longitude)
//        Log.d("address", ""+address)
//        Log.d("postalCode", ""+postalCode)
//        Toast.makeText(washeeRegistrationActivity, ""+latitude+", "+longitude+", "+address, Toast.LENGTH_LONG).show()
    }

    private fun checkPermissions(): Boolean{
        if (ActivityCompat.checkSelfPermission(courierRegistrationActivity,
                Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(courierRegistrationActivity,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            return true
        }
        return false
    }

    private fun requestPermission(){
        ActivityCompat.requestPermissions(
            courierRegistrationActivity, arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION),
            LOCATION_CODE
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_CODE){
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                getCurrentLocation()
            }
        }
    }

    private fun isLocationEnabled():Boolean{
        val locationManager: LocationManager = courierRegistrationActivity.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER)
    }

    private fun checkValidation(){
        fullName = binding.fullNameEdt.text.toString()
        email = binding.emailAddressEdt.text.toString()
        if (fullName.isEmpty()){
            binding.fullNameEdt.error = resources.getString(R.string.fill_feild)
        }else if (email.isEmpty()){
            binding.emailAddressEdt.error = resources.getString(R.string.fill_feild)
        }else if (idImage.isEmpty()){
            Toast.makeText(courierRegistrationActivity, resources.getString(R.string.upload_images), Toast.LENGTH_SHORT).show()
        } else{
            updateUser()
        }
    }

    private fun updateUser(){
        val userParams = UpdateUser(fullName, AppDefs.user.results!!.phone, email, gender, birthdate, latitude, longitude, address, postalCode, "3", profileImage, idImage)
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
        val updateUserCall: Call<UserData> =
            retrofit.create(RetrofitAPIs::class.java).updateUser(userParams)
        updateUserCall.enqueue(object : Callback<UserData> {
            override fun onResponse(call: Call<UserData>, response: Response<UserData>) {
                AppDefs.user = response.body()!!
                binding.progressBar.visibility = View.GONE
                navController.navigate(CourierSignUpFragmentDirections.actionCourierSignUpFragmentToCourierDrivingDataFragment())
            }

            override fun onFailure(call: Call<UserData>, t: Throwable) {
                TODO("Not yet implemented")
            }

        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
            if (imageType == 1){
                val imageBitmap = data?.extras?.get("data") as Bitmap
                binding.profileImage.setImageBitmap(imageBitmap)
                profileImage = courierRegistrationActivity.convertToBase64(imageBitmap)!!
            }else{
                binding.uploadIDLayout.visibility = View.GONE
                val imageBitmap = data?.extras?.get("data") as Bitmap
                binding.idImage.setImageBitmap(imageBitmap)
                idImage = courierRegistrationActivity.convertToBase64(imageBitmap)!!
            }
        }else if (requestCode == REQUEST_IMAGE_GALLERY && resultCode == Activity.RESULT_OK){
            if (imageType == 1){
                val inputStream: InputStream = requireContext().contentResolver.openInputStream(data!!.data!!)!!
                val imageBitmap = BitmapFactory.decodeStream(inputStream)
                binding.profileImage.setImageBitmap(imageBitmap)
                profileImage = courierRegistrationActivity.convertToBase64(imageBitmap)!!
            }else{
                binding.uploadIDLayout.visibility = View.GONE
                val inputStream: InputStream = requireContext().contentResolver.openInputStream(data!!.data!!)!!
                val imageBitmap = BitmapFactory.decodeStream(inputStream)
                binding.idImage.setImageBitmap(imageBitmap)
                idImage = courierRegistrationActivity.convertToBase64(imageBitmap)!!
            }
        }else if (requestCode == REQUEST_CODE && resultCode == Activity.RESULT_OK){
            dispatchTakePictureIntent()
        }
    }

}