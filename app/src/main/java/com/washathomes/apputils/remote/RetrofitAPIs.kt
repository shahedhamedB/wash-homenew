package com.washathomes.apputils.remote

import com.washathomes.apputils.modules.*
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface RetrofitAPIs {

    //Registration
    @GET("splash_screen")
    fun getAppData(): Call<DataList>

    @GET("get_language")
    fun getLanguages(): Call<Languages>

    @POST("register_phone")
    fun login(@Body userParams: UserLogin): Call<UserData>

    @GET("get_user_type")
    fun getAccountTypes(): Call<AccountTypes>

    @GET("get_user_intro/1")
    fun getWasheeIntro(): Call<AccountTypeIntros>

    @GET("get_user_intro/2")
    fun getWasherIntro(): Call<AccountTypeIntros>

    @GET("get_user_intro/3")
    fun getCourierIntro(): Call<AccountTypeIntros>

    @POST("update_data")
    fun updateUser(@Body userParams: UpdateUser): Call<UserData>

    @POST("driving_data")
    fun updateDrivingData(@Body userParams: UpdateDrivingData): Call<UserData>

    @GET("get_service")
    fun getServices(): Call<Services>

    @POST("services_update")
    fun updateServicesData(@Body userParams: UpdateServicesData): Call<UserData>

    @POST("update_sign_in")
    fun updateSignIn(@Body userTypeObj: UserTypeObj): Call<UserData>

    @POST("get_notifications")
    fun getNotifications(@Body userTypeObj: UserTypeObj): Call<Notifications>

    @POST("delete_notifications")
    fun deleteNotification(@Body notificationId: NotificationId): Call<BooleanResponse>

    @POST("read_notifications")
    fun readNotification(@Body notificationId: NotificationId): Call<BooleanResponse>

    @GET("delete_user")
    fun deleteAccount(): Call<BooleanResponse>

    //WasheeMain
    @GET("cat_list")
    fun getCategories(): Call<Categories>

    @POST("items_list")
    fun getCategoryItems(@Body params: CategoryItemsObj): Call<CategoryItems>

    @POST("get_favorite")
    fun getFavorites(@Body params: LocationObj): Call<CategoryItems>

    @POST("user_add_fav_item")
    fun addFavorite(@Body params: Favorite): Call<BooleanResponse>

    @POST("user_delete_fav_item")
    fun removeFavorite(@Body params: Favorite): Call<BooleanResponse>

    @POST("add_cart")
    fun addItemToCart(@Body item: AddItemToCartObj): Call<BooleanResponse>

    @POST("get_cart")
    fun getCart(@Body cartObj: CartObj): Call<Cart>

    @POST("update_quantity_cart")
    fun updateQuantity(@Body qnt: UpdateQuantityObj): Call<BooleanResponse>

    @POST("delete_items_cart")
    fun deleteCartItem(@Body cartItemObj: DeleteItemObj): Call<BooleanResponse>

    @POST("get_items_options")
    fun getDeliveryInfo(@Body locationObj: LocationObj): Call<DeliveryInfoPrices>

    @POST("update_delivery_info")
    fun updateDeliveryInfo(@Body updateDeliveryInfo: UpdateDeliveryInfo): Call<BooleanResponse>

    @POST("promo_code")
    fun getPromoCode(@Body promoCodeObj: PromoCodeObj): Call<PromoCodeResponse>

    @POST("update_overview")
    fun updateOverview(@Body overviewObj: OverviewObj): Call<BooleanResponse>

    @POST("update_user_lang")
    fun updateLanguage(@Body updateLanguage: UpdateLanguage): Call<UserData>

    @POST("get_orders")
    fun getOrderHistory(@Body orderHistoryObj: OrderHistoryObj): Call<OrderHistory>

    @GET("get_orders_active")
    fun getWasheeActiveOrders(): Call<WasheeOrders>

    @POST("droupoff_order_washee_to_landry")
    fun washeeDropOffOrder(@Body orderObj: OrderObj):Call<BooleanResponse>

    @POST("pickup_order_washee_to_landry")
    fun washeePickUpOrder(@Body orderObj: OrderObj):Call<BooleanResponse>

    @POST("create_orders")
    fun createOrder(@Body orderObj: CreateOrderObj):Call<BooleanResponse>

    @POST("get_ads")
    fun getAds(@Body locationObj: LocationObj):Call<Ads>

    @POST("get_washee_order_review")
    fun getWasheeOrderReview(@Body orderObj: OrderObj): Call<WasheeReviewData>

    @POST("order_washee_review")
    fun washeeOrderReview(@Body review: Review): Call<BooleanResponse>

    @GET("order_list_buyer_chat")
    fun getWasheeInbox(): Call<InboxMessages>

    //Washer

    @GET("washer_available")
    fun getWasherAvailability(): Call<UserData>

    @GET("washer_available_delevery")
    fun getWasherDeliveryAvailability(): Call<UserData>

    @GET("get_orders_washer_active")
    fun getWasherActiveOrders(): Call<ActiveOrders>

    @GET("get_orders_washer_pending")
    fun getWasherPendingOrders(): Call<PendingOrders>

    @POST("acsspt_order_washer")
    fun washerAcceptOrder(@Body orderObj: OrderObj):Call<BooleanResponse>

    @POST("reject_order_washer")
    fun washerRejectOrder(@Body orderObj: OrderObj):Call<BooleanResponse>

    @POST("pickup_order_washer_to_washee")
    fun washerPickUpOrder(@Body orderObj: OrderObj):Call<BooleanResponse>

    @POST("end_landry_order_washer")
    fun washerFinishOrder(@Body orderObj: OrderObj):Call<BooleanResponse>

    @POST("droupoff_order_washer_to_washee")
    fun washerDropOffOrder(@Body orderObj: OrderObj):Call<BooleanResponse>

    @POST("get_user_order_review")
    fun getWasherReview(@Body orderObj: OrderObj):Call<WasherReview>

    @POST("order_washer_review")
    fun washerOrderReview(@Body review: Review): Call<BooleanResponse>

    @GET("wallet_summary_washer")
    fun washerWallet():Call<WalletObj>

    @POST("get_orders_washer_history")
    fun getWasherOrderHistory(@Body statusStr: StatusStr): Call<OrderHistoryResponse>

    @GET("order_list_seller_chat")
    fun getWasherInbox(): Call<InboxMessages>

    // Courier

    @GET("driver_available")
    fun changeCourierAvailability(): Call<UserData>

    @GET("get_orders_driver_active")
    fun getCourierActiveOrders(): Call<ActiveOrders>

    @GET("get_orders_driver_pending")
    fun getCourierPendingOrders(): Call<PendingOrders>

    @POST("acsspt_order_driver")
    fun courierAcceptOrder(@Body orderObj: OrderObj):Call<BooleanResponse>

    @POST("reject_order_driver")
    fun courierRejectOrder(@Body orderObj: OrderObj):Call<BooleanResponse>

    @POST("pickup_order_driver_to_washer")
    fun courierPickUpOrderFromWashee(@Body orderObj: OrderObj):Call<BooleanResponse>

    @POST("dropoff_order_driver_to_washer")
    fun courierDropOffOrderToWasher(@Body orderObj: OrderObj):Call<BooleanResponse>

    @POST("pickup_order_driver_to_landry")
    fun courierPickUpOrderFromWasher(@Body orderObj: OrderObj):Call<BooleanResponse>

    @POST("droupoff_order_driver_to_washee")
    fun courierDropOffOrderToWashee(@Body orderObj: OrderObj):Call<BooleanResponse>

    @POST("get_driver_order_review")
    fun getCourierReview(@Body orderObj: OrderObj):Call<CourierReview>

    @POST("order_driver_review")
    fun courierOrderReview(@Body review: Review): Call<BooleanResponse>

    @GET("wallet_summary_dirver")
    fun driverWallet():Call<WalletObj>

    @POST("get_orders_driver_history")
    fun getCourierOrderHistory(@Body statusStr: StatusStr): Call<OrderHistoryResponse>

    @GET("order_list_seller_chat")
    fun getCourierInbox(): Call<InboxMessages>
}