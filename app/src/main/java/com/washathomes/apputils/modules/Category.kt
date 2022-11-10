package com.washathomes.apputils.modules

data class Categories(val results: ArrayList<Category>)

data class Category(val id: String, val title: String, val status: String)
