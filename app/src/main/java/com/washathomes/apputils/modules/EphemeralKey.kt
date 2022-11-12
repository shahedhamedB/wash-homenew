package com.washathomes.apputils.modules

import com.google.gson.annotations.SerializedName

data class EphemeralKey(val id: String, @SerializedName("object") val obj: String, val associated_objects: ArrayList<AssociatedObject>, val created: Int, val expires: Int, val livemode: Boolean, val secret: String)

data class AssociatedObject(val id: String, val type: String)
