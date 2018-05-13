package com.arny.mvpclean.data.utils

import org.joda.time.DateTime

fun getTimeDiff(time: String,nowTime:Long): Long {
    val h = time.split(":".toRegex())[0].toInt()
    val min = time.split(":".toRegex())[1].toInt()
    val next = DateTime.now().withHourOfDay(h).withMinuteOfHour(min)
    val afterNow = next.isAfter(nowTime)
    if (afterNow) {
        val diff = next.minus(nowTime).millis
        return  diff
    }
    next.plusDays(1)
    val diff = next.minus(nowTime).millis
    return diff
}