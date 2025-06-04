package com.silenthink.memoapp.util

import java.security.MessageDigest
import java.security.SecureRandom

object PasswordUtils {
    
    /**
     * 生成随机盐值
     */
    private fun generateSalt(): String {
        val random = SecureRandom()
        val salt = ByteArray(16)
        random.nextBytes(salt)
        return bytesToHex(salt)
    }
    
    /**
     * 将字节数组转换为十六进制字符串
     */
    private fun bytesToHex(bytes: ByteArray): String {
        val result = StringBuilder()
        for (byte in bytes) {
            result.append(String.format("%02x", byte))
        }
        return result.toString()
    }
    
    /**
     * 使用SHA-256算法对密码进行哈希
     */
    private fun hashPassword(password: String, salt: String): String {
        val md = MessageDigest.getInstance("SHA-256")
        val saltedPassword = password + salt
        val hashedBytes = md.digest(saltedPassword.toByteArray())
        return bytesToHex(hashedBytes)
    }
    
    /**
     * 加密密码（生成盐值并哈希）
     */
    fun encryptPassword(password: String): String {
        val salt = generateSalt()
        val hashedPassword = hashPassword(password, salt)
        return "$salt:$hashedPassword"
    }
    
    /**
     * 验证密码
     */
    fun verifyPassword(password: String, encryptedPassword: String): Boolean {
        return try {
            val parts = encryptedPassword.split(":")
            if (parts.size != 2) return false
            
            val salt = parts[0]
            val storedHash = parts[1]
            val inputHash = hashPassword(password, salt)
            
            storedHash == inputHash
        } catch (e: Exception) {
            false
        }
    }
}