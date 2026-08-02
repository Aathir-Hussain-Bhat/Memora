package com.example.data

import android.content.Context
import android.content.SharedPreferences

class SettingsRepository(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("sparky_prefs", Context.MODE_PRIVATE)

    var openRouterApiKey: String
        get() = prefs.getString("openRouterApiKey", "") ?: ""
        set(value) = prefs.edit().putString("openRouterApiKey", value).apply()

    var modelName: String
        get() = prefs.getString("modelName", "deepseek/deepseek-r1:free") ?: "deepseek/deepseek-r1:free"
        set(value) = prefs.edit().putString("modelName", value).apply()

    var mockMode: Boolean
        get() = prefs.getBoolean("mockMode", false)
        set(value) = prefs.edit().putBoolean("mockMode", value).apply()
}
