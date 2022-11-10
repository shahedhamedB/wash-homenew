package com.washathomes.views.main.courier.more.settings.identification

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
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.bumptech.glide.Glide
import com.washathomes.apputils.appdefs.AppDefs
import com.washathomes.apputils.appdefs.Urls
import com.washathomes.apputils.modules.UpdateUser
import com.washathomes.apputils.modules.UserData
import com.washathomes.apputils.remote.RetrofitAPIs
import com.washathomes.R
import com.washathomes.views.main.courier.CourierMainActivity
import com.washathomes.views.registration.washerregistration.WasherSignUpFragmentDirections
import com.washathomes.databinding.FragmentCourierIdentificationBinding
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
class CourierIdentificationFragment : Fragment() {

    lateinit var binding: FragmentCourierIdentificationBinding
    lateinit var courierMainActivity: CourierMainActivity
    lateinit var navController: NavController
    private val REQUEST_IMAGE_GALLERY = 101
    private val REQUEST_IMAGE_CAPTURE = 111
    private val LOCATION_CODE = 100
    private val REQUEST_CODE = 110
    var idImage = ""
    
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val layout = inflater.inflate(R.layout.fragment_courier_identification, container, false)
        binding = FragmentCourierIdentificationBinding.inflate(layoutInflater)
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
        Glide.with(courierMainActivity).load(AppDefs.user.results!!.id_image).into(binding.washerUploadIdentificationImage)
    }

    private fun onClick(){
        binding.toolbarBackIcon.setOnClickListener { navController.popBackStack() }
        binding.uploadID.setOnClickListener { imagePickerPopUp() }
        binding.washerUploadIdentificationUpload.setOnClickListener { updateUser() }
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

    private fun updateUser(){
        val userParams = UpdateUser(AppDefs.user.results!!.name, AppDefs.user.results!!.phone, AppDefs.user.results!!.email, AppDefs.user.results!!.gender, AppDefs.user.results!!.birthdate, AppDefs.user.results!!.latitude, AppDefs.user.results!!.longitude, AppDefs.user.results!!.address, AppDefs.user.results!!.zip_code, "3", AppDefs.user.results!!.image, idImage)
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
                navController.navigate(WasherSignUpFragmentDirections.actionWasherSignUpFragmentToWasherDrivingDataFragment())
            }

            override fun onFailure(call: Call<UserData>, t: Throwable) {
                TODO("Not yet implemented")
            }

        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
            binding.uploadIDLayout.visibility = View.GONE
            val imageBitmap = data?.extras?.get("data") as Bitmap
            binding.washerUploadIdentificationImage.setImageBitmap(imageBitmap)
            idImage = courierMainActivity.convertToBase64(imageBitmap)!!
        }else if (requestCode == REQUEST_IMAGE_GALLERY && resultCode == Activity.RESULT_OK){
            binding.uploadIDLayout.visibility = View.GONE
            val inputStream: InputStream = requireContext().contentResolver.openInputStream(data!!.data!!)!!
            val imageBitmap = BitmapFactory.decodeStream(inputStream)
            binding.washerUploadIdentificationImage.setImageBitmap(imageBitmap)
            idImage = courierMainActivity.convertToBase64(imageBitmap)!!
        }else if (requestCode == REQUEST_CODE && resultCode == Activity.RESULT_OK){
            dispatchTakePictureIntent()
        }
    }

}