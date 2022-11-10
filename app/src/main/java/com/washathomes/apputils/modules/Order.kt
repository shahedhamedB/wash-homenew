package com.washathomes.apputils.modules

data class OrderHistory(val status: Status, val results: ArrayList<Order>)

data class Order(val id: String, val user_id: String, val buyer_dropoff: String, val buyer_dropoff_date: String, val buyer_pickup: String, val buyer_pickup_date: String, val seller_dropoff: String, val seller_dropoff_date: String, val seller_pickup: String, val seller_pickup_date: String, val driver_confirmed_date: String
    , val date: String, val time: String, val pickup_date: String, val pickup_time: String, val delivery_date: String, val delivery_time: String, val pickup_lat: String, val picku_long: String, val dropoff_lat: String, val dropoff_long: String, val is_delivery_pickup: String, val is_express: String, val total_amount: String, val sub_total: String, val tax: String, val discount: String
    , val status: String, val orders_items: ArrayList<OrderItem>)

data class OrderItem(val id: String, val title: String, val price: String, val image: String, val status: String, val quantity: String, val servcie: ArrayList<ServiceItem>)

data class ServiceItem(val id: String, val title: String)

data class OrderHistoryObj(val status: String, val page: String)

data class CreateOrderObj(val lat: String, val long: String, val zip_code: String, val type_pymant: String, val payment_id_to_charge: String, val payment_provider_id: String)
