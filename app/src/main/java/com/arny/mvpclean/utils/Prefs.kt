package com.arny.mvpclean.utils

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import androidx.content.edit
import com.arny.mvpclean.utils.Prefs.getSettings

object Prefs {
    @Volatile
    private var settings: SharedPreferences? = null

    fun getSettings(context: Context): SharedPreferences? {
        synchronized(Prefs::class.java) {
            if (settings == null) {
                settings = PreferenceManager.getDefaultSharedPreferences(context)
            }
        }
        return settings
    }
}

/**
 * Получение конфига по ключу
 *
 * @param key     Ключ
 * @param context Контекст
 * @return Значение конфига
 */
fun getString(key: String, context: Context): String? {
    return getSettings(context)?.getString(key, null)
}

/**
 * Получение конфига по ключу
 *
 * @param key        Ключ
 * @param context    Контекст
 * @param defaultVal Значение по умолчанию
 * @return Значение конфига
 */
fun getString(key: String, context: Context, defaultVal: String): String? {
    return getSettings(context)?.getString(key, defaultVal)
}

/**
 * Получение конфига по ключу
 *
 * @param key     Ключ
 * @param context Контекст
 * @return Значение конфига
 */
fun getInt(key: String, context: Context): Int {
    return getSettings(context)?.getInt(key, 0) ?: 0
}

fun getLong(key: String, context: Context): Long {
    return getSettings(context)?.getLong(key, 0) ?: 0.toLong()
}

/**
 * Получение конфига по ключу
 *
 * @param key     Ключ
 * @param context Контекст
 * @return Значение конфига
 */
fun getBoolean(key: String, defaultVal: Boolean, context: Context): Boolean {
    return getSettings(context)?.getBoolean(key, defaultVal) ?: defaultVal
}

/**
 * Установка конфига
 *
 * @param key     Ключ
 * @param value   Значение
 * @param context Текущий контекст
 */
fun setString(key: String, value: String, context: Context) {
    getSettings(context)?.edit { putString(key, value) }
}

/**
 * Установка числового конфига
 *
 * @param key     Ключ
 * @param value   Значение
 * @param context Текущий контекст
 */
fun setBoolean(key: String, value: Boolean, context: Context) {
    getSettings(context)?.edit { putBoolean(key, value) }
}

/**
 * Установка числового конфига
 *
 * @param key     Ключ
 * @param value   Значение
 * @param context Текущий контекст
 */
fun setInt(key: String, value: Int?, context: Context) {
    getSettings(context)?.edit { putInt(key, value ?: 0) }
}

fun setLong(key: String, value: Long, context: Context) {
    getSettings(context)?.edit { putLong(key, value) }
}

/**
 * Удаление ключа из конфига
 *
 * @param key     Ключ
 * @param context Контекст
 */
fun remove(key: String, context: Context) {
    getSettings(context)?.edit { remove(key) }
}