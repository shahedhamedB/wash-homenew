package com.washathomes.apputils.modules.chatnew

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class OrderModel(
    @SerializedName("buyer_profile_picture")
    val buyer_profile_picture: String,
    @SerializedName("courier_id")
    val courier_id: String,
    @SerializedName("date_created")
    val date_created: String,
    @SerializedName("driver_profile_picture")
    val driver_profile_picture: String,
    @SerializedName("id")
    val id: String,
    @SerializedName("seller_profile_picture")
    val seller_profile_picture: String,
    @SerializedName("id_status")
    val id_status: String?,
    @SerializedName("washee_id")
    val washee_id: String,
    @SerializedName("washer_id")
    val washer_id: String
) : Parcelable