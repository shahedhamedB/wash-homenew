package com.washathomes.apputils.base


open class ServiceException : Throwable {
    constructor()

    constructor(detailMessage: String) : super(detailMessage)
}

class TokenException : Throwable()

class ErrorType {
    var error: String? = null
    var error_description: String? = null

    override fun toString(): String {
        return "$error $error_description"
    }
}


