package com.silenthink.memoapp.util

import android.content.Context
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import com.silenthink.memoapp.data.model.Memo
import com.silenthink.memoapp.data.model.User
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.lang.reflect.Type
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * 数据备份恢复工具类
 */
object BackupUtils {
    
    private const val BACKUP_DIR = "backup"
    private const val BACKUP_FILE_PREFIX = "memo_backup_"
    private const val BACKUP_FILE_EXTENSION = ".json"
    private const val DATE_FORMAT = "yyyy-MM-dd_HH-mm-ss"
    
    // Gson实例，用于JSON序列化和反序列化
    private val gson: Gson = GsonBuilder()
        .registerTypeAdapter(Date::class.java, DateSerializer())
        .registerTypeAdapter(Date::class.java, DateDeserializer())
        .setPrettyPrinting()
        .create()
    
    /**
     * 备份数据类
     */
    data class BackupData(
        val version: Int = 1,
        val backupTime: Date = Date(),
        val memos: List<Memo> = emptyList(),
        val users: List<User> = emptyList()
    )
    
    /**
     * 创建备份文件
     */
    fun createBackup(
        context: Context,
        memos: List<Memo>,
        users: List<User>
    ): Result<String> {
        return try {
            val backupDir = getBackupDirectory(context)
            if (!backupDir.exists()) {
                backupDir.mkdirs()
            }
            
            val timestamp = SimpleDateFormat(DATE_FORMAT, Locale.getDefault()).format(Date())
            val fileName = "$BACKUP_FILE_PREFIX$timestamp$BACKUP_FILE_EXTENSION"
            val backupFile = File(backupDir, fileName)
            
            val backupData = BackupData(
                memos = memos,
                users = users
            )
            
            FileWriter(backupFile).use { writer ->
                gson.toJson(backupData, writer)
            }
            
            Result.success(backupFile.absolutePath)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 从备份文件恢复数据
     */
    fun restoreFromBackup(backupFilePath: String): Result<BackupData> {
        return try {
            val backupFile = File(backupFilePath)
            if (!backupFile.exists()) {
                return Result.failure(Exception("备份文件不存在"))
            }
            
            val backupData = FileReader(backupFile).use { reader ->
                gson.fromJson(reader, BackupData::class.java)
            }
            
            Result.success(backupData)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 获取所有备份文件
     */
    fun getBackupFiles(context: Context): List<File> {
        val backupDir = getBackupDirectory(context)
        if (!backupDir.exists()) {
            return emptyList()
        }
        
        return backupDir.listFiles { file ->
            file.isFile && file.name.startsWith(BACKUP_FILE_PREFIX) && 
            file.name.endsWith(BACKUP_FILE_EXTENSION)
        }?.sortedByDescending { it.lastModified() } ?: emptyList()
    }
    
    /**
     * 删除备份文件
     */
    fun deleteBackupFile(filePath: String): Boolean {
        return try {
            File(filePath).delete()
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * 获取备份文件信息
     */
    fun getBackupFileInfo(file: File): BackupFileInfo? {
        return try {
            val backupData = FileReader(file).use { reader ->
                gson.fromJson(reader, BackupData::class.java)
            }
            
            BackupFileInfo(
                fileName = file.name,
                filePath = file.absolutePath,
                fileSize = file.length(),
                backupTime = backupData.backupTime,
                memoCount = backupData.memos.size,
                userCount = backupData.users.size,
                version = backupData.version
            )
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * 获取备份目录
     */
    private fun getBackupDirectory(context: Context): File {
        return File(context.filesDir, BACKUP_DIR)
    }
    
    /**
     * 备份文件信息
     */
    data class BackupFileInfo(
        val fileName: String,
        val filePath: String,
        val fileSize: Long,
        val backupTime: Date,
        val memoCount: Int,
        val userCount: Int,
        val version: Int
    )
    
    /**
     * Date序列化器
     */
    private class DateSerializer : JsonSerializer<Date> {
        override fun serialize(
            src: Date?,
            typeOfSrc: Type?,
            context: JsonSerializationContext?
        ): JsonElement {
            return JsonPrimitive(src?.time ?: 0)
        }
    }
    
    /**
     * Date反序列化器
     */
    private class DateDeserializer : JsonDeserializer<Date> {
        override fun deserialize(
            json: JsonElement?,
            typeOfT: Type?,
            context: JsonDeserializationContext?
        ): Date {
            return Date(json?.asLong ?: 0)
        }
    }
} 