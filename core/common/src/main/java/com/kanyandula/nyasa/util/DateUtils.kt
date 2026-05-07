package com.kanyandula.nyasa.util

import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object DateUtils {

    // dates from server look like this: "2019-07-23T03:28:01.406944Z"
    fun convertServerStringDateToLong(sd: String): Long {
        val datePart = sd.substringBefore("T")
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
        return try {
            sdf.parse(datePart)?.time ?: 0L
        } catch (e: ParseException) {
            0L
        }
    }

    fun convertLongToStringDate(longDate: Long): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
        return sdf.format(Date(longDate))
    }
}