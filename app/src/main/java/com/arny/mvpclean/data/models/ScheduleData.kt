package com.arny.mvpclean.data.models

import java.util.concurrent.TimeUnit

data class ScheduleData(var isWork: Boolean = false) {
    var time: String? = null
    var isRepeat: Boolean = false
    var repeatPeriod: Int? = null
    var repeatType: TimeUnit? = null
}