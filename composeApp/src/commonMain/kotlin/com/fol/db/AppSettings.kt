package com.fol.com.fol.db

import co.touchlab.kermit.Logger
import com.russhwolf.settings.Settings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update


data class ThemeState(
    val system: Boolean,
    val dark: Boolean
)

class AppSettings(private val settings: Settings) {

    private val _themeState = MutableStateFlow(ThemeState(isDarkTheme(), isSystemTheme()))
    val themeState: StateFlow<ThemeState> = _themeState.asStateFlow()

    companion object {
        const val USER_CREATED = "USER_CREATED"
        const val DARK_THEME = "dark_theme"
        const val SYSTEM_THEME = "system_theme"
        const val ACCEPTED_TERMS = "ACCEPTED_TERMS"
    }

    fun accountExists() : Boolean {
        val accountExists =  settings.getBoolean(USER_CREATED, false)
        Logger.i { "accountExists: $accountExists" }
        return accountExists
    }

    fun setAccountExists(created: Boolean) {
        Logger.i { "setAccountExists: $created" }
        settings.putBoolean(USER_CREATED, created)
    }

    fun acceptedTerms() : Boolean {
        val acceptedTerms =  settings.getBoolean(ACCEPTED_TERMS, false)
        Logger.i { "acceptedTerms: $acceptedTerms" }
        return acceptedTerms
    }

    fun setAcceptTerms(accepted: Boolean) {
        Logger.i { "setAcceptTerms: $accepted" }
        settings.putBoolean(ACCEPTED_TERMS, accepted)
    }

    fun isDarkTheme(): Boolean {
        return settings.getBoolean(DARK_THEME, false)
    }

    fun setDarkTheme(value: Boolean) {
        settings.putBoolean(DARK_THEME, value)
        _themeState.update { it.copy(dark = value) }
    }

    fun isSystemTheme(): Boolean {
        return settings.getBoolean(SYSTEM_THEME, true)
    }

    fun setSystemTheme(value: Boolean) {
        settings.putBoolean(SYSTEM_THEME, value)
        _themeState.update { it.copy(system = value) }
    }

}
