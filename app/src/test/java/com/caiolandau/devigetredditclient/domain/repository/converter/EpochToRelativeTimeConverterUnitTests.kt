package com.caiolandau.devigetredditclient.domain.repository.converter

import com.caiolandau.devigetredditclient.util.DateUtilsWrapper
import io.mockk.every
import io.mockk.mockk
import org.junit.Test

import org.junit.Assert.*
import java.time.Clock
import java.time.Instant
import java.time.ZoneId

class EpochToRelativeTimeConverterUnitTests {

    @Test
    fun test_convert() {
        val epochSeconds = 1577743200L
        val fixedClock = Clock.fixed(Instant.ofEpochMilli(1577736000000L), ZoneId.of("UTC"))
        val mockWrapper = mockk<DateUtilsWrapper>()
        every {
            mockWrapper.getRelativeTimeSpanString(epochSeconds*1000L, fixedClock.millis(), 0L )
        } returns "2 hours ago"

        val result = EpochToRelativeTimeConverter().convert(epochSeconds, fixedClock, mockWrapper)

        assertEquals("2 hours ago", result)
    }
}