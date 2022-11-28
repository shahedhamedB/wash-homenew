package com.washathomes.views.main.washee.checkout.payment

import android.os.Bundle
import androidx.lifecycle.MutableLiveData
import com.washathomes.apputils.base.BaseViewModel
//import com.washathomes.base.ui.BaseViewModel
//import com.washathomes.data.remote.APIManager
//import com.washathomes.data.remote.StripeAPIManager
//import com.washathomes.model.data.Basket
//import com.washathomes.model.data.Order
//import com.washathomes.model.data.UserAddress
import com.washathomes.model.stripe.IntentResponse
import com.washathomes.retrofit.remote.APIManager
import java.util.*
import javax.inject.Inject

class WasheePaymentViewModel @Inject constructor(
//    private val stripeAPI: StripeAPIManager,
    private val apiManager: APIManager
) : BaseViewModel() {

    var liveData = MutableLiveData<ModelState>()
    var clientSecret = ""
    lateinit var intentResponse: IntentResponse
//    lateinit var basket: Basket
//    lateinit var userAddress: UserAddress

    override fun handleIntent(extras: Bundle) {
        super.handleIntent(extras)
//        basket = extras.getParcelable("basket") ?: Basket()
//        userAddress = extras.getParcelable("orderAddress") ?: UserAddress()
        stateLiveData.value = State.ShowContent
    }

//    fun placeOrder() {
//        apiManager.addOrder(basket).sendRequest({
//            getOrder(it.order_id)
//        }) {
//
//        }
//    }

//    private fun getOrder(orderId: Int) {
//        apiManager.getOrderById(orderId).sendRequest {
//            liveData.value = ModelState.OnSetOrderSuccess(it.data)
//            stateLiveData.value = State.ShowContent
//        }
//    }

    fun createPaymentIntent(params: HashMap<String, Any>) {
//        stripeAPI.createPaymentIntent(params).sendRequest(
//                false,
//            requestType = RequestType.ACTION,
//            stateType = StateType.NONE,
//            successHandler = {
//                intentResponse = it
//                clientSecret = it.response.client_secret
//                basket.payment_id_to_charge = it.response.id
//                liveData.value = ModelState.OnCreatePaymentIntentSuccess
//            })
//        {
//            stateLiveData.value = State.OnError(it, RequestType.ACTION)
//            stateLiveData.value = State.ShowContent
//        }
    }

    sealed class ModelState {
//        object OnCreatePaymentIntentSuccess : ModelState()
//        data class OnSetOrderSuccess(val order: Order) : ModelState()
    }
}