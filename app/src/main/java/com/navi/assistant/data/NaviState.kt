package com.navi.assistant.data

import android.content.Context
import android.content.SharedPreferences

/** Status AI Core yang ditampilkan di widget. */
enum class CoreStatus(val label: String) {
    STANDBY("STANDBY"),
    ONLINE_LINK("ONLINE LINK"),
    OFFLINE_LOCAL("OFFLINE LOCAL"),
    BUSY("PROCESSING")
}

/**
 * Status ringan yang dibaca widget dan aplikasi. Disimpan di SharedPreferences
 * agar widget bisa dirender tanpa memulai proses berat apa pun.
 */
object NaviState {
    private const val PREFS = "navi_state"
    private const val KEY_USER_NAME = "user_name"
    private const val KEY_LAST_MESSAGE = "last_message"
    private const val KEY_CORE_STATUS = "core_status"

    const val DEFAULT_USER_NAME = "OPERATOR"
    const val MAX_NAME_LENGTH = 24
    const val MAX_MESSAGE_LENGTH = 160

    private fun prefs(context: Context): SharedPreferences =
        context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun hasUserName(context: Context): Boolean =
        prefs(context).contains(KEY_USER_NAME)

    fun getUserName(context: Context): String =
        prefs(context).getString(KEY_USER_NAME, null) ?: DEFAULT_USER_NAME

    /** Menyimpan nama (dibersihkan dan dipotong). Mengembalikan nilai yang tersimpan. */
    fun setUserName(context: Context, raw: String): String {
        val clean = raw
            .filter { !it.isISOControl() }
            .trim()
            .take(MAX_NAME_LENGTH)
            .uppercase()
        if (clean.isEmpty()) {
            prefs(context).edit().remove(KEY_USER_NAME).apply()
            return DEFAULT_USER_NAME
        }
        prefs(context).edit().putString(KEY_USER_NAME, clean).apply()
        return clean
    }

    fun getLastMessage(context: Context, fallback: String): String =
        prefs(context).getString(KEY_LAST_MESSAGE, null) ?: fallback

    fun setLastMessage(context: Context, message: String) {
        val clean = message.replace('\n', ' ').trim().take(MAX_MESSAGE_LENGTH)
        prefs(context).edit().putString(KEY_LAST_MESSAGE, clean).apply()
    }

    fun getCoreStatus(context: Context): CoreStatus {
        val name = prefs(context).getString(KEY_CORE_STATUS, null)
        return CoreStatus.entries.firstOrNull { it.name == name } ?: CoreStatus.STANDBY
    }

    fun setCoreStatus(context: Context, status: CoreStatus) {
        prefs(context).edit().putString(KEY_CORE_STATUS, status.name).apply()
    }
}
