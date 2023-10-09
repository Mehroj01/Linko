package com.neb.linko.cache

import android.content.SharedPreferences

class LanguageCache(var sharedPreferences: SharedPreferences) {

    var editor: SharedPreferences.Editor = sharedPreferences.edit()

    fun getLanguage(): Boolean {
        return sharedPreferences.getBoolean("language", true)
    }

    fun updateLanguage(str: String) {
        editor.putBoolean("language", if (str == "en") true else str != "ar")
        editor.commit()
    }

}