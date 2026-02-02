package com.dnavarro.turismoapp.data

import android.content.Context
import android.content.SharedPreferences

import android.util.Base64
import org.json.JSONObject

object SessionManager {
        fun getUserIdFromToken(token: String): String? {
            try {
                val parts = token.split(".")
                if (parts.size < 2) return null
                val payload = String(Base64.decode(parts[1], Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING))
                val json = JSONObject(payload)
                // El campo puede ser "userId" o "sub" según tu backend
                return json.optString("userId", json.optString("sub", null))
            } catch (e: Exception) {
                return null
            }
        }
    private const val PREF_NAME = "session"
    private const val KEY_TOKEN = "token"

    private fun getPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    fun saveToken(context: Context, token: String) {
        val editor = getPreferences(context).edit()
        editor.putString(KEY_TOKEN, token)
        editor.apply()
    }

    fun getToken(context: Context): String? {
        return getPreferences(context).getString(KEY_TOKEN, null)
    }

    fun clearSession(context: Context) {
        val editor = getPreferences(context).edit()
        editor.clear()
        editor.apply()
    }
}