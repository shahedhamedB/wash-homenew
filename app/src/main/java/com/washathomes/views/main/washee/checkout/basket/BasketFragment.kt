package com.washathomes.views.main.washee.checkout.basket

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.washathomes.apputils.appdefs.AppDefs
import com.washathomes.apputils.appdefs.Urls
import com.washathomes.apputils.remote.RetrofitAPIs
import com.washathomes.apputils.modules.*
import com.washathomes.R
import com.washathomes.views.main.washee.checkout.basket.adapters.CartItemsAdapter
import com.washathomes.views.main.washee.WasheeMainActivity
import com.washathomes.databinding.FragmentBasketBinding
import dagger.hilt.android.AndroidEntryPoint
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.*
@AndroidEntryPoint
class BasketFragment : Fragment() {

    lateinit var binding: FragmentBasketBinding
    lateinit var navController: NavController
    lateinit var washeeMainActivity: WasheeMainActivity
    lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private val LOCATION_CODE = 100
    lateinit var cart: CartData
    var latitude = ""
    var longitude = ""
    var postalCode = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val layout = inflater.inflate(R.layout.fragment_basket, container, false)
        binding = FragmentBasketBinding.inflate(layoutInflater)
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
        getCurrentLocation()
        getCart()
    }

    private fun initViews(view: View){
        navController = Navigation.findNavController(view)
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(washeeMainActivity)
    }

    private fun onClick(){
        binding.emptyActionButton.setOnClickListener { navController.popBackStack() }
        binding.basketAddItemsLayout.setOnClickListener { navController.popBackStack() }
        binding.toolbarBackIcon.setOnClickListener { navController.popBackStack() }
        binding.basketDeliveryButton.setOnClickListener { navController.navigate(BasketFragmentDirections.actionBasketFragmentToDeliveryInfoFragment()) }
    }

    private fun getCart(){
        binding.progressBar.visibility = View.VISIBLE
        val cartObj = CartObj(latitude, longitude, postalCode)
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
        val getCartCall: Call<Cart> =
            retrofit.create(RetrofitAPIs::class.java).getCart(cartObj)
        getCartCall.enqueue(object : Callback<Cart> {
            override fun onResponse(call: Call<Cart>, response: Response<Cart>) {
                binding.progressBar.visibility = View.GONE
                if (response.isSuccessful){
                    cart = response.body()!!.results
                    AppDefs.cartData = cart
                    binding.basketTotal.text = ""+cart.sub_total
                    if (cart.CartItem.size == 0){
                        binding.basketLayout.visibility = View.GONE
                        binding.emptyLayout.visibility = View.VISIBLE
                    }else{
                        binding.basketLayout.visibility = View.VISIBLE
                        binding.emptyLayout.visibility = View.GONE
                    }
                    setCartItemsRV()
                }else{
                    val gson = Gson()
                    val type = object : TypeToken<ErrorResponse>() {}.type //ErrorResponse is the data class that matches the error response
                    val errorResponse = gson.fromJson<ErrorResponse>(response.errorBody()!!.charStream(), type) // errorResponse is an instance of ErrorResponse that will contain details about the error
                    if (errorResponse.status.massage.toString() == "No Data"){
                        binding.basketLayout.visibility = View.GONE
                        binding.emptyLayout.visibility = View.VISIBLE
                    }else {
                        Toast.makeText(
                            washeeMainActivity,
                            errorResponse.status.massage.toString(),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }

            override fun onFailure(call: Call<Cart>, t: Throwable) {
                binding.progressBar.visibility = View.GONE
                Toast.makeText(washeeMainActivity, resources.getString(R.string.internet_connection), Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun setCartItemsRV(){
        val adapter = CartItemsAdapter(this, cart.CartItem)
        binding.basketRV.adapter = adapter
        binding.basketRV.layoutManager = LinearLayoutManager(washeeMainActivity)
    }

    fun updateQuantity(itemId: String, qnt: String){
        binding.progressBar.visibility = View.VISIBLE
        val qntObj = UpdateQuantityObj(cart.id, itemId, qnt, latitude, longitude, postalCode)
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
        val updateQuantityCall: Call<BooleanResponse> =
            retrofit.create(RetrofitAPIs::class.java).updateQuantity(qntObj)
        updateQuantityCall.enqueue(object : Callback<BooleanResponse> {
            override fun onResponse(call: Call<BooleanResponse>, response: Response<BooleanResponse>) {
                binding.progressBar.visibility = View.GONE
                if (response.isSuccessful){
                    getCart()
                }else{
                    val gson = Gson()
                    val type = object : TypeToken<ErrorResponse>() {}.type //ErrorResponse is the data class that matches the error response
                    val errorResponse = gson.fromJson<ErrorResponse>(response.errorBody()!!.charStream(), type) // errorResponse is an instance of ErrorResponse that will contain details about the error
                    Toast.makeText(washeeMainActivity, errorResponse.status.massage.toString(), Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<BooleanResponse>, t: Throwable) {
                binding.progressBar.visibility = View.GONE
                Toast.makeText(washeeMainActivity, resources.getString(R.string.internet_connection), Toast.LENGTH_SHORT).show()
            }
        })
    }

    fun deleteCartItemDialog(itemId: String){
        val msgDialog = AlertDialog.Builder(washeeMainActivity)
        msgDialog.setTitle(washeeMainActivity.resources.getString(R.string.delete_item))
        msgDialog.setMessage(washeeMainActivity.resources.getString(R.string.delete_item_desc))
        msgDialog.setPositiveButton(
            washeeMainActivity.resources.getString(R.string.yes)
        ) { dialogInterface, i ->
            deleteCartItem(itemId)
        }
        msgDialog.setNegativeButton(
            resources.getString(R.string.cancel)
        ) { dialogInterface: DialogInterface, i: Int -> dialogInterface.dismiss() }
        msgDialog.show()
    }

    private fun deleteCartItem(itemId: String){
        binding.progressBar.visibility = View.VISIBLE
        val itemObj = DeleteItemObj(cart.id, itemId)
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
        val deleteItemCall: Call<BooleanResponse> =
            retrofit.create(RetrofitAPIs::class.java).deleteCartItem(itemObj)
        deleteItemCall.enqueue(object : Callback<BooleanResponse> {
            override fun onResponse(call: Call<BooleanResponse>, response: Response<BooleanResponse>) {
                binding.progressBar.visibility = View.GONE
                if (response.isSuccessful){
                    getCart()
                }else{
                    val gson = Gson()
                    val type = object : TypeToken<ErrorResponse>() {}.type //ErrorResponse is the data class that matches the error response
                    val errorResponse = gson.fromJson<ErrorResponse>(response.errorBody()!!.charStream(), type) // errorResponse is an instance of ErrorResponse that will contain details about the error
                    Toast.makeText(washeeMainActivity, errorResponse.status.massage.toString(), Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<BooleanResponse>, t: Throwable) {
                binding.progressBar.visibility = View.GONE
                Toast.makeText(washeeMainActivity, resources.getString(R.string.internet_connection), Toast.LENGTH_SHORT).show()
            }
        })
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
                washeeMainActivity,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                washeeMainActivity,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermission()
            return
        }
        fusedLocationProviderClient.lastLocation.addOnCompleteListener(washeeMainActivity){ task ->
            val location: Location? = task.result
            if (location != null){
                latitude = ""+location.latitude
                longitude = ""+location.longitude
                getAddress(location.latitude, location.longitude)
            }else{
                Toast.makeText(washeeMainActivity, "Null", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun getAddress(latitude: Double, longitude: Double){
        val geocoder: Geocoder
        val addresses: List<Address>
        geocoder = Geocoder(washeeMainActivity, Locale.getDefault())

        addresses = geocoder.getFromLocation(
            latitude,
            longitude,
            1
        ) // Here 1 represent max location result to returned, by documents it recommended 1 to 5


//        address = addresses[0].getAddressLine(0) // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
        if (addresses[0].postalCode != null){
            postalCode = addresses[0].postalCode
        }
    }

    private fun requestPermission(){
        ActivityCompat.requestPermissions(
            washeeMainActivity, arrayOf(
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
}