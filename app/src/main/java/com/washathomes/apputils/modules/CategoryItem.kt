package com.washathomes.apputils.modules

data class CategoryItems(val results: ArrayList<CategoryItem>)

data class CategoryItem(val id: String, val title: String, val image: String, val status: String, val favorite: String, var price: Double, val items_service: ArrayList<ItemService>, var quantity: Int = 0, var selectedServices: ArrayList<ItemService> = ArrayList(), var notes: String = "")

data class ItemService(val id: String, val title: String, val price: Double, val status: String, var checked: Boolean = false)

data class CategoryItemsObj(val category_id: String, val lat: String, val long: String, val zip_code: String)
