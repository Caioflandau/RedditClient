package com.caiolandau.devigetredditclient.repository.converter

import android.text.format.DateUtils

class EpochToRelativeTimeConverter {
    fun convert(epoch: Double): String {
        val nowUtc = System.currentTimeMillis() // currentTimeMillis always returns UTC timestamps
        return DateUtils.getRelativeTimeSpanString(epoch.toLong()*1000L, nowUtc, 0L).toString()
    }
}