package com.arny.mvpclean.data.models

data class ScheduleData(var isWork: Boolean = false) {
    var time: String? = null
    var isRepeat: Boolean = false
    var periodType: Int? = null
}