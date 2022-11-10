package com.washathomes.views.main.washee.home

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.washathomes.apputils.appdefs.AppDefs
import com.washathomes.apputils.appdefs.Urls
import com.washathomes.apputils.helpers.Helpers
import com.washathomes.apputils.modules.*
import com.washathomes.apputils.remote.RetrofitAPIs
import com.washathomes.R
import com.washathomes.views.main.washee.home.adapters.CategoriesAdapter
import com.washathomes.views.main.washee.home.adapters.CategoryItemsAdapter
import com.washathomes.views.main.washee.home.adapters.ImagesAdapter
import com.washathomes.views.main.washee.WasheeMainActivity
import com.washathomes.databinding.FragmentWasheeHomeBinding
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
class WasheeHomeFragment : Fragment() {

    lateinit var binding: FragmentWasheeHomeBinding
    lateinit var navController: NavController
    lateinit var washeeMainActivity: WasheeMainActivity
    var categories: ArrayList<Category> = ArrayList()
    var categoryItems: ArrayList<CategoryItem> = ArrayList()
    var filteredCategoryItems: ArrayList<CategoryItem> = ArrayList()
    var ads: ArrayList<Ad> = ArrayList()
    lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private val LOCATION_CODE = 100
    var latitude = ""
    var longitude = ""
    var postalCode = ""
    var selectedCategory = ""
    var isOptionsVisible = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val layout = inflater.inflate(R.layout.fragment_washee_home, container, false)
        binding = FragmentWasheeHomeBinding.inflate(layoutInflater)
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
        getCategories()
        getPrices()
    }

    private fun initViews(view: View){
        navController = Navigation.findNavController(view)
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(washeeMainActivity)
        if (AppDefs.user.results!!.image!!.isEmpty() || !AppDefs.user.results!!.image!!.startsWith("https://")){
            Glide.with(washeeMainActivity).load(R.drawable.ic_baseline_person_24).into(binding.homeUserProfileImage)
        }else{
            Glide.with(washeeMainActivity).load(AppDefs.user.results!!.image!!).into(binding.homeUserProfileImage)
        }
        binding.homeUserWelcome.text = resources.getString(R.string.hey_there)+" "+AppDefs.user.results!!.name
        binding.numberPicker.minValue = 1
        binding.numberPicker.maxValue = 20
    }

    private fun onClick(){
        binding.orderSummary.setOnClickListener { navController.navigate(WasheeHomeFragmentDirections.actionWasheeHomeFragmentToBasketFragment()) }
        binding.toolbarLayout.toolbarBasketIcon.setOnClickListener { navController.navigate(WasheeHomeFragmentDirections.actionWasheeHomeFragmentToBasketFragment()) }
        binding.optionsFB.setOnClickListener {
            if (isOptionsVisible){
                isOptionsVisible = false
                binding.optionsLayout.visibility = View.GONE
            }else{
                isOptionsVisible = true
                binding.optionsLayout.visibility = View.VISIBLE
            }
        }

        binding.chatWithUs.setOnClickListener {
            openWhatsApp(AppDefs.deliveryInfoPrices[11].price)
        }

        binding.callUs.setOnClickListener { callPhone() }
        binding.contactUs.setOnClickListener { openWebPageInBrowser() }

        binding.toolbarLayout.toolbarLeftIcon.setOnClickListener { navController.navigate(WasheeHomeFragmentDirections.actionNavigationHomeToWasheeNotificationsFragment()) }

        binding.homeSearchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener,
            android.widget.SearchView.OnQueryTextListener {

            override fun onQueryTextChange(qString: String): Boolean {
                filterCategoryItems(qString)
                return true
            }
            override fun onQueryTextSubmit(qString: String): Boolean {
                washeeMainActivity.hideKeyboard()
                filterCategoryItems(qString)
                return true
            }
        })
    }

    private fun getAds(){
        binding.progressBar.visibility = View.VISIBLE
        val locationObject = LocationObj(latitude, longitude, postalCode)
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
        val adsCall: Call<Ads> =
            retrofit.create(RetrofitAPIs::class.java).getAds(locationObject)
        adsCall.enqueue(object : Callback<Ads> {
            override fun onResponse(call: Call<Ads>, response: Response<Ads>) {
                binding.homeLayout.visibility = View.VISIBLE
                binding.progressBar.visibility = View.GONE
                if (response.isSuccessful){
                    ads = response.body()!!.results
                    initSlider()
                }else{
                    val gson = Gson()
                    val type = object : TypeToken<ErrorResponse>() {}.type //ErrorResponse is the data class that matches the error response
                    val errorResponse = gson.fromJson<ErrorResponse>(response.errorBody()!!.charStream(), type) // errorResponse is an instance of ErrorResponse that will contain details about the error
                    Toast.makeText(washeeMainActivity, errorResponse.status.massage.toString(), Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Ads>, t: Throwable) {
                binding.progressBar.visibility = View.GONE
                Toast.makeText(washeeMainActivity, resources.getString(R.string.internet_connection), Toast.LENGTH_SHORT).show()
            }

        })
    }

    private fun openWhatsApp(num: String) {
        val url = "https://api.whatsapp.com/send?phone="+num
        val i = Intent(Intent.ACTION_VIEW)
        i.data = Uri.parse(url)
        startActivity(i)
    }

    private fun callPhone(){
        val intent = Intent(Intent.ACTION_DIAL)
        intent.data = Uri.parse("tel:"+AppDefs.deliveryInfoPrices[12].price)
        startActivity(intent)
    }

    private fun openWebPageInBrowser() {
        val browserIntent =
            Intent(Intent.ACTION_VIEW, Uri.parse(AppDefs.deliveryInfoPrices[13].price))
        startActivity(browserIntent)
    }

    private fun initSlider() {
        binding.viewPager.visibility = View.VISIBLE
        binding.tabDots.setupWithViewPager(binding.viewPager, true)
        val mAdapter = ImagesAdapter(ads, washeeMainActivity)
        mAdapter.notifyDataSetChanged()
        binding.viewPager.offscreenPageLimit = ads.size
        binding.viewPager.adapter = mAdapter
        Helpers.setSliderTimer(3000, binding.viewPager, mAdapter)
    }

    private fun getCategories(){
        binding.homeLayout.visibility = View.GONE
        binding.progressBar.visibility = View.VISIBLE
        categories.clear()
        categories.add(Category("0", resources.getString(R.string.title_home), "0"))
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
        val categoriesCall: Call<Categories> =
            retrofit.create(RetrofitAPIs::class.java).getCategories()
        categoriesCall.enqueue(object : Callback<Categories> {
            override fun onResponse(call: Call<Categories>, response: Response<Categories>) {
                binding.homeLayout.visibility = View.VISIBLE
                binding.progressBar.visibility = View.GONE
                if (response.isSuccessful){
                    for (result in response.body()!!.results){
                        categories.add(result)
                    }
                    setCategoriesRV()
                }else{
                    val gson = Gson()
                    val type = object : TypeToken<ErrorResponse>() {}.type //ErrorResponse is the data class that matches the error response
                    val errorResponse = gson.fromJson<ErrorResponse>(response.errorBody()!!.charStream(), type) // errorResponse is an instance of ErrorResponse that will contain details about the error
                    Toast.makeText(washeeMainActivity, errorResponse.status.massage.toString(), Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Categories>, t: Throwable) {
                binding.progressBar.visibility = View.GONE
                Toast.makeText(washeeMainActivity, resources.getString(R.string.internet_connection), Toast.LENGTH_SHORT).show()
            }

        })
    }

    private fun setCategoriesRV(){
        val adapter = CategoriesAdapter(this, categories, 0)
        binding.homeCategoriesRV.adapter = adapter
        binding.homeCategoriesRV.layoutManager = LinearLayoutManager(washeeMainActivity, LinearLayoutManager.HORIZONTAL, false)
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

    fun getCategoryItems(categoryId: String){
        if (categoryId == "0"){
            binding.sliderLayout.visibility = View.VISIBLE
            binding.favorite.visibility = View.VISIBLE
            getAds()
            getFavorites()
        }else{
            binding.favorite.visibility = View.GONE
            binding.sliderLayout.visibility = View.GONE
            binding.itemsRV.visibility = View.VISIBLE
            binding.noFavoritesLayout.visibility = View.GONE
            binding.progressBar.visibility = View.VISIBLE
            categoryItems.clear()
            val itemsObj = CategoryItemsObj(categoryId, latitude, longitude, postalCode)
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
            val categoryItemsCall: Call<CategoryItems> =
                retrofit.create(RetrofitAPIs::class.java).getCategoryItems(itemsObj)
            categoryItemsCall.enqueue(object : Callback<CategoryItems> {
                override fun onResponse(call: Call<CategoryItems>, response: Response<CategoryItems>) {
                    binding.homeLayout.visibility = View.VISIBLE
                    binding.progressBar.visibility = View.GONE
                    if (response.isSuccessful){
                        for (result in response.body()!!.results){
                            categoryItems.add(result)
                        }
                        setCategoryItemsRV()
                    }else{
                        val gson = Gson()
                        val type = object : TypeToken<ErrorResponse>() {}.type //ErrorResponse is the data class that matches the error response
                        val errorResponse = gson.fromJson<ErrorResponse>(response.errorBody()!!.charStream(), type) // errorResponse is an instance of ErrorResponse that will contain details about the error
                        Toast.makeText(washeeMainActivity, errorResponse.status.massage.toString(), Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<CategoryItems>, t: Throwable) {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(washeeMainActivity, resources.getString(R.string.internet_connection), Toast.LENGTH_SHORT).show()
                }

            })
        }

    }

    fun setCategoryItemsRV(){
        val categoryItemsAdapter = CategoryItemsAdapter(this, categoryItems)
        binding.itemsRV.adapter = categoryItemsAdapter
        binding.itemsRV.layoutManager = LinearLayoutManager(washeeMainActivity)
    }

    private fun filterCategoryItems(search: String){
        filteredCategoryItems.clear()
        for (item in categoryItems){
            if (item.title.lowercase(Locale.getDefault()).contains(search)){
                filteredCategoryItems.add(item)
            }
        }
        val categoryItemsAdapter = CategoryItemsAdapter(this, filteredCategoryItems)
        binding.itemsRV.adapter = categoryItemsAdapter
        binding.itemsRV.layoutManager = LinearLayoutManager(washeeMainActivity)
    }

    fun addToBasket(item: CategoryItem){
        binding.numberPickerLayout.visibility = View.VISIBLE
        binding.pickerDoneBtn.setOnClickListener {
            item.selectedServices = ArrayList()
            item.quantity = binding.numberPicker.value
            for (service in item.items_service){
                if (service.checked){
                    item.selectedServices.add(service)
                }
            }
            addItemToCart(item)
            binding.numberPickerLayout.visibility = View.GONE
        }
        binding.pickerCancelBtn.setOnClickListener { binding.numberPickerLayout.visibility = View.GONE }
    }

    private fun addItemToCart(item: CategoryItem){
        binding.progressBar.visibility = View.VISIBLE
        val selectedServices: ArrayList<SelectedService> = ArrayList()
        for (service in item.selectedServices){
            selectedServices.add(SelectedService(service.id))
        }
        val itemObj = AddItemToCartObj(item.id, item.quantity.toString(), selectedServices, item.notes, latitude, longitude, postalCode)
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
        val addToCartCall: Call<BooleanResponse> =
            retrofit.create(RetrofitAPIs::class.java).addItemToCart(itemObj)
        addToCartCall.enqueue(object : Callback<BooleanResponse> {
            override fun onResponse(call: Call<BooleanResponse>, response: Response<BooleanResponse>) {
                binding.progressBar.visibility = View.GONE
                if (response.isSuccessful){
                    getCart()
                    getCategoryItems(selectedCategory)
                }else{
                    getCategoryItems(selectedCategory)
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

    private fun getCart(){
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
                    binding.homeOrderTotal.text = ""+response.body()!!.results.sub_total
                    if(response.body()!!.results.CartItem.size > 0){
                        binding.toolbarLayout.toolbarBasketBadge.visibility = View.VISIBLE
                    }else{
                        binding.toolbarLayout.toolbarBasketBadge.visibility = View.GONE
                    }
                }else{
                    val gson = Gson()
                    val type = object : TypeToken<ErrorResponse>() {}.type //ErrorResponse is the data class that matches the error response
                    val errorResponse = gson.fromJson<ErrorResponse>(response.errorBody()!!.charStream(), type) // errorResponse is an instance of ErrorResponse that will contain details about the error
                    binding.homeOrderTotal.text = "$ 0.0"
                }
            }

            override fun onFailure(call: Call<Cart>, t: Throwable) {
                Toast.makeText(washeeMainActivity, t.message, Toast.LENGTH_SHORT).show()
                binding.progressBar.visibility = View.GONE
//                Toast.makeText(washeeMainActivity, resources.getString(R.string.internet_connection), Toast.LENGTH_SHORT).show()
            }

        })
    }

    private fun getFavorites(){
        binding.progressBar.visibility = View.VISIBLE
        categoryItems.clear()
        val itemsObj = LocationObj(latitude, longitude, postalCode)
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
        val categoryItemsCall: Call<CategoryItems> =
            retrofit.create(RetrofitAPIs::class.java).getFavorites(itemsObj)
        categoryItemsCall.enqueue(object : Callback<CategoryItems> {
            override fun onResponse(call: Call<CategoryItems>, response: Response<CategoryItems>) {
                binding.homeLayout.visibility = View.VISIBLE
                binding.progressBar.visibility = View.GONE
                if (response.isSuccessful){
                    for (result in response.body()!!.results){
                        categoryItems.add(result)
                    }
                    if (categoryItems.size == 0){
                        binding.noFavoritesLayout.visibility = View.VISIBLE
                        binding.itemsRV.visibility = View.GONE
                    }else{
                        binding.noFavoritesLayout.visibility = View.GONE
                        binding.itemsRV.visibility = View.VISIBLE
                        setCategoryItemsRV()
                    }
                }else{
                    val gson = Gson()
                    val type = object : TypeToken<ErrorResponse>() {}.type //ErrorResponse is the data class that matches the error response
                    val errorResponse = gson.fromJson<ErrorResponse>(response.errorBody()!!.charStream(), type) // errorResponse is an instance of ErrorResponse that will contain details about the error
                    Toast.makeText(washeeMainActivity, errorResponse.status.massage.toString(), Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<CategoryItems>, t: Throwable) {
                binding.progressBar.visibility = View.GONE
                Toast.makeText(washeeMainActivity, resources.getString(R.string.internet_connection), Toast.LENGTH_SHORT).show()
            }

        })
    }
    
    fun addFavorite(itemId: String){
        binding.progressBar.visibility = View.VISIBLE
        val itemsObj = Favorite(itemId)
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
        val categoryItemsCall: Call<BooleanResponse> =
            retrofit.create(RetrofitAPIs::class.java).addFavorite(itemsObj)
        categoryItemsCall.enqueue(object : Callback<BooleanResponse> {
            override fun onResponse(call: Call<BooleanResponse>, response: Response<BooleanResponse>) {
                binding.homeLayout.visibility = View.VISIBLE
                binding.progressBar.visibility = View.GONE
                if (response.isSuccessful){
                    getCategoryItems(selectedCategory)
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

    fun removeFavorite(itemId: String){
        binding.progressBar.visibility = View.VISIBLE
        val itemsObj = Favorite(itemId)
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
        val categoryItemsCall: Call<BooleanResponse> =
            retrofit.create(RetrofitAPIs::class.java).removeFavorite(itemsObj)
        categoryItemsCall.enqueue(object : Callback<BooleanResponse> {
            override fun onResponse(call: Call<BooleanResponse>, response: Response<BooleanResponse>) {
                binding.homeLayout.visibility = View.VISIBLE
                binding.progressBar.visibility = View.GONE
                if (response.isSuccessful){
                    getCategoryItems(selectedCategory)
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

    private fun getPrices(){
        val locationObj = LocationObj(latitude, longitude, postalCode)
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
        val getCartCall: Call<DeliveryInfoPrices> =
            retrofit.create(RetrofitAPIs::class.java).getDeliveryInfo(locationObj)
        getCartCall.enqueue(object : Callback<DeliveryInfoPrices> {
            override fun onResponse(call: Call<DeliveryInfoPrices>, response: Response<DeliveryInfoPrices>) {
                if (response.isSuccessful){
                    AppDefs.deliveryInfoPrices = response.body()!!.results
                }else{
                    val gson = Gson()
                    val type = object : TypeToken<ErrorResponse>() {}.type //ErrorResponse is the data class that matches the error response
                    val errorResponse = gson.fromJson<ErrorResponse>(response.errorBody()!!.charStream(), type) // errorResponse is an instance of ErrorResponse that will contain details about the error
                    Toast.makeText(
                        washeeMainActivity,
                        errorResponse.status.massage.toString(),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<DeliveryInfoPrices>, t: Throwable) {
                Toast.makeText(washeeMainActivity, resources.getString(R.string.internet_connection), Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun appInstalledOrNot(uri: String): Boolean {
        val pm = requireActivity().packageManager
        return try {
            pm.getPackageInfo(uri, PackageManager.GET_ACTIVITIES)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
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