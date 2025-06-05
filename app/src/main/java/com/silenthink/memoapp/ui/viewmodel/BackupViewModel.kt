package com.silenthink.memoapp.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.silenthink.memoapp.data.database.MemoDatabase
import com.silenthink.memoapp.data.repository.BackupRepository
import com.silenthink.memoapp.util.BackupUtils
import kotlinx.coroutines.launch

class BackupViewModel(application: Application) : AndroidViewModel(application) {
    
    private val database = MemoDatabase.getDatabase(application)
    private val backupRepository = BackupRepository(
        application,
        database.memoDao(),
        database.userDao()
    )
    
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading
    
    private val _backupResult = MutableLiveData<Result<String>?>()
    val backupResult: LiveData<Result<String>?> = _backupResult
    
    private val _restoreResult = MutableLiveData<Result<BackupUtils.BackupData>?>()
    val restoreResult: LiveData<Result<BackupUtils.BackupData>?> = _restoreResult
    
    private val _backupFiles = MutableLiveData<List<BackupUtils.BackupFileInfo>>()
    val backupFiles: LiveData<List<BackupUtils.BackupFileInfo>> = _backupFiles
    
    private val _deleteResult = MutableLiveData<Result<Boolean>?>()
    val deleteResult: LiveData<Result<Boolean>?> = _deleteResult
    
    /**
     * 创建备份
     */
    fun createBackup() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val result = backupRepository.createBackup()
                _backupResult.value = result
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * 恢复数据
     */
    fun restoreFromBackup(filePath: String, clearExistingData: Boolean = false) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val result = backupRepository.restoreFromBackup(filePath, clearExistingData)
                _restoreResult.value = result
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * 加载备份文件列表
     */
    fun loadBackupFiles() {
        viewModelScope.launch {
            try {
                val files = backupRepository.getBackupFiles()
                _backupFiles.value = files
            } catch (e: Exception) {
                _backupFiles.value = emptyList()
            }
        }
    }
    
    /**
     * 删除备份文件
     */
    fun deleteBackupFile(filePath: String) {
        viewModelScope.launch {
            try {
                val success = backupRepository.deleteBackupFile(filePath)
                _deleteResult.value = Result.success(success)
                
                // 删除成功后刷新列表
                if (success) {
                    loadBackupFiles()
                }
            } catch (e: Exception) {
                _deleteResult.value = Result.failure(e)
            }
        }
    }
    
    /**
     * 获取备份文件详情
     */
    suspend fun getBackupFileDetails(filePath: String): Result<BackupUtils.BackupData> {
        return backupRepository.getBackupFileDetails(filePath)
    }
    
    /**
     * 清除结果状态
     */
    fun clearResults() {
        _backupResult.value = null
        _restoreResult.value = null
        _deleteResult.value = null
    }
} 