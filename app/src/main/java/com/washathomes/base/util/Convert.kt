package com.washathomes.base.util


class Convert {
    companion object {
        fun meterToMillFormat(i: Int): String {
            val mill = i * 0.000621371192
            return String.format("%.2f", mill)
        }
    }
}