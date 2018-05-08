package com.arny.mvpclean.presenter.schedule

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.AdapterView
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Spinner
import com.arny.arnylib.adapters.ADBuilder
import com.arny.arnylib.interfaces.ChoiseDialogListener
import com.arny.arnylib.utils.checkDialog
import com.arny.mvpclean.R
import com.arny.mvpclean.data.models.ScheduleData
import com.redmadrobot.inputmask.MaskedTextChangedListener


class ScheduleCleanDialog(context: Context, private var onSheduleListener: OnSheduleListener) : ADBuilder(context) {
    private var checkRepeat: CheckBox? = null
    private var editRepeatCount: EditText? = null
    private var spinnerRepeatType: Spinner? = null
    private var editTime: EditText? = null
    private var checkWeeks: CheckBox? = null
    private var time: String? = null
    private var count: Int = 0
    private var periodType: Int? = null
    private var scheduleData: ScheduleData? = null
    private var weekDaysSelected: Array<Int>? = null
    private var dateTimeListener: MaskedTextChangedListener? = null

    interface OnSheduleListener {
        fun onSheduleSet(scheduleData: ScheduleData?)
    }

    override fun initUI(view: View) {
        checkRepeat = view.findViewById(R.id.checkRepeat)
        editRepeatCount = view.findViewById(R.id.editRepeatCount)
        spinnerRepeatType = view.findViewById(R.id.spinnerRepeatType)
        editTime = view.findViewById(R.id.editTime)
        checkWeeks = view.findViewById(R.id.checkWeeks)
        super.setCancelable(false)
        super.setPositiveButton("ОК", { _, i ->
            scheduleData = ScheduleData()
            scheduleData?.isWork = true
            scheduleData?.time = editTime?.text.toString()
            scheduleData?.isRepeat = checkRepeat?.isChecked ?: false
            scheduleData?.isWeedDays = checkWeeks?.isChecked ?: false
            scheduleData?.repeatPeriod = count
            scheduleData?.periodType = periodType
            scheduleData?.weekDays = weekDaysSelected
            onSheduleListener.onSheduleSet(scheduleData)
        })
        super.setNegativeButton("Отмена", { _, _ -> })
        if (editTime != null) {
            dateTimeListener = MaskedTextChangedListener(
                    "[00]:[00]",
                    emptyList(),
                    false,
                    editTime!!,
                    null, null)
        }
        editTime?.addTextChangedListener(dateTimeListener)
    }

    override fun getTitle(): String {
        return "Расписание очистки каталогов"
    }

    override fun getView(): View {
        return LayoutInflater.from(context).inflate(R.layout.dialog_repeat_layout, null)
    }

    override fun updateDialogView() {
        time = editTime?.text.toString()
        count = editRepeatCount?.text.toString().toInt()
        checkRepeat?.setOnCheckedChangeListener({ _, checked ->
            val enabl = if (checked) "вкл" else "выкл"
            checkRepeat?.text = "Повтор $enabl"
            if (checked) {
                checkWeeks?.isChecked = false
                editRepeatCount?.isEnabled = true
                spinnerRepeatType?.isEnabled = true
            }
        })
        spinnerRepeatType?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(p0: AdapterView<*>?) {
            }

            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, position: Int, p3: Long) {
                periodType = position
            }
        }
        checkWeeks?.setOnCheckedChangeListener({ _, checked ->
            if (checked) {
                editRepeatCount?.isEnabled = false
                spinnerRepeatType?.isEnabled = false
                checkRepeat?.isChecked = false
                val weekDays = arrayOf("ПН", "ВТ", "СР", "ЧТ", "ПТ", "СБ", "ВС")
                checkDialog(context, "Дни недели", weekDays, dialogListener = ChoiseDialogListener {
                    weekDaysSelected = it
                    val wDays = StringBuilder()
                    weekDays.forEachIndexed { index, s ->
                        val hasIndex = weekDaysSelected?.contains(index) ?: false
                        if (hasIndex) {
                            wDays.append(s)
                            if (index != weekDays.size - 1) {
                                wDays.append(",")
                            }
                        }
                    }
                    checkWeeks?.text = "Дни недели $wDays"
                })
            }
        })

    }
}
