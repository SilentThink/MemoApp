package com.silenthink.memoapp.data.repository

import android.content.Context
import com.silenthink.memoapp.data.dao.MemoDao
import com.silenthink.memoapp.data.dao.UserDao
import com.silenthink.memoapp.data.model.Memo
import com.silenthink.memoapp.data.model.User
import com.silenthink.memoapp.util.BackupUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/**
 * 备份恢复数据仓库
 */
class BackupRepository(
    private val context: Context,
    private val memoDao: MemoDao,
    private val userDao: UserDao
) {
    
    /**
     * 创建数据备份
     */
    suspend fun createBackup(): Result<String> = withContext(Dispatchers.IO) {
        try {
            // 获取所有备忘录数据
            val memos = getAllMemosSync()
            
            // 获取所有用户数据  
            val users = getAllUsersSync()
            
            // 创建备份文件
            BackupUtils.createBackup(context, memos, users)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 从备份文件恢复数据
     */
    suspend fun restoreFromBackup(
        backupFilePath: String,
        clearExistingData: Boolean = false
    ): Result<BackupUtils.BackupData> = withContext(Dispatchers.IO) {
        try {
            // 读取备份数据
            val result = BackupUtils.restoreFromBackup(backupFilePath)
            if (result.isFailure) {
                return@withContext result
            }
            
            val backupData = result.getOrThrow()
            
            // 如果需要清除现有数据
            if (clearExistingData) {
                clearAllData()
            }
            
            // 恢复用户数据
            backupData.users.forEach { user ->
                try {
                    // 检查用户是否已存在，避免重复插入
                    val existingUser = userDao.getUserByUsername(user.username)
                    if (existingUser == null) {
                        userDao.insertUser(user.copy(id = 0)) // 重置ID，让数据库自动生成
                    }
                } catch (e: Exception) {
                    // 忽略重复用户错误，继续处理其他数据
                }
            }
            
            // 恢复备忘录数据
            backupData.memos.forEach { memo ->
                try {
                    memoDao.insertMemo(memo.copy(id = 0)) // 重置ID，让数据库自动生成
                } catch (e: Exception) {
                    // 记录错误但不中断恢复过程
                }
            }
            
            Result.success(backupData)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 获取所有备份文件
     */
    suspend fun getBackupFiles(): List<BackupUtils.BackupFileInfo> = withContext(Dispatchers.IO) {
        BackupUtils.getBackupFiles(context).mapNotNull { file ->
            BackupUtils.getBackupFileInfo(file)
        }
    }
    
    /**
     * 删除备份文件
     */
    suspend fun deleteBackupFile(filePath: String): Boolean = withContext(Dispatchers.IO) {
        BackupUtils.deleteBackupFile(filePath)
    }
    
    /**
     * 获取备份文件详情
     */
    suspend fun getBackupFileDetails(filePath: String): Result<BackupUtils.BackupData> = withContext(Dispatchers.IO) {
        BackupUtils.restoreFromBackup(filePath)
    }
    
    /**
     * 清除所有数据
     */
    private suspend fun clearAllData() {
        // 清除所有备忘录
        val allMemos = getAllMemosSync()
        allMemos.forEach { memo ->
            memoDao.deleteMemo(memo)
        }
    }
    
    /**
     * 同步获取所有备忘录（用于备份）
     */
    private suspend fun getAllMemosSync(): List<Memo> {
        return try {
            // 由于我们需要同步获取数据，这里需要一个同步的查询方法
            // 如果DAO中没有同步方法，我们需要添加一个
            memoDao.getAllMemosList()
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    /**
     * 同步获取所有用户（用于备份）
     */
    private suspend fun getAllUsersSync(): List<User> {
        return try {
            // 使用UserDao而不是MemoDao
            userDao.getAllUsersList()
        } catch (e: Exception) {
            emptyList()
        }
    }
} 