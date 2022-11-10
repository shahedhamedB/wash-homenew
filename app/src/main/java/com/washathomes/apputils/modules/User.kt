package com.washathomes.apputils.modules

data class UserLogin(val phone: String?, val language_code: String?, val fcm_token: String?, val type_token: String?)
data class UserData(val token: String?, val results: User?)
data class User (val id: String?, val name: String?, val email:String?, val phone: String?,
        val gender: String?, val birthdate: String?, val image: String?, val id_image: String?,
        val sigup_type: String?, val washee_status: String?, val washer_status: String?, val courier_status: String?,
        var washer_available: String?, val express: String?, var dreiver_available: String?, val dreiver_type: String?,
        val dreiver_miles: String?, val language_code: String?, val date_created: String?, val modified_date: String?,
        val sign_date: String?, val countryId: String?, val latitude: String?, val longitude: String?, val address: String?,
        val zip_code: String?, val fcm_token: String?, val status: String?)

data class UpdateUser(val name: String?, val phone: String?, val email: String?, val gender: String?, val birthdate: String?,
val latitude: String?, val longitude: String?, val address: String?, val zip_code: String?, val user_type: String?, val image: String?
, val id_image: String?)

data class UpdateDrivingData(val dreiver_available: String?, val dreiver_type: String?, val dreiver_miles: String?, val vehicle_image_id: String?
, val vehicle_licenses: String?, val vehicle_image: String?)

data class UpdateServicesData(val washer_available: String?, val express: String?, val servcie: ArrayList<ServiceAvailable>, val machine_image: String?
, val dryer_image: String?, val extra_image: String?)