package com.fol.com.fol.db

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
    }

    fun accountExists() : Boolean {
        return settings.getBoolean(USER_CREATED, false)
    }

    fun setAccountExists(created: Boolean) {
        settings.putBoolean(USER_CREATED, created)
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
