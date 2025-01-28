package org.intelehealth.app.ui.dialog

import android.app.DatePickerDialog
import android.app.DatePickerDialog.OnDateSetListener
import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.widget.DatePicker
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentManager
import org.intelehealth.app.R
import org.intelehealth.klivekit.utils.DateTimeUtils
import java.util.Calendar
import java.util.TimeZone

/**
 * Created by Vaghela Mithun R. on 11-07-2024 - 15:59.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/
class CalendarDialog private constructor() : AppCompatDialogFragment(), OnDateSetListener {
    private var maxDate: Long = 0
    private var minDate: Long = 0
    private var selectedDate: Long = Calendar.getInstance().timeInMillis
    private var format: String = "MMM dd, yyyy"
    private lateinit var listener: OnDatePickListener

    interface OnDatePickListener {
        fun onDatePick(day: Int, month: Int, year: Int, value: String?)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = selectedDate
        val year = calendar[Calendar.YEAR]
        val month = calendar[Calendar.MONTH]
        val day = calendar[Calendar.DAY_OF_MONTH]
        val datePickerDialog = DatePickerDialog(
            requireContext(),
            this, year, month, day
        ).apply {
            if (maxDate > 0) datePicker.maxDate = maxDate
            if (minDate > 0) datePicker.minDate = minDate
        }

        datePickerDialog.setOnShowListener {
            updateButtonTheme(datePickerDialog)
        }

        return datePickerDialog
    }

    private fun updateButtonTheme(datePickerDialog: DatePickerDialog) {
        datePickerDialog.getButton(DatePickerDialog.BUTTON_POSITIVE)
            .setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.colorAccent
                )
            ) // Change to your desired color

        datePickerDialog.getButton(DatePickerDialog.BUTTON_NEGATIVE)
            .setTextColor(ContextCompat.getColor(requireContext(), R.color.colorAccent))
    }

    override fun onDateSet(datePicker: DatePicker?, year: Int, month: Int, day: Int) {
        val calendar = Calendar.getInstance()
        calendar[Calendar.YEAR] = year
        calendar[Calendar.MONTH] = month
        calendar[Calendar.DAY_OF_MONTH] = day

        val sdf = DateTimeUtils.getSimpleDateFormat(format, TimeZone.getDefault())
        val formattedDate = sdf.format(calendar.time)

        if (::listener.isInitialized) listener.onDatePick(day, month, year, formattedDate)
    }

    class Builder {
        private val calendarDialog = CalendarDialog()

        fun maxDate(max: Long): Builder {
            calendarDialog.maxDate = max
            return this
        }

        fun minDate(min: Long): Builder {
            calendarDialog.minDate = min
            return this
        }

        fun selectedDate(selected: Long): Builder {
            calendarDialog.selectedDate = selected
            return this
        }

        fun format(format: String): Builder {
            calendarDialog.format = format
            return this
        }

        fun listener(listener: OnDatePickListener): Builder {
            calendarDialog.listener = listener
            return this
        }

        fun build() = calendarDialog
    }

    companion object {
        const val TAG = "CalendarDialog"

          fun showDatePickerDialog(listener: OnDatePickListener ,childFragmentManager : FragmentManager) {
            Builder()
                .maxDate(Calendar.getInstance().timeInMillis)
                .selectedDate(Calendar.getInstance().timeInMillis)
                .format(DateTimeUtils.MMM_DD_YYYY_FORMAT)
                .listener(listener)
                .build().show(childFragmentManager, TAG)
        }
    }
}