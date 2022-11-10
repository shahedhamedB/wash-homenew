package com.washathomes.views.main.washee.checkout.deliveryinfo

import android.Manifest
import android.app.Activity
import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.bumptech.glide.Glide
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.washathomes.apputils.appdefs.AppDefs
import com.washathomes.apputils.appdefs.Urls
import com.washathomes.apputils.modules.*
import com.washathomes.apputils.remote.RetrofitAPIs
import com.washathomes.R
import com.washathomes.views.main.washee.checkout.deliveryinfo.adapters.TimesAdapter
import com.washathomes.views.main.washee.WasheeMainActivity
import com.washathomes.views.maps.MapsActivity
import com.washathomes.databinding.FragmentDeliveryInfoBinding
import dagger.hilt.android.AndroidEntryPoint
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
@AndroidEntryPoint
class DeliveryInfoFragment : Fragment() {

    lateinit var binding: FragmentDeliveryInfoBinding
    lateinit var navController: NavController
    lateinit var washeeMainActivity: WasheeMainActivity
    lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    var prices: ArrayList<DeliveryInfo> = ArrayList()
    var pickUpTimes: ArrayList<String> = ArrayList()
    var dropOffTimes: ArrayList<String> = ArrayList()
    private val LOCATION_CODE = 100
    var deliverySpeed = "0"
    var deliveryOption = "1"
    var pickUpDate = ""
    var pickUpTime = ""
    var pickUpLat = ""
    var pickUpLan = ""
    var pickUpAddress = ""
    var dropOffDate = ""
    var dropOffTime = ""
    var dropOffLat = ""
    var dropOffLan = ""
    var dropOffAddress = ""
    var insurance = "0"
    var latitude = ""
    var longitude = ""
    var postalCode = ""
    var currentTime = ""
    var isPickUp = true
    var sameDropOff = true
    var expressValue = 0.00
    var deliveryFee = 0.00
    var insuranceValue = 0.00
    var currentExpressValue = 0.00
    var currentDeliveryFee = 0.00
    var currentInsuranceValue = 0.00
    var total = 0.0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val layout = inflater.inflate(R.layout.fragment_delivery_info, container, false)
        binding = FragmentDeliveryInfoBinding.inflate(layoutInflater)
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

