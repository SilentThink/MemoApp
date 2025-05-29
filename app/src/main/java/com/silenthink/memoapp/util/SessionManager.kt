package com.silenthink.memoapp.util

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {
    private var prefs: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    private var editor: SharedPreferences.Editor = prefs.edit()

    companion object {
        private const val PREF_NAME = "MemoAppSession"
        private const val IS_LOGIN = "IsLoggedIn"
        private const val KEY_USERNAME = "username"
    }

    // 登录状态
    var isLoggedIn: Boolean
        get() = prefs.getBoolean(IS_LOGIN, false)
        set(value) {
            editor.putBoolean(IS_LOGIN, value)
            editor.apply()
        }

    // 用户名
    var username: String?
        get() = prefs.getString(KEY_USERNAME, null)
        set(value) {
            editor.putString(KEY_USERNAME, value)
            editor.apply()
        }

    // 创建登录会话
    fun createLoginSession(username: String) {
        editor.putBoolean(IS_LOGIN, true)
        editor.putString(KEY_USERNAME, username)
        editor.apply()
    }

    // 清除会话数据
    fun logout() {
        editor.clear()
        editor.apply()
    }
} 