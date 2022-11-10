package com.washathomes.apputils.modules

data class ActiveOrders(val status: Status, val results: ArrayList<ActiveOrder>)

data class ActiveOrder(val Date: String, val Orders: ArrayList<ActiveOrderObj>)

data class ActiveOrderObj(val id: String, val user_id: String, val washer_is_delevery: String, val lat: String, val long: String, val date: String, val time: String, val pickup_date: String, val pickup_time: String, val delivery_date: String, val delivery_time: String, val pickup_lat: String, val picku_long: String, val dropoff_lat: String, val dropoff_long: String, val is_delivery_pickup: String, val is_express: String, val sub_total: String, val delivery_amount: String, val total_amount: String, val status: String, val status_title: String, val state_title: String, val orders_items: ArrayList<OrderItem>, val washee_name: WasheeData?)

data class PendingOrders(val status: Status, val results: ArrayList<PendingOrder>)

data class PendingOrder(val Date: String, val Orders: ArrayList<PendingOrderObj>)

data class PendingOrderObj(val id: String, val user_id: String, val remain_min: String, val washer_is_delevery: String, val lat: String, val long: String, val date: String, val time: String, val pickup_date: String, val pickup_time: String, val delivery_date: String, val delivery_time: String, val pickup_lat: String, val picku_long: String, val dropoff_lat: String, val dropoff_long: String, val is_delivery_pickup: String, val is_express: String, val sub_total: String, val delivery_amount: String, val total_amount: String, val status: String, val status_title: String, val state_title: String, val orders_items: ArrayList<OrderItem>, val washee_name: WasheeData?)

data class OrderObj(val order_id: String)

data class WasheeData(val name: String?, val phone: String?)
