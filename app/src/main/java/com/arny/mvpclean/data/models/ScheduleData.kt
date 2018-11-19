package com.arny.mvpclean.data.models

data class ScheduleData(var isWork: Boolean = false) {
    var time: String? = null
    var isRepeat: Boolean = false
    var repeatPeriod: Int? = null
    var periodType: Int? = null
}