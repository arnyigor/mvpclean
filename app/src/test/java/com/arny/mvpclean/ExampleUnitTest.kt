package com.arny.mvpclean

import com.arny.mvpclean.data.repository.utils.getTimeDiff
import org.assertj.core.api.Assertions.assertThat
import org.joda.time.DateTime
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun getTimeDiffT() {
        val now = DateTime.now()
        val plusMinutes = now.plusMinutes(10)
        val h = plusMinutes.hourOfDay
        val min = plusMinutes.minuteOfHour
        val timeDiff = getTimeDiff("$h:$min",0)
        assertThat(timeDiff).isGreaterThan(0)
    }
}
