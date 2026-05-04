package com.fintrack.mobile.data.preferences

data class UserPreferences(
    val displayName: String,
    val email: String,
    val currencyCode: String,
    val darkTheme: Boolean,
    val isLoggedIn: Boolean
) {
    companion object {
        val DEFAULT = UserPreferences(
            displayName = "",
            email = "",
            currencyCode = "ARS",
            darkTheme = false,
            isLoggedIn = false
        )
    }
}
