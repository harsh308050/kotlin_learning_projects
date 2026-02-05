package com.harsh.shopit.main.utils

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object Prefs {

    private const val PREF_NAME = "shopit_prefs"

    fun prefs(context: Context) =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    // -------- STRING --------
    fun putString(context: Context, key: String, value: String) {
        prefs(context).edit().putString(key, value).apply()
    }

    fun getString(context: Context, key: String, default: String? = null): String? {
        return prefs(context).getString(key, default)
    }




    // -------- JSON --------
    fun <T> putObject(context: Context, key: String, value: T) {
        val gson = Gson()
        val json = gson.toJson(value)
        prefs(context).edit().putString(key, json).apply()
    }
     inline fun <reified T> getObject(context: Context, key: String): T? {
        val json = prefs(context).getString(key, null) ?: return null
        val gson = Gson()
        return gson.fromJson(json, T::class.java)
    }



    // -------- INT --------
    fun putInt(context: Context, key: String, value: Int) {
        prefs(context).edit().putInt(key, value).apply()
    }

    fun getInt(context: Context, key: String, default: Int = -1): Int {
        return prefs(context).getInt(key, default)
    }

    // -------- BOOLEAN --------
    fun putBoolean(context: Context, key: String, value: Boolean) {
        prefs(context).edit().putBoolean(key, value).apply()
    }

    fun getBoolean(context: Context, key: String, default: Boolean = false): Boolean {
        return prefs(context).getBoolean(key, default)
    }

    // -------- CLEAR --------
    fun remove(context: Context, key: String) {
        prefs(context).edit().remove(key).apply()
    }

    fun clear(context: Context) {
        prefs(context).edit().clear().apply()
    }
}
