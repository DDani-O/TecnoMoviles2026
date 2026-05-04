package com.fintrack.mobile.ui.util

import java.text.DateFormat
import java.text.NumberFormat
import java.util.Currency
import java.util.Date
import java.util.Locale
import java.math.BigDecimal
import java.math.RoundingMode

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
