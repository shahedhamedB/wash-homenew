package com.washathomes.base.util

import android.icu.util.Calendar
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created on 2019-10-21.
 */

class DateUtil {

    companion object {

        fun convertLongToTimeString(time: Long): String {
            val date = Date(time)
            val format = SimpleDateFormat("yyyy.MM.dd HH:mm", Locale.US)
            return format.format(date)
        }

        fun isSameDay(date1: Date, date2: Date): Boolean {
            val fmt = SimpleDateFormat("yyyyMMdd", Locale.US)
            return fmt.format(date1) == fmt.format(date2)
        }

        fun daysLater(date: Date, days: Int): Date {
            val cal = Calendar.getInstance()
            cal.time = date
            cal.add(Calendar.DAY_OF_YEAR, days)
            return cal.time
        }
//
//        fun currentTimeToLong(): Long {
//            return System.currentTimeMillis()
//        }
//
//        fun convertDateToLong(date: String): Long {
//            val df = SimpleDateFormat("yyyy.MM.dd HH:mm")
//            return df.parse(date).time
//        }
    }

}