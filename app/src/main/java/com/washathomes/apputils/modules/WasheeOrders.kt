package com.washathomes.apputils.modules

data class WasheeOrders(val status: Status, val results: ArrayList<WasheeActiveOrder>)

data class WasheeActiveOrder(val id: String, val user_id: String, val washer_is_delevery: String, val lat: String, val long: String, val date: String, val time: String, val pickup_date: String, val pickup_time: String, val delivery_date: String, val delivery_time: String, val pickup_lat: String, val picku_long: String, val dropoff_lat: String, val dropoff_long: String, val is_delivery_pickup: String, val is_express: String, val delivery_amount: String, val total_amount: String, val status: String, val status_title: String, val state_title: String, val orders_items: ArrayList<OrderItem>)

