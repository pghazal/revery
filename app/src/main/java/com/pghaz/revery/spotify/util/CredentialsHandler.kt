package com.pghaz.revery.spotify.util

import android.content.Context
import android.content.SharedPreferences
import java.util.concurrent.TimeUnit

/**
 * Taken from: https://github.com/kaaes/spotify-web-api-android
 */
object CredentialsHandler {

    private const val ACCESS_TOKEN_NAME = "com.pghaz.revery.spotify.credentials.access_token"
    private const val ACCESS_TOKEN = "access_token"
    private const val EXPIRES_AT = "expires_at"

    private fun getSharedPreferences(appContext: Context): SharedPreferences {
        return appContext.getSharedPreferences(ACCESS_TOKEN_NAME, Context.MODE_PRIVATE)
    }

    fun setToken(context: Context, token: String?, expiresIn: Int, unit: TimeUnit) {
        val appContext = context.applicationContext
        val now = System.currentTimeMillis()
        val expiresAt = now + unit.toMillis(expiresIn.toLong())
        val sharedPref = getSharedPreferences(appContext)
        val editor = sharedPref.edit()
        editor.putString(ACCESS_TOKEN, token)
        editor.putLong(EXPIRES_AT, expiresAt)
        editor.apply()
    }

    fun getToken(context: Context): String? {
        val appContext = context.applicationContext
        val sharedPref = getSharedPreferences(appContext)
        val token = sharedPref.getString(ACCESS_TOKEN, null)
        val expiresAt = sharedPref.getLong(EXPIRES_AT, 0L)
        return if (token == null || expiresAt < System.currentTimeMillis()) {
            null
        } else {
            token
        }
    }
}