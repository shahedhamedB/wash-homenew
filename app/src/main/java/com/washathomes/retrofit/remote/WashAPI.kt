package com.washathomes.retrofit.remote


import com.washathomes.apputils.modules.chatmodel.Order
import com.washathomes.apputils.modules.chatnew.OrderListResponse
import io.reactivex.Observable

import retrofit2.Response
import retrofit2.http.*

interface WashAPI {

    // region Orders
    @FormUrlEncoded
    @POST("order_detailed_buyer_info")
    suspend fun getBuyerOrder(@Header("Authorization")token: String,@Field("order_id") orderId: Int): Response<Order>


    @GET("order_list_buyer_chat")
    suspend fun getBuyerOrdersChat(@Header("Authorization")token: String): Response<OrderListResponse>




    @GET("order_list_seller_chat")
    suspend fun getSellerOrdersChat(@Header("Authorization")token: String): Response<OrderListResponse>

    @GET("order_list_driver_chat")
    suspend fun getDriverOrdersChat(@Header("Authorization")token: String): Response<OrderListResponse>
    @FormUrlEncoded
    @POST("buyer_order_delete")
    fun buyerOrderDelete(
        @Field("order_id") orderId: Int,
        @Field("feedback") feedback: String
    ): Observable<Response<ArrayList<Unit>>>


}