package com.washathomes.apputils.modules

data class AddItemToCartObj(val items_id: String, val quantity: String, val service: ArrayList<SelectedService>, val note: String, val lat: String, val long: String, val zip_code: String)

data class SelectedService(val service_id: String)

data class CartObj(val lat: String, val long: String, val zip_code: String)

data class Cart(val status: Status, val results: CartData)

data class CartData(val id: String, val sub_total: String, var total_price: String, val discount: String, val taks: String, val CartItem: ArrayList<CartItem>, val status: String)

data class CartItem(val id: String, val title: String, val price: String, val image: String, val status: String, val quantity: String, val servcie: ArrayList<CartItemService>)

data class CartItemService(val id: String, val title: String)

data class UpdateQuantityObj(val cart_id: String, val items_id: String, val quantity: String, val lat: String, val long: String, val zip_code: String)

data class DeleteItemObj(val cart_id: String, val items_id: String)
