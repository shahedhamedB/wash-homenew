package com.washathomes.apputils.modules.chatmodel

import android.os.Parcelable
import com.washathomes.apputils.modules.OrderItem

import kotlinx.android.parcel.Parcelize
import java.util.*

@Parcelize
data class Order(
    val id: Int,
    val order_no: String,
    val amount_com: Double,
    val amount_express: Double,
    val amount_items_total: Double,
    val amount_seller: Double,
    val amount_seller_total: Double,
    val amount_tax: Double,
    val amount_total: Double,
    val buyer_address1: String?,
    val buyer_address2: String?,
    val buyer_cancel_date: Date,
    val buyer_city: String?,
    val buyer_country: String?,
    val buyer_dropoff: Int,
    val buyer_dropoff_date: Date,
    val buyer_address_id: Int,
    val buyer_first_name: String,
    val buyer_last_name: String,
    val buyer_lat: String?,
    val buyer_lon: String?,
    val buyer_pickup: String,
    val buyer_pickup_date: Date,
    val buyer_postal_code: String?,
    val buyer_profile_picture: String,
    val buyer_reviewed_date: Date,
    val buyer_state: String?,
    val buyer_user_id: Int,
    val date_confirmed: Date?,
    val date_created: Date,
    val date_completed: Date,
    val date_deleted: Date,
    val date_modified: Date,
    val delivery_date: Date,
    val delivery_found: String,
    val delivery_pickup_amount: Double,
    val delivery_pickup_service: Int,
    val delivery_time: String,
    val delivery_time_end: String,
    val delivery_time_start: String,
    val driver_confirmed_date: Date,
    val driver_first_name: String?,
    val driver_last_name: String?,
    val driver_profile_picture: String,
    val driver_reviewed_date: Date,
    val driver_user_id: Int,
    val insurance_amount: Double,
    val is_express: Int,
    val is_insurance: Int,
    val no_of_items: Int,
    val notes_buyer: String?,
    val notes_delivery: String?,
    val notes_seller: String?,
    val pickup_date: Date,
    val pickup_time: String,
    val pickup_time_end: String,
    val pickup_time_start: String,
    val remain_min: Int,
    val seller_address1: String?,
    val seller_address2: String?,
    val seller_city: String?,
    val seller_confirmed_date: Date,
    val seller_country: String?,
    val seller_dropoff: Int,
    val seller_dropoff_date: String,
    val seller_found: String,
    val seller_first_name: String,
    val seller_last_name: String,
    val seller_lat: String?,
    val seller_lon: String?,
    val seller_pickup: String,
    val seller_pickup_date: String,
    val seller_postal_code: String?,
    val seller_profile_picture: String,
    val seller_reviewed_date: Date,
    val seller_state: String,
    val seller_user_id: Int,
    val status: Int,
    val status_title: String,
    val next_date: Date,
    var lineType: Int = 0, // ViewType - washer home line
    val amount_total_to_show: String,
    val delivery_pickup_amount_to_show: String,
    val amount_seller_to_show: String,
    val amount_com_to_show: String,
    val amount_items_total_to_show: String,
    val amount_tax_to_show: String,
    val subtotal_to_show: String,
    val amount_order_to_show: String,
    val pickup_datetime: Date,
    val delivery_datetime: Date
) : Parcelable {

    fun getBuyerFullName() = "$buyer_first_name $buyer_last_name"
    /* fun getSellerFullName() = "$seller_first_name $seller_last_name"
    fun getDriverFullName() = "$driver_first_name $driver_last_name"
    fun getCreateDateFormatted(): String = this.date_created.getOrderDateStr()
    fun getCompleteDateFormatted(): String = this.date_completed.getOrderDateStr()
    fun getDeletedDateFormatted(): String = this.date_deleted.getOrderDateStr()
    fun getModifiedDateFormatted(): String = this.date_modified.getOrderDateStr()
    fun getConfirmDateFormatted(): String = this.date_confirmed.getOrderDateStr()
    fun getPickupDayFormatted(): String = this.pickup_datetime.getOrderDateWithDayStr()
    fun getDeliveryDayFormatted(): String = this.delivery_datetime.getOrderDateWithDayStr()
    fun getPickupTime(): String = this.pickup_datetime.getOrderTime()
    fun getDeliveryTime(): String = this.delivery_datetime.getOrderTime()*/



    fun getChatRoom(): ChatRoom {
        return ChatRoom(
            orderId = id.toString(),
            roomKey = this.id.toString(),
            buyerId = this.buyer_user_id.toString(),
            sellerId = this.seller_user_id.toString(),
            driverId = this.driver_user_id.toString(),
            order = this,
            messages = ArrayList()
        )
    }

}


