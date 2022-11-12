package com.washathomes.retrofit.remote

data class Response<T>(

    val data: T,
    val msg: String,
    val new_user: Int?,
    val status: Int,
    val token: String
)
