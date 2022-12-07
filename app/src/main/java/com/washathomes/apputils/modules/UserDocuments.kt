package com.washathomes.apputils.modules

data class UserDocuments(val id: String, val user_id: String, val vehicle_image_id: String, val vehicle_licenses: String, val vehicle_image: String, val machine_image: String, val dryer_image: String, val extra_image: String)

data class UserDocs(val status: Status, val results: UserDocuments?)
