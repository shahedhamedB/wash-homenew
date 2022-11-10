package com.washathomes.apputils.base.extension

import android.content.Context
import com.washathomes.R
import com.washathomes.apputils.base.WashareServiceException
import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException

fun Throwable.getErrorMessage(context: Context): String {
    return when (this) {
        is WashareServiceException -> generateServiceException(context)
        is HttpException -> context.getString(R.string.error_unknown)
        is SocketTimeoutException -> context.getString(R.string.please_check_your_network_connection_and_try_again)
        is IOException -> context.getString(R.string.error_connection_not_found)
        else -> context.getString(R.string.error_unknown)
    }
}

private fun WashareServiceException.generateServiceException(context: Context): String {
    return error?.message.toString()
}
