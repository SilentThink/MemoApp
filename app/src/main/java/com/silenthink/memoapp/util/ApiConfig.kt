package com.silenthink.memoapp.util

import android.content.Context
import android.content.SharedPreferences

object ApiConfig {
    private const val PREF_NAME = "api_config"
    private const val KEY_DEEPSEEK_API_KEY = "deepseek_api_key"
    private const val DEFAULT_API_KEY = "" // 空字符串，需要用户设置
    
    // DeepSeek API配置
    const val DEEPSEEK_BASE_URL = "https://api.deepseek.com/v1/"
    const val DEEPSEEK_MODEL = "deepseek-chat"
    
    private fun getPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }
    
    fun getDeepSeekApiKey(context: Context): String {
        return getPreferences(context).getString(KEY_DEEPSEEK_API_KEY, DEFAULT_API_KEY) ?: DEFAULT_API_KEY
    }
    
    fun setDeepSeekApiKey(context: Context, apiKey: String) {
        getPreferences(context).edit()
            .putString(KEY_DEEPSEEK_API_KEY, apiKey)
            .apply()
    }
    
    fun isApiKeyConfigured(context: Context): Boolean {
        return getDeepSeekApiKey(context).isNotBlank()
    }
    
    fun clearApiKey(context: Context) {
        getPreferences(context).edit()
            .remove(KEY_DEEPSEEK_API_KEY)
            .apply()
    }
} 