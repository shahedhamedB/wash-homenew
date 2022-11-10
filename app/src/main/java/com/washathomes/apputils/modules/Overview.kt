package com.washathomes.apputils.modules

data class PromoCodeObj(val code: String, val lat: String, val long: String, val zip_code: String)

data class PromoCode(val id: String, val code: String, val date_end: String, val type: String, val discount: String, val status: String)

data class PromoCodeResponse(val status: Status, val results: PromoCode)

data class OverviewObj(val promotion_id: String, val lat: String, val long: String, val zip_code: String)