        openTimePicker()
        setTimes()
    }

    private fun initViews(view: View){
        navController = Navigation.findNavController(view)
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(washeeMainActivity)
    }

    private fun onClick() {
        binding.toolbarBackIcon.setOnClickListener { navController.popBackStack() }
        binding.deliveryServiceSpeedNormal.setOnClickListener {
            deliverySpeed = "0"
            binding.deliveryServiceSpeedNormalRadio.isChecked = true
            binding.deliveryServiceSpeedExpressRadio.isChecked = false
            currentExpressValue = 0.00
            calculateTotal()
        }
        binding.deliveryServiceSpeedExpress.setOnClickListener {
            deliverySpeed = "1"
            binding.deliveryServiceSpeedExpressRadio.isChecked = true
            binding.deliveryServiceSpeedNormalRadio.isChecked = false
            currentExpressValue = expressValue
            calculateTotal()
        }
        binding.deliveryOthersCollectLayout.setOnClickListener {
            deliveryOption = "1"
            binding.deliveryOthersCollectRadio.isChecked = true
            binding.selfDropOffRadio.isChecked = false
            currentDeliveryFee = deliveryFee
            calculateTotal()
        }
        binding.selfDropOffLayout.setOnClickListener {
            deliveryOption = "0"
            binding.deliveryOthersCollectRadio.isChecked = false
            binding.selfDropOffRadio.isChecked = true
            currentDeliveryFee = 0.00
            calculateTotal()
        }
        binding.pickUpDeliveryAddressCard.setOnClickListener {
            isPickUp = true
            startMapsActivity()
        }
        binding.dropOffDeliveryAddressCard.setOnClickListener {
            isPickUp = false
            startMapsActivity()
        }
        binding.pickUpDateLayout.setOnClickListener {
            isPickUp = true
            openDatePicker(0)
        }
        binding.dropOffDateLayout.setOnClickListener {
            isPickUp = false
            if (deliverySpeed == "0"){
                openDatePicker(2)
            }else{
                openDatePicker(1)
            }
        }
        binding.switchInsurance.setOnClickListener {
            if (insurance == "0"){

                Glide.with(washeeMainActivity).load(R.drawable.switch_on).into(binding.switchInsurance)
                insurance = "1"
                currentInsuranceValue = insuranceValue
                calculateTotal()
            }else{
                Glide.with(washeeMainActivity).load(R.drawable.switch_off_gray).into(binding.switchInsurance)
                insurance = "0"
                currentInsuranceValue = 0.0
                calculateTotal()
            }
        }
        binding.switchSamePickUpDropOff.setOnClickListener {
            if(sameDropOff){
                dropOffLat = ""
                dropOffLan = ""
                dropOffDate = ""
                dropOffTime = ""
                sameDropOff = false
                Glide.with(washeeMainActivity).load(R.drawable.switch_off_gray).into(binding.switchSamePickUpDropOff)
                binding.dropOffLayout.visibility = View.VISIBLE
            }else{
                dropOffLat = pickUpLat
                dropOffLan = pickUpLan
                dropOffDate = pickUpDate
                dropOffTime = pickUpTime
                sameDropOff = true
                Glide.with(washeeMainActivity).load(R.drawable.switch_on).into(binding.switchSamePickUpDropOff)
                binding.dropOffLayout.visibility = View.GONE
            }
        }
        binding.deliveryInfoNextButton.setOnClickListener { checkValidation() }
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
                    prices = response.body()!!.results
                    AppDefs.deliveryInfoPrices = prices
                    for (price in prices){
                        if (price.id == "3"){
                            binding.deliveryExpressAmount.text = ""+washeeMainActivity.formatter.format(price.price.toDouble())
                            expressValue = price.price.toDouble()
                        }else if (price.id == "5"){
                            binding.deliveryOthersCollectAmount.text = ""+washeeMainActivity.formatter.format(price.price.toDouble())
                            deliveryFee = price.price.toDouble()
                            currentDeliveryFee = deliveryFee
                        }else if (price.id == "7"){
                            binding.insuraceValue.text = resources.getString(R.string.i_want_to_insure_my_laundry_items)+" ($"+washeeMainActivity.formatter.format(price.price.toDouble())+")"
                            insuranceValue = price.price.toDouble()
                        }
                    }
                    binding.deliveryFees.text = resources.getString(R.string.delivery_fee_for_every_5_miles_is)+" $"+washeeMainActivity.formatter.format(prices[4].price.toDouble())+
                            resources.getString(R.string.plus)+" $"+washeeMainActivity.formatter.format(prices[10].price.toDouble())+" "+resources.getString(R.string.for_every_mile)
                    calculateTotal()
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

    private fun calculateTotal(){
        var total = AppDefs.cartData.total_price.substring(0, AppDefs.cartData.total_price.indexOf(" ")).toDouble()
        val currency = AppDefs.cartData.total_price.substring(AppDefs.cartData.total_price.indexOf(" "))
        total += currentExpressValue + currentDeliveryFee + currentInsuranceValue
        binding.deliveryCurrentTotalText.text = ""+total + currency

    }

    private fun setTimes(){
        AppDefs.times.add("07:00 - 08:00")
        AppDefs.times.add("08:00 - 09:00")
        AppDefs.times.add("09:00 - 10:00")
        AppDefs.times.add("10:00 - 11:00")
        AppDefs.times.add("11:00 - 12:00")
        AppDefs.times.add("12:00 - 13:00")
        AppDefs.times.add("13:00 - 14:00")
        AppDefs.times.add("14:00 - 15:00")
        AppDefs.times.add("15:00 - 16:00")
        AppDefs.times.add("16:00 - 17:00")
        AppDefs.times.add("17:00 - 18:00")
        AppDefs.times.add("18:00 - 19:00")
        AppDefs.times.add("19:00 - 20:00")
        AppDefs.times.add("20:00 - 21:00")
        AppDefs.times.add("21:00 - 22:00")
        AppDefs.times.add("22:00 - 23:00")
        AppDefs.times.add("23:00 - 00:00")
    }

    private fun openDatePicker(days: Int){
        val c = Calendar.getInstance()
        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH)
        val day = c.get(Calendar.DAY_OF_MONTH)


        val dpd = DatePickerDialog(washeeMainActivity, { view, year, monthOfYear, dayOfMonth ->
            var m :String = "" + (monthOfYear+1)
            var d :String = "" + (dayOfMonth)
            var y = year
            if (m.length == 1){
                m = "0$m"
            }
            if (d.length == 1){
                d = "0$d"
            }
            if (isPickUp){
                pickUpDate = "$d/$m/$y"
                binding.pickUpDate.text = pickUpDate
                if (sameDropOff){
                    if (d == "31"){
                        val month = m.toInt()
                        if (deliverySpeed == "1"){
                            d = "01"
                            if (month == 12){
                                m = "01"
                                y = year+1
                            }else{
                                m = (month+1).toString()
                            }
                        }else{
                            d = "02"
                            if (month == 12){
                                m = "02"
                            }else if(month == 11){
                                m = "01"
                            }else{
                                m = (month+1).toString()
                            }
                        }
                    }else if (d == "30"){
                        if (m == "01" || m == "03" || m == "05" || m == "07" || m == "08" || m == "10" || m == "12"){
                            val month = m.toInt()
                            if (deliverySpeed == "1"){
                                d = "31"
                            }else{
                                d = "01"
                                if (month == 12){
                                    m = "01"
                                    y = year+1
                                }else{
                                    m = (month+1).toString()
                                }
                            }
                        }else{
                            val month = m.toInt()
                            if (deliverySpeed == "1"){
                                d = "01"
                                m = (month+1).toString()
                            }else{
                                d = "02"
                                m = (month+1).toString()
                            }
                        }
                    }else if (d == "29"){
                        if (m == "01" || m == "03" || m == "05" || m == "07" || m == "08" || m == "10" || m == "12"){
                            if (deliverySpeed == "1"){
                                d = "30"
                            }else{
                                d = "31"
                            }
                        }else{
                            val month = m.toInt()
                            if (deliverySpeed == "1"){
                                d = "30"
                            }else{
                                d = "01"
                                m = (month+1).toString()
                            }
                        }
                    }else{
                        if (deliverySpeed == "1"){
                            val day = d.toInt()
                            d = (day+1).toString()
                        }else{
                            val day = d.toInt()
                            d = (day+2).toString()
                        }
                    }
                    if (m.length == 1){
                        m = "0$m"
                    }
                    if (d.length == 1){
                        d = "0$d"
                    }
                    dropOffDate = "$d/$m/$y"
                }
                setTimesSpinner(true)
            }else{
                dropOffDate = "$d/$m/$y"
                binding.dropOffDate.text = dropOffDate
                setTimesSpinner(false)
            }

        }, year, month, day)

        if (days != 0){
            val format = SimpleDateFormat("dd/MM/yyyy")
            val date = format.parse(pickUpDate)

            c.add(Calendar.DAY_OF_YEAR, days)

            dpd.datePicker.minDate = c.time.time
        }else{
            dpd.datePicker.minDate = System.currentTimeMillis()
        }

        dpd.show()
    }

    private fun openTimePicker(){
//        val mTimePicker: TimePickerDialog
//        val mcurrentTime = Calendar.getInstance()
//        val hour = mcurrentTime.get(Calendar.HOUR_OF_DAY)
//        val minute = mcurrentTime.get(Calendar.MINUTE)
//
//        mTimePicker = TimePickerDialog(washeeMainActivity, object : TimePickerDialog.OnTimeSetListener {
//            override fun onTimeSet(view: TimePicker?, hourOfDay: Int, minute: Int) {
//                if (isPickUp){
//                    pickUpTime = String.format("%d : %d", hourOfDay, minute)
//                    binding.pickUpTime.text = pickUpTime
//                }else{
//                    dropOffTime = String.format("%d : %d", hourOfDay, minute)
//                    binding.dropOffTime.text = dropOffTime
//                }
//            }
//        }, hour, minute, false)
//
//        mTimePicker.show()
        val sdf = SimpleDateFormat("yyyy:MM:dd:HH:mm")
        val currentDateAndTime: String = sdf.format(Date())

        val date: Date = sdf.parse(currentDateAndTime)!!
        val calendar = Calendar.getInstance()
        calendar.time = date
        calendar.add(Calendar.HOUR, 0)

        val sdf2 = SimpleDateFormat("HH:mm")
        currentTime = sdf2.format(calendar.time)
        println("Time here $currentTime")
    }

    private fun setTimesSpinner(isPickUp: Boolean){
        val currentHour = currentTime.substring(0,2).toInt()
        val sdf = SimpleDateFormat("dd/MM/yyyy")
        val currentDate = sdf.format(Date())
        if (isPickUp){
            if (currentDate == pickUpDate){
                for (time in AppDefs.times){
                    val prefix = time.substring(0,2).toInt()
                    if (prefix > currentHour){
                        pickUpTimes.add(time)
                    }
                }
                setPickUpTimesSpinner()
            }else{
                pickUpTimes = AppDefs.times
                setPickUpTimesSpinner()
            }
        }else{
            dropOffTimes = AppDefs.times
            setDropOffTimesSpinner()
        }
    }

    private fun setPickUpTimesSpinner() {
        val adapter = TimesAdapter(washeeMainActivity, pickUpTimes)
        binding.pickUpTimesSpinner.adapter = adapter

        binding.pickUpTimesSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onNothingSelected(parent: AdapterView<*>?) {

            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                pickUpTime = pickUpTimes[position]
            }

        }
    }

    private fun setDropOffTimesSpinner() {
        val adapter = TimesAdapter(washeeMainActivity, dropOffTimes)
        binding.dropOffTimesSpinner.adapter = adapter

        binding.dropOffTimesSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onNothingSelected(parent: AdapterView<*>?) {

            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                dropOffTime = dropOffTimes[position]
            }

        }
    }

    private fun startMapsActivity() {
        val intent = Intent(context, MapsActivity::class.java)
        startActivityForResult(
            intent,
            LOCATION_CODE
        )
    }

    private fun getCurrentLocation(){
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
                getAddress(location.latitude, location.longitude, "")
                getPrices()
            }else{
                Toast.makeText(washeeMainActivity, "Null", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun getAddress(latitude: Double, longitude: Double, type: String){
        val geocoder: Geocoder
        val addresses: List<Address>
        geocoder = Geocoder(washeeMainActivity, Locale.getDefault())

        addresses = geocoder.getFromLocation(
            latitude,
            longitude,
            1
        ) // Here 1 represent max location result to returned, by documents it recommended 1 to 5

        if (type == "0"){
            pickUpAddress = addresses[0].getAddressLine(0)
        }else if (type == "1"){
            dropOffAddress = addresses[0].getAddressLine(0)
        }

//        address = addresses[0].getAddressLine(0) // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
        if (addresses[0].postalCode != null){
            postalCode = addresses[0].postalCode
        }
    }

    private fun checkValidation(){
        if (pickUpLat.isEmpty()){
            Toast.makeText(washeeMainActivity, resources.getString(R.string.empty_pick_up_location), Toast.LENGTH_SHORT).show()
        }else if (pickUpDate.isEmpty()){
            Toast.makeText(washeeMainActivity, resources.getString(R.string.empty_pick_up_date), Toast.LENGTH_SHORT).show()
        }else if (!sameDropOff){
            if (dropOffLat.isEmpty()){
                Toast.makeText(washeeMainActivity, resources.getString(R.string.empty_drop_off_location), Toast.LENGTH_SHORT).show()
            }else if (dropOffDate.isEmpty()){
                Toast.makeText(washeeMainActivity, resources.getString(R.string.empty_drop_off_date), Toast.LENGTH_SHORT).show()
            }else{
                updateDeliveryInfo()
            }
        }else{
            updateDeliveryInfo()
        }
    }

    private fun updateDeliveryInfo(){
        binding.progressBar.visibility = View.VISIBLE
        val updateDeliveryInfo = UpdateDeliveryInfo(deliveryOption, deliverySpeed, insurance, pickUpDate, pickUpTime, dropOffDate, dropOffTime, latitude, longitude, postalCode, pickUpLat, pickUpLan, dropOffLat, dropOffLan)
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
        val updateDeliveryCall: Call<BooleanResponse> =
            retrofit.create(RetrofitAPIs::class.java).updateDeliveryInfo(updateDeliveryInfo)
        updateDeliveryCall.enqueue(object : Callback<BooleanResponse> {
            override fun onResponse(call: Call<BooleanResponse>, response: Response<BooleanResponse>) {
                binding.progressBar.visibility = View.GONE
                if (response.isSuccessful) {
                    AppDefs.deliveryInfo = DeliveryInfoObj(deliverySpeed, deliveryOption, pickUpAddress, dropOffAddress, pickUpDate, pickUpTime, dropOffDate, dropOffTime, insurance)
                    navController.navigate(DeliveryInfoFragmentDirections.actionDeliveryInfoFragmentToOverviewFragment())
//                    AppDefs.cartData.total_price = total.toString()
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

            override fun onFailure(call: Call<BooleanResponse>, t: Throwable) {
                Toast.makeText(washeeMainActivity, resources.getString(R.string.internet_connection), Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun requestPermission(){
        ActivityCompat.requestPermissions(
            washeeMainActivity, arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION),
            LOCATION_CODE
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == LOCATION_CODE && resultCode == Activity.RESULT_OK) {
            if (isPickUp){
                pickUpLat = data!!.getStringExtra("latitude")!!
                pickUpLan = data.getStringExtra("longitude")!!
                getAddress(pickUpLat.toDouble(), pickUpLan.toDouble(), "0")
                if (sameDropOff){
                    dropOffLat = data.getStringExtra("latitude")!!
                    dropOffLan = data.getStringExtra("longitude")!!
                    getAddress(dropOffLat.toDouble(), dropOffLan.toDouble(), "1")
                }
            }else{
                dropOffLat = data!!.getStringExtra("latitude")!!
                dropOffLan = data.getStringExtra("longitude")!!
                getAddress(dropOffLat.toDouble(), dropOffLan.toDouble(), "1")
            }
        }
    }


}