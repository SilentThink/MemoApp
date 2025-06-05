package com.silenthink.memoapp.ui.screen

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.silenthink.memoapp.R
import com.silenthink.memoapp.util.ApiConfig

class SettingsActivity : AppCompatActivity() {
    
    private lateinit var toolbar: Toolbar
    private lateinit var apiKeyLayout: TextInputLayout
    private lateinit var apiKeyEditText: TextInputEditText
    private lateinit var saveButton: MaterialButton
    private lateinit var clearButton: MaterialButton
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        
        initViews()
        setupToolbar()
        loadCurrentApiKey()
        setupClickListeners()
    }
    
    private fun initViews() {
        toolbar = findViewById(R.id.toolbar)
        apiKeyLayout = findViewById(R.id.api_key_layout)
        apiKeyEditText = findViewById(R.id.api_key_edit_text)
        saveButton = findViewById(R.id.save_button)
        clearButton = findViewById(R.id.clear_button)
    }
    
    private fun setupToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
            title = "AI设置"
        }
    }
    
    private fun loadCurrentApiKey() {
        val currentApiKey = ApiConfig.getDeepSeekApiKey(this)
        if (currentApiKey.isNotBlank()) {
            // 只显示前8位字符，其余用*代替
            val maskedKey = if (currentApiKey.length > 8) {
                currentApiKey.take(8) + "*".repeat(currentApiKey.length - 8)
            } else {
                "*".repeat(currentApiKey.length)
            }
            apiKeyEditText.setText(maskedKey)
        }
    }
    
    private fun setupClickListeners() {
        saveButton.setOnClickListener {
            saveApiKey()
        }
        
        clearButton.setOnClickListener {
            clearApiKey()
        }
    }
    
    private fun saveApiKey() {
        val apiKey = apiKeyEditText.text.toString().trim()
        
        if (apiKey.isBlank()) {
            apiKeyLayout.error = "请输入API密钥"
            return
        }
        
        if (apiKey.contains("*")) {
            // 如果包含*，说明用户没有修改，不需要保存
            Toast.makeText(this, "API密钥未修改", Toast.LENGTH_SHORT).show()
            return
        }
        
        // 基本验证API密钥格式
        if (!isValidApiKey(apiKey)) {
            apiKeyLayout.error = "API密钥格式不正确"
            return
        }
        
        ApiConfig.setDeepSeekApiKey(this, apiKey)
        apiKeyLayout.error = null
        Toast.makeText(this, "API密钥已保存", Toast.LENGTH_SHORT).show()
        
        // 重新加载显示
        loadCurrentApiKey()
    }
    
    private fun clearApiKey() {
        ApiConfig.clearApiKey(this)
        apiKeyEditText.setText("")
        Toast.makeText(this, "API密钥已清除", Toast.LENGTH_SHORT).show()
    }
    
    private fun isValidApiKey(apiKey: String): Boolean {
        // 基本的API密钥格式验证
        return apiKey.length >= 10 && apiKey.startsWith("sk-")
    }
    
    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
} 