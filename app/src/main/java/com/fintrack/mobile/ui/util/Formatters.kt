package com.fintrack.mobile.ui.util

import androidx.compose.ui.graphics.Color
import com.fintrack.mobile.ui.theme.SupermarketCarrefourAccent
import com.fintrack.mobile.ui.theme.SupermarketCarrefourBg
import com.fintrack.mobile.ui.theme.SupermarketCotoAccent
import com.fintrack.mobile.ui.theme.SupermarketCotoBg
import com.fintrack.mobile.ui.theme.SupermarketDefaultAccent
import com.fintrack.mobile.ui.theme.SupermarketDefaultBg
import com.fintrack.mobile.ui.theme.SupermarketJumboAccent
import com.fintrack.mobile.ui.theme.SupermarketJumboBg
import java.text.DateFormat
import java.text.NumberFormat
import java.util.Currency
import java.util.Date
import java.util.Locale
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.Calendar

fun formatCurrency(cents: Long, currencyCode: String): String {
    val locale = if (currencyCode == "ARS") {
        Locale.Builder().setLanguage("es").setRegion("AR").build()
    } else {
        Locale.US
    }
    val format = NumberFormat.getCurrencyInstance(locale)
    format.currency = Currency.getInstance(currencyCode)
    return format.format(cents / 100.0)
}

fun formatDate(dateMillis: Long): String {
    val locale = Locale.Builder().setLanguage("es").setRegion("AR").build()
    val formatter = DateFormat.getDateInstance(DateFormat.MEDIUM, locale)
    return formatter.format(Date(dateMillis))
}

fun formatTime(dateMillis: Long): String {
    val locale = Locale.Builder().setLanguage("es").setRegion("AR").build()
    val formatter = DateFormat.getTimeInstance(DateFormat.SHORT, locale)
    return formatter.format(Date(dateMillis))
}

fun parseCents(input: String): Long {
    val normalized = input.trim().replace(",", ".")
    if (normalized.isBlank()) return 0L
    return try {
        val value = BigDecimal(normalized).setScale(2, RoundingMode.HALF_UP)
        value.movePointRight(2).toLong()
    } catch (_: Exception) {
        0L
    }
}

fun updateDateMillis(currentMillis: Long, year: Int, month: Int, dayOfMonth: Int): Long {
    val calendar = Calendar.getInstance().apply { timeInMillis = currentMillis }
    calendar.set(Calendar.YEAR, year)
    calendar.set(Calendar.MONTH, month)
    calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
    return calendar.timeInMillis
}

fun updateTimeMillis(currentMillis: Long, hourOfDay: Int, minute: Int): Long {
    val calendar = Calendar.getInstance().apply { timeInMillis = currentMillis }
    calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
    calendar.set(Calendar.MINUTE, minute)
    calendar.set(Calendar.SECOND, 0)
    calendar.set(Calendar.MILLISECOND, 0)
    return calendar.timeInMillis
}

data class SupermarketColors(
    val bgColor: Color,
    val accentColor: Color,
    val logoRes: Int?
)

fun getSupermarketColors(supermarketName: String): SupermarketColors {
    return when {
        supermarketName.contains("Carrefour", ignoreCase = true) ->
            SupermarketColors(
                bgColor = SupermarketCarrefourBg,
                accentColor = SupermarketCarrefourAccent,
                logoRes = com.fintrack.mobile.R.drawable.logo_carrefour
            )
        supermarketName.contains("Coto", ignoreCase = true) ->
            SupermarketColors(
                bgColor = SupermarketCotoBg,
                accentColor = SupermarketCotoAccent,
                logoRes = com.fintrack.mobile.R.drawable.logo_coto
            )
        supermarketName.contains("Jumbo", ignoreCase = true) ->
            SupermarketColors(
                bgColor = SupermarketJumboBg,
                accentColor = SupermarketJumboAccent,
                logoRes = com.fintrack.mobile.R.drawable.logo_jumbo
            )
        else ->
            SupermarketColors(
                bgColor = SupermarketDefaultBg,
                accentColor = SupermarketDefaultAccent,
                logoRes = null
            )
    }
}
