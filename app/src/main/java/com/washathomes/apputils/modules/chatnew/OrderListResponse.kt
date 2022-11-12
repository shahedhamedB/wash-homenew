package com.washathomes.apputils.modules.chatnew

import com.google.gson.annotations.SerializedName

data class OrderListResponse(
    @SerializedName("results")
    val results: List<OrderModel>,
    @SerializedName("status")
    val status: Status
)