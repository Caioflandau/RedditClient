package com.caiolandau.devigetredditclient.util

import android.text.format.DateUtils

// A simple class wrapping "DateUtils" static methods
class DateUtilsWrapper {
    fun getRelativeTimeSpanString(time: Long, now: Long, minResolution: Long) =
        DateUtils.getRelativeTimeSpanString(time, now, minResolution).toString()
}