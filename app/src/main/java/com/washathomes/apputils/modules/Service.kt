package com.washathomes.apputils.modules

data class Services(val results: ArrayList<Service>)

data class Service(val id: String?, val title: String?, val icon: String?, val icon_selected: String?, val status: String?, var isSelected: Boolean? = false)

data class ServiceAvailable(val servcie_id: String?)
