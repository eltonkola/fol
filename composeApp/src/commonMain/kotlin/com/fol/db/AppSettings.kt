package com.fol.com.fol.db

import com.russhwolf.settings.Settings

class AppSettings(private val settings: Settings) {
    
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
    }

    fun isSystemTheme(): Boolean {
        return settings.getBoolean(SYSTEM_THEME, true)
    }

    fun setSystemTheme(value: Boolean) {
        settings.putBoolean(SYSTEM_THEME, value)
    }

}
