package com.fintrack.mobile.ui.util

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.text.format.DateFormat
import java.util.Calendar

/**
 * showDatePicker: Muestra un diálogo de selección de fecha y ejecuta el callback con los nuevos millis.
 */
fun showDatePicker(context: Context, currentMillis: Long, onDateSelected: (Long) -> Unit) {
    val cal = Calendar.getInstance().apply { timeInMillis = currentMillis }
    DatePickerDialog(
        context,
        { _, y, m, d -> onDateSelected(updateDateMillis(currentMillis, y, m, d)) },
        cal.get(Calendar.YEAR),
        cal.get(Calendar.MONTH),
        cal.get(Calendar.DAY_OF_MONTH)
    ).show()
}

/**
 * showTimePicker: Muestra un diálogo de selección de hora y ejecuta el callback con los nuevos millis.
 */
fun showTimePicker(context: Context, currentMillis: Long, onTimeSelected: (Long) -> Unit) {
    val cal = Calendar.getInstance().apply { timeInMillis = currentMillis }
    TimePickerDialog(
        context,
        { _, h, m -> onTimeSelected(updateTimeMillis(currentMillis, h, m)) },
        cal.get(Calendar.HOUR_OF_DAY),
        cal.get(Calendar.MINUTE),
        DateFormat.is24HourFormat(context)
    ).show()
}
