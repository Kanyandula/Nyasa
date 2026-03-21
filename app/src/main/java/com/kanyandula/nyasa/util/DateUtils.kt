package com.kanyandula.nyasa.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object DateUtils {

    companion object {

        // dates from server look like this: "2019-07-23T03:28:01.406944Z"
        fun convertServerStringDateToLong(sd: String): Long {
            var stringDate = sd.removeRange(sd.indexOf("T") until sd.length)
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
            val time = sdf.parse(stringDate).time
            return time
        }

        fun convertLongToStringDate(longDate: Long): String {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
            val date = sdf.format(Date(longDate))
            return date
        }
    }
}
