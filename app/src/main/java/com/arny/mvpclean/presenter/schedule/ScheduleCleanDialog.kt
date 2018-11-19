package com.arny.mvpclean.presenter.schedule

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.AdapterView
import com.arny.mvpclean.R
import com.arny.mvpclean.data.adapters.AbstractDialogBuilder
import com.arny.mvpclean.data.dialogs.ChoiseDialogListener
import com.arny.mvpclean.data.dialogs.checkDialog
import com.arny.mvpclean.data.models.ScheduleData
import com.redmadrobot.inputmask.MaskedTextChangedListener
import kotlinx.android.synthetic.main.dialog_repeat_layout.view.*


class ScheduleCleanDialog(context: Context, private var onSheduleListener: OnSheduleListener) : AbstractDialogBuilder(context) {
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
        view.apply {
            setCancelable(false)
            setPositiveButton("ОК") { _, i ->
                scheduleData = ScheduleData()
                scheduleData?.isWork = true
                scheduleData?.time = editTime?.text.toString()
                scheduleData?.isRepeat = checkRepeat?.isChecked ?: false
                scheduleData?.repeatPeriod = count
                scheduleData?.periodType = periodType
                onSheduleListener.onSheduleSet(scheduleData)
            }
            setNegativeButton("Отмена", { _, _ -> })
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
    }

    override fun getTitle(): String {
        return "Расписание очистки каталогов"
    }

    override fun getView(): View {
        return LayoutInflater.from(context).inflate(R.layout.dialog_repeat_layout, null)
    }

    override fun updateDialogView(view: View) {
        view.apply {
            time = editTime?.text.toString()
            count = editRepeatCount?.text.toString().toInt()
            checkRepeat?.setOnCheckedChangeListener { _, checked ->
                val enabl = if (checked) "вкл" else "выкл"
                checkRepeat?.text = "Повтор $enabl"
                if (checked) {
                    editRepeatCount?.isEnabled = true
                    spinnerRepeatType?.isEnabled = true
                }
            }
            spinnerRepeatType?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(p0: AdapterView<*>?) {
                }

                override fun onItemSelected(p0: AdapterView<*>?, p1: View?, position: Int, p3: Long) {
                    periodType = position
                }
            }
        }
    }
}
