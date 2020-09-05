package com.caiolandau.devigetredditclient.domain.repository.converter

import com.caiolandau.devigetredditclient.util.DateUtilsWrapper
import java.time.Clock

class EpochToRelativeTimeConverter {
    fun convert(
        epochUTCSeconds: Long,
        clock: Clock = Clock.systemUTC(),
        dateUtilsWrapper: DateUtilsWrapper = DateUtilsWrapper()
    ): String {
        val nowUtc = clock.millis()
        return dateUtilsWrapper.getRelativeTimeSpanString(epochUTCSeconds * 1000L, nowUtc, 0L)
    }
}