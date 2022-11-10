package com.washathomes.apputils.modules

data class Status(val massage: String?, val code: String?)

data class ErrorResponse(val status: Status)

data class BooleanResponse(val status: Status, val results: Boolean)
