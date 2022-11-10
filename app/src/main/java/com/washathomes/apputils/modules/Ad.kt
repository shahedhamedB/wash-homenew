package com.washathomes.apputils.modules

data class Ad(val id: String, val image: String, val status: String)

data class Ads(val status: Status, val results: ArrayList<Ad>)
