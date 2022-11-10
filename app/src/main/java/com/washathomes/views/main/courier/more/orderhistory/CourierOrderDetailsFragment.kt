package com.washathomes.views.main.courier.more.orderhistory

import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.washathomes.apputils.appdefs.AppDefs
import com.washathomes.R
import com.washathomes.views.main.courier.CourierMainActivity
import com.washathomes.databinding.FragmentCourierOrderDetailsBinding
import dagger.hilt.android.AndroidEntryPoint
import java.util.*
@AndroidEntryPoint
class CourierOrderDetailsFragment : Fragment() {

    lateinit var binding: FragmentCourierOrderDetailsBinding
    lateinit var courierMainActivity: CourierMainActivity
    lateinit var navController: NavController
    var subTotal = ""
    var taxValue = ""
    var pickUpAddress = ""
    var dropOffAddress = ""
    
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val layout = inflater.inflate(R.layout.fragment_courier_order_details, container, false)
        binding = FragmentCourierOrderDetailsBinding.inflate(layoutInflater)
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
        getAddress(AppDefs.orderHistory.pickup_lat.toDouble(), AppDefs.orderHistory.picku_long.toDouble(), true)
        getAddress(AppDefs.orderHistory.dropoff_lat.toDouble(), AppDefs.orderHistory.dropoff_long.toDouble(), false)
        setData()
    }

    private fun onClick(){
        binding.toolbarBackIcon.setOnClickListener { navController.popBackStack() }
        binding.viewItemsLayout.setOnClickListener { navController.navigate(
            CourierOrderDetailsFragmentDirections.actionCourierOrderDetailsFragmentToCourierViewItemsFragment()) }
    }

    private fun setData(){
        binding.orderId.text = resources.getString(R.string.order)+" #"+ AppDefs.orderHistory.id
        binding.orderUpdated.text = resources.getString(R.string.order_updated_on)+" "+ AppDefs.orderHistory.time+" "+ AppDefs.orderHistory.date
        if (AppDefs.orderHistory.is_express == "0"){
            binding.deliverySpeed.text = resources.getString(R.string.normal_delivery)
        }else{
            binding.deliverySpeed.text = resources.getString(R.string.express_delivery)
        }
        if (AppDefs.orderHistory.is_delivery_pickup == "0"){
            binding.deliveryOption.text = resources.getString(R.string.self_drop_off_amp_pickup)
        }else{
            binding.deliveryOption.text = resources.getString(R.string.washer_driver_to_collect_amp_return)
        }
        binding.pickUpAddress.text = pickUpAddress
        binding.dropOffAddress.text = dropOffAddress
        binding.pickUpDate.text = AppDefs.orderHistory.pickup_date
        binding.dropOffDate.text = AppDefs.orderHistory.delivery_date
        binding.pickUpTime.text = AppDefs.orderHistory.pickup_time
        binding.dropOffTime.text = AppDefs.orderHistory.delivery_time
        val taxPercent = AppDefs.deliveryInfoPrices[8].price.toDouble()*100
        binding.paymentTaxLabel.text = resources.getString(R.string.tax)+" ("+taxPercent+"%)"
        binding.paymentCurrentTotalText.text = ""+AppDefs.orderHistory.total_amount
        binding.paymentSubTotalText.text = ""+AppDefs.orderHistory.sub_total
        binding.paymentTaxText.text = ""+AppDefs.orderHistory.tax
        binding.paymentDiscountText.text = ""+AppDefs.orderHistory.discount
    }

//    private fun calculateTotal(discount: Double){
//        val discountValue = discount*subTotal
//        if (discount != 0.00){
//            binding.paymentDiscountLabel.text = resources.getString(R.string.discount)+" "+discount+"%"
//            binding.paymentDiscountText.text = ""+courierMainActivity.formatter.format(discountValue)
//        }
//        val total = subTotal+taxValue-discountValue
//        binding.paymentCurrentTotalText.text = ""+courierMainActivity.formatter.format(total)
//    }

    private fun getAddress(latitude: Double, longitude: Double, isPickUp: Boolean){
        val geocoder: Geocoder
        val addresses: List<Address>
        geocoder = Geocoder(courierMainActivity, Locale.getDefault())

        addresses = geocoder.getFromLocation(
            latitude,
            longitude,
            1
        ) // Here 1 represent max location result to returned, by documents it recommended 1 to 5

        // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
        if (isPickUp){
            pickUpAddress = addresses[0].getAddressLine(0)
        }else{
            dropOffAddress = addresses[0].getAddressLine(0)
        }

    }

}
