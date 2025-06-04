package com.silenthink.memoapp.ui.screen

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.silenthink.memoapp.MainActivity
import com.silenthink.memoapp.databinding.ActivityLoginBinding
import com.silenthink.memoapp.ui.viewmodel.UserViewModel
import com.silenthink.memoapp.util.SessionManager

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var sessionManager: SessionManager
    private lateinit var userViewModel: UserViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)
        userViewModel = ViewModelProvider(this)[UserViewModel::class.java]

        // 检查是否已经登录
        if (sessionManager.isLoggedIn) {
            navigateToMainActivity()
            return
        }

        setupObservers()
        setupListeners()
    }

    private fun setupObservers() {
        userViewModel.loginResult.observe(this) { result ->
            result?.let {
                if (it.isSuccess) {
                    val user = it.getOrNull()
                    user?.let { u ->
                        val rememberMe = binding.cbRememberMe.isChecked
                        if (rememberMe) {
                            sessionManager.createLoginSession(u.username)
                        }
                        Toast.makeText(this, "登录成功", Toast.LENGTH_SHORT).show()
                        navigateToMainActivity()
                    }
                } else {
                    Toast.makeText(this, it.exceptionOrNull()?.message ?: "登录失败", Toast.LENGTH_SHORT).show()
                }
                userViewModel.clearResults()
            }
        }

        userViewModel.isLoading.observe(this) { isLoading ->
            binding.btnLogin.isEnabled = !isLoading
            // 可以添加进度条显示
        }
    }

    private fun setupListeners() {
        binding.btnLogin.setOnClickListener {
            val username = binding.etUsername.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "请输入用户名和密码", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            userViewModel.login(username, password)
        }

        binding.tvRegisterLink.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
    }

    private fun navigateToMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}