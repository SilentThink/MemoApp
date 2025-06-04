package com.silenthink.memoapp.ui.screen

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.silenthink.memoapp.databinding.ActivityRegisterBinding
import com.silenthink.memoapp.ui.viewmodel.UserViewModel

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var userViewModel: UserViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        userViewModel = ViewModelProvider(this)[UserViewModel::class.java]

        setupObservers()
        setupListeners()
    }

    private fun setupObservers() {
        userViewModel.registerResult.observe(this) { result ->
            result?.let {
                if (it.isSuccess) {
                    Toast.makeText(this, "注册成功！请登录", Toast.LENGTH_SHORT).show()
                    navigateToLogin()
                } else {
                    Toast.makeText(this, it.exceptionOrNull()?.message ?: "注册失败", Toast.LENGTH_SHORT).show()
                }
                userViewModel.clearResults()
            }
        }

        userViewModel.isLoading.observe(this) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.btnRegister.isEnabled = !isLoading
        }
    }

    private fun setupListeners() {
        binding.btnRegister.setOnClickListener {
            val username = binding.etUsername.text.toString().trim()
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()
            val confirmPassword = binding.etConfirmPassword.text.toString().trim()

            if (validateInput(username, email, password, confirmPassword)) {
                userViewModel.register(username, password, email)
            }
        }

        binding.tvLoginLink.setOnClickListener {
            navigateToLogin()
        }
    }

    private fun validateInput(username: String, email: String, password: String, confirmPassword: String): Boolean {
        // 验证用户名
        if (username.isEmpty()) {
            binding.tilUsername.error = "请输入用户名"
            return false
        } else if (username.length < 3) {
            binding.tilUsername.error = "用户名至少3个字符"
            return false
        } else {
            binding.tilUsername.error = null
        }

        // 验证邮箱
        if (email.isEmpty()) {
            binding.tilEmail.error = "请输入邮箱"
            return false
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.tilEmail.error = "请输入有效的邮箱地址"
            return false
        } else {
            binding.tilEmail.error = null
        }

        // 验证密码
        if (password.isEmpty()) {
            binding.tilPassword.error = "请输入密码"
            return false
        } else if (password.length < 6) {
            binding.tilPassword.error = "密码至少6个字符"
            return false
        } else {
            binding.tilPassword.error = null
        }

        // 验证确认密码
        if (confirmPassword.isEmpty()) {
            binding.tilConfirmPassword.error = "请确认密码"
            return false
        } else if (password != confirmPassword) {
            binding.tilConfirmPassword.error = "两次输入的密码不一致"
            return false
        } else {
            binding.tilConfirmPassword.error = null
        }

        return true
    }

    private fun navigateToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }
}