package com.washathomes.apputils.modules

data class DeliveryInfo(val id: String, val name: String, val price: String)

data class DeliveryInfoPrices(val status: Status, val results: ArrayList<DeliveryInfo>)

data class LocationObj(val lat: String, val long: String, val zip_code: String)

data class UpdateDeliveryInfo(val delivery_pickup: String, val express: String, val insurance: String, val pickup_date: String, val pickup_time: String, val delivery_date: String, val delivery_time: String, val lat: String, val long: String, val zip_code: String, val pickup_lat: String, val picku_long: String, val dropoff_lat: String, val dropoff_long: String)

data class DeliveryInfoObj(val deliverySpeed: String, val deliveryOptions: String, val pickUpAddress: String, val dropOffAddress: String, val pickUpDate: String, val pickUpTime: String, val dropOffDate: String, val dropOffTime: String, val insurance: String)
