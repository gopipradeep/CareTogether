package com.example.orphans

import android.content.Context
import com.google.gson.Gson

object UserCache {
    private const val PREFS_NAME = "user_prefs"
    private const val KEY_USER_PROFILE = "user_profile"

    fun saveUserProfile(context: Context, profile: UserProfile) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val json = Gson().toJson(profile)
        prefs.edit().putString(KEY_USER_PROFILE, json).apply()
    }

    fun getUserProfile(context: Context): UserProfile? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val json = prefs.getString(KEY_USER_PROFILE, null)
        return if (json != null) {
            Gson().fromJson(json, UserProfile::class.java)
        } else {
            null
        }
    }

    fun clear(context: Context) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit().clear().apply()
    }
}