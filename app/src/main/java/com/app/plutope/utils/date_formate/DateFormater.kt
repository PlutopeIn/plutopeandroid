package com.app.plutope.utils.date_formate

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Context
import android.content.res.Resources
import android.view.View
import android.view.ViewGroup
import com.app.plutope.R
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale


const val ymdHMS = "yyyy-MM-dd HH:mm:ss"
const val ymd = "yyyy-MM-dd"
const val dmyhm = "dd MMM yyyy hh:mm"

fun String.toCal(
    sourceFormat: String = ymdHMS,
): Calendar? {
    return try {
        val calendar = Calendar.getInstance()
        val sdf = SimpleDateFormat(sourceFormat, Locale.getDefault())
        val temp = sdf.parse(this)
        if (temp != null) {
            calendar.time = temp
        }
        calendar
    } catch (e: java.lang.Exception) {
        e.printStackTrace()
        null
    }
}

fun Calendar.toAny(
    toFormat: String = ymdHMS,
): String {
    val sdf = SimpleDateFormat(toFormat, Locale.getDefault())
    val result = sdf.format(this.time)
    return result.replace("pm", "PM").replace("am", "AM")
}

fun Context.showDatePicker(
    onDateSetListener: (date: Calendar) -> Unit,
    selectedDate: Calendar? = null,
    minDate: Calendar? = null,
    maxDate: Calendar? = null,
    hideDayFromPicker: Boolean = false,
    hideMonthFromPicker: Boolean = false,
    hideYearFromPicker: Boolean = false,
) {
    val style =
        if (hideDayFromPicker || hideMonthFromPicker || hideYearFromPicker) AlertDialog.THEME_HOLO_LIGHT else R.style.Theme_ADrive_DatePickerDialog
    val calendar: Calendar = selectedDate ?: Calendar.getInstance()
    val datePickerDialog = DatePickerDialog(
        this, style, { _, year, month, dayOfMonth ->
            calendar.set(Calendar.YEAR, year)
            calendar.set(Calendar.MONTH, month)
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            onDateSetListener(calendar)

        },
        calendar[Calendar.YEAR],
        calendar[Calendar.MONTH],
        calendar[Calendar.DAY_OF_MONTH]
    )

    datePickerDialog.datePicker.let { dp ->
        if (hideDayFromPicker)
            dp.findViewById<ViewGroup>(
                Resources.getSystem().getIdentifier("day", "id", "android")
            ).visibility =
                View.GONE
        if (hideMonthFromPicker)
            dp.findViewById<ViewGroup>(
                Resources.getSystem().getIdentifier("month", "id", "android")
            ).visibility =
                View.GONE
        if (hideYearFromPicker)
            dp.findViewById<ViewGroup>(
                Resources.getSystem().getIdentifier("year", "id", "android")
            ).visibility =
                View.GONE
        minDate?.let { it -> dp.minDate = it.timeInMillis }
        maxDate?.let { it -> dp.maxDate = it.timeInMillis }
    }

    datePickerDialog.show()
}
fun Long.getReadableDate(): String {
    val timestampSeconds = this / 1000
    val date = Date(timestampSeconds * 1000) // Convert seconds to milliseconds
    val dateFormatter = SimpleDateFormat()

    val now = Date()
    val today = Calendar.getInstance().apply { time = now }
    val target = Calendar.getInstance().apply { time = date }

    return if (isSameDay(today, target)) {
        dateFormatter.applyPattern("h:mm a")
        dateFormatter.format(date)
    } else if (isYesterday(today, target)) {
        "Yesterday"
    } else if (isTomorrow(today, target)) {
        "Tomorrow"
    } else if(dateFallsInCurrentWeek(today,target)){
        dateFormatter.applyPattern("EEEE")
        dateFormatter.format(date)
    }else {
        dateFormatter.applyPattern("MMM d, yyyy")
        dateFormatter.format(date)
    }
}

fun isSameDay(cal1: Calendar, cal2: Calendar): Boolean {
    return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
            cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
}

fun isYesterday(today: Calendar, target: Calendar): Boolean {
    return (today.get(Calendar.YEAR) == target.get(Calendar.YEAR) &&
            today.get(Calendar.DAY_OF_YEAR) - target.get(Calendar.DAY_OF_YEAR) == 1)
}

fun isTomorrow(today: Calendar, target: Calendar): Boolean {
    return (today.get(Calendar.YEAR) == target.get(Calendar.YEAR) &&
            target.get(Calendar.DAY_OF_YEAR) - today.get(Calendar.DAY_OF_YEAR) == 1)
}

fun dateFallsInCurrentWeek(today: Calendar, target: Calendar): Boolean {
    val currentWeek = today.get(Calendar.WEEK_OF_YEAR)
    val datesWeek = target.clone() as Calendar // Create a copy of the target Calendar

    val isInCurrentWeek = (currentWeek == datesWeek.get(Calendar.WEEK_OF_YEAR))

    return isInCurrentWeek
}