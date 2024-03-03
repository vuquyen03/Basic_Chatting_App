package com.example.chattingapp.util

import android.content.Context
import android.content.SharedPreferences

class PreferenceManager(context: Context) {
    private var sharedPreferences: SharedPreferences

    init {
        sharedPreferences = context .getSharedPreferences(Constants.KEY_PREFERENCE_NAME, Context.MODE_PRIVATE)
    }

    private fun getBoolean(key: String): Boolean {
        return sharedPreferences.getBoolean(key, false)
    }

    private fun putBoolean(key: String, value: Boolean) {
        val editor = sharedPreferences.edit()
        editor.putBoolean(key, value)
        editor.apply()
    }

    private fun putString(key: String, value: String) {
        val editor = sharedPreferences.edit()
        editor.putString(key, value)
        editor.apply()
    }

    fun getString(key: String): String? {
        return sharedPreferences.getString(key, null)
    }

    private fun clear() {
        val editor = sharedPreferences.edit()
        editor.clear()
        editor.apply()
    }
}