package com.silenthink.memoapp.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.silenthink.memoapp.data.database.MemoDatabase
import com.silenthink.memoapp.data.model.User
import com.silenthink.memoapp.data.repository.UserRepository
import kotlinx.coroutines.launch

class UserViewModel(application: Application) : AndroidViewModel(application) {
    
    private val userRepository: UserRepository
    
    private val _loginResult = MutableLiveData<Result<User>>()
    val loginResult: LiveData<Result<User>> = _loginResult
    
    private val _registerResult = MutableLiveData<Result<Long>>()
    val registerResult: LiveData<Result<Long>> = _registerResult
    
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading
    
    init {
        val userDao = MemoDatabase.getDatabase(application).userDao()
        userRepository = UserRepository(userDao)
    }
    
    fun login(username: String, password: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val result = userRepository.loginUser(username, password)
                _loginResult.value = result
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun register(username: String, password: String, email: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val result = userRepository.registerUser(username, password, email)
                _registerResult.value = result
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun clearResults() {
        _loginResult.value = null
        _registerResult.value = null
    }
}