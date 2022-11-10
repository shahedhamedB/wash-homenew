package com.washathomes.views.main.washee.more.orderhistory

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
import com.washathomes.apputils.appdefs.AppDefs.Companion.order
import com.washathomes.R
import com.washathomes.views.main.washee.WasheeMainActivity
import com.washathomes.databinding.FragmentOrderDetailsBinding
import dagger.hilt.android.AndroidEntryPoint
import java.util.*
@AndroidEntryPoint
class OrderDetailsFragment : Fragment() {

    lateinit var binding: FragmentOrderDetailsBinding
    lateinit var washeeMainActivity: WasheeMainActivity
    lateinit var navController: NavController
    var subTotal = 0.00
    var taxValue = 0.00
    var pickUpAddress = ""
    var dropOffAddress = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val layout = inflater.inflate(R.layout.fragment_order_details, container, false)
        binding = FragmentOrderDetailsBinding.inflate(layoutInflater)
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
    }

    private fun initViews(view: View){
        navController = Navigation.findNavController(view)
        getAddress(order.pickup_lat.toDouble(), order.picku_long.toDouble(), true)
        getAddress(order.dropoff_lat.toDouble(), order.dropoff_long.toDouble(), false)
        setData()
    }

    private fun onClick(){
        binding.toolbarBackIcon.setOnClickListener { navController.popBackStack() }
        binding.viewItemsLayout.setOnClickListener { navController.navigate(OrderDetailsFragmentDirections.actionOrderDetailsFragmentToViewOrderItemsFragment()) }
    }

    private fun setData(){
        binding.orderId.text = resources.getString(R.string.order)+" #"+order.id
        binding.orderUpdated.text = resources.getString(R.string.order_updated_on)+" "+order.time+" "+order.date
        if (order.is_express == "0"){
            binding.deliverySpeed.text = resources.getString(R.string.normal_delivery)
        }else{
            binding.deliverySpeed.text = resources.getString(R.string.express_delivery)
        }
        if (order.is_delivery_pickup == "0"){
            binding.deliveryOption.text = resources.getString(R.string.self_drop_off_amp_pickup)
        }else{
            binding.deliveryOption.text = resources.getString(R.string.washer_driver_to_collect_amp_return)
        }
        binding.pickUpAddress.text = pickUpAddress
        binding.dropOffAddress.text = dropOffAddress
        binding.pickUpDate.text = order.buyer_pickup_date
        binding.dropOffDate.text = order.buyer_dropoff_date
        binding.pickUpTime.text = order.pickup_time
        binding.dropOffTime.text = order.delivery_time
        binding.paymentTaxLabel.text = resources.getString(R.string.tax)+" "+AppDefs.deliveryInfoPrices[8].price+"%"
        subTotal = order.total_amount.toDouble()
        taxValue = AppDefs.deliveryInfoPrices[8].price.toDouble()*subTotal
        binding.paymentSubTotalText.text = ""+washeeMainActivity.formatter.format(subTotal)
        binding.paymentTaxText.text = ""+washeeMainActivity.formatter.format(taxValue)
        binding.viewItems.text = resources.getString(R.string.view_items)+" ("+order.orders_items.size+")"
        calculateTotal()
    }

    private fun calculateTotal(){
        val taxPercent = AppDefs.deliveryInfoPrices[8].price.toDouble()*100
        binding.paymentTaxLabel.text = resources.getString(R.string.tax)+" ("+taxPercent+"%)"
        binding.paymentCurrentTotalText.text = ""+order.total_amount
        binding.paymentSubTotalText.text = ""+order.sub_total
        binding.paymentTaxText.text = ""+order.tax
        binding.paymentDiscountText.text = ""+order.discount
    }

    private fun getAddress(latitude: Double, longitude: Double, isPickUp: Boolean){
        val geocoder: Geocoder
        val addresses: List<Address>
        geocoder = Geocoder(washeeMainActivity, Locale.getDefault())

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