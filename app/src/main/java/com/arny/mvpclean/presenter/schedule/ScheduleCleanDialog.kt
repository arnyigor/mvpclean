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
import java.util.concurrent.TimeUnit


class ScheduleCleanDialog(context: Context, private var onSheduleListener: OnSheduleListener) : ADBuilder(context) {
    private var checkRepeat: CheckBox? = null
    private var spinnerRepeatType: Spinner? = null
    private var editTime: EditText? = null
    private var time: String? = null
    private var periodType: TimeUnit? = null
    private var periodCount: Int = 0
    private var scheduleData: ScheduleData? = null
    private var dateTimeListener: MaskedTextChangedListener? = null

    interface OnSheduleListener {
        fun onSheduleSet(scheduleData: ScheduleData?)
    }

    override fun initUI(view: View) {
        checkRepeat = view.findViewById(R.id.checkRepeat)
        spinnerRepeatType = view.findViewById(R.id.spinnerRepeatType)
        editTime = view.findViewById(R.id.editTime)
        super.setCancelable(false)
        super.setPositiveButton("ОК") { _, i ->
            scheduleData = ScheduleData()
            scheduleData?.isWork = true
            scheduleData?.time = editTime?.text.toString()
            scheduleData?.isRepeat = checkRepeat?.isChecked ?: false
            scheduleData?.repeatPeriod = periodCount
            scheduleData?.repeatType = periodType
            onSheduleListener.onSheduleSet(scheduleData)
        }
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
        checkRepeat?.setOnCheckedChangeListener { _, checked ->
            val enabl = if (checked) "вкл" else "выкл"
            checkRepeat?.text = "Повтор $enabl"
            if (checked) {
                spinnerRepeatType?.isEnabled = true
            }
        }
        spinnerRepeatType?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(p0: AdapterView<*>?) {
            }

            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, position: Int, p3: Long) {
                when (position) {
                    0 -> {
                        periodCount = 15
                        periodType = TimeUnit.MINUTES
                    }
                    1 -> {
                        periodCount = 30
                        periodType = TimeUnit.MINUTES
                    }
                    2 -> {
                        periodCount = 1
                        periodType = TimeUnit.HOURS
                    }
                    3 -> {
                        periodCount = 3
                        periodType = TimeUnit.HOURS
                    }
                    4 -> {
                        periodCount = 6
                        periodType = TimeUnit.HOURS
                    }
                    5 -> {
                        periodCount = 9
                        periodType = TimeUnit.HOURS
                    }
                    6 -> {
                        periodCount = 12
                        periodType = TimeUnit.HOURS
                    }
                    7 -> {
                        periodCount = 18
                        periodType = TimeUnit.HOURS
                    }
                    8 -> {
                        periodCount = 24
                        periodType = TimeUnit.HOURS
                    }
                }
            }
        }
    }
}
//<item>15мин</item>
//<item>30мин</item>
//<item>1 час</item>
//<item>3 час</item>
//<item>6 час</item>
//<item>9 час</item>
//<item>12 час</item>
//<item>18 час</item>
//<item>24 час</item>