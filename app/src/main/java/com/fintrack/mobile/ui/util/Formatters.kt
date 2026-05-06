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

data class SupermarketColors(
    val bgColor: androidx.compose.ui.graphics.Color,
    val accentColor: androidx.compose.ui.graphics.Color,
    val logoRes: Int?
)

fun getSupermarketColors(supermarketName: String): SupermarketColors {
    return when {
        supermarketName.contains("Carrefour", ignoreCase = true) ->
            SupermarketColors(
                bgColor = androidx.compose.ui.graphics.Color(0xFFEEF6FF),
                accentColor = androidx.compose.ui.graphics.Color(0xFF5FA8E6),
                logoRes = com.fintrack.mobile.R.drawable.logo_carrefour
            )
        supermarketName.contains("Coto", ignoreCase = true) ->
            SupermarketColors(
                bgColor = androidx.compose.ui.graphics.Color(0xFFFFF1F1),
                accentColor = androidx.compose.ui.graphics.Color(0xFFEB8A8A),
                logoRes = com.fintrack.mobile.R.drawable.logo_coto
            )
        supermarketName.contains("Jumbo", ignoreCase = true) ->
            SupermarketColors(
                bgColor = androidx.compose.ui.graphics.Color(0xFFEDF8EF),
                accentColor = androidx.compose.ui.graphics.Color(0xFF7BCB85),
                logoRes = com.fintrack.mobile.R.drawable.logo_jumbo
            )
        else ->
            SupermarketColors(
                bgColor = androidx.compose.ui.graphics.Color(0xFFF6FAFB),
                accentColor = androidx.compose.ui.graphics.Color(0xFF7A8C93),
                logoRes = null
            )
    }
}
