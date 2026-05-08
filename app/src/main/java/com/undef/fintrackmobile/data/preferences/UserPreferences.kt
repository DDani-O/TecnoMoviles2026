package com.undef.fintrackmobile.data.preferences

data class UserPreferences(
    val displayName: String,
    val lastName: String = "",
    val email: String,
    val birthDate: String = "",
    val currencyCode: String,
    val profileImageUri: String? = null,
    val isLoggedIn: Boolean
) {
    companion object {
        val DEFAULT = UserPreferences(
            displayName = "",
            email = "",
            currencyCode = "ARS",
            isLoggedIn = false
        )
    }
}
