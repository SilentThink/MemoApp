package com.silenthink.memoapp.data.repository

import com.silenthink.memoapp.data.dao.UserDao
import com.silenthink.memoapp.data.model.User
import com.silenthink.memoapp.util.PasswordUtils
import java.util.Date

class UserRepository(private val userDao: UserDao) {
    
    suspend fun registerUser(username: String, password: String, email: String): Result<Long> {
        return try {
            // 检查用户名是否已存在
            if (userDao.isUsernameExists(username) > 0) {
                Result.failure(Exception("用户名已存在"))
            }
            // 检查邮箱是否已存在
            else if (userDao.isEmailExists(email) > 0) {
                Result.failure(Exception("邮箱已被注册"))
            }
            else {
                val encryptedPassword = PasswordUtils.encryptPassword(password)
                val user = User(
                    username = username,
                    password = encryptedPassword,
                    email = email,
                    createdDate = Date()
                )
                val userId = userDao.insertUser(user)
                Result.success(userId)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun loginUser(username: String, password: String): Result<User> {
        return try {
            val user = userDao.getUserByUsername(username)
            if (user != null && PasswordUtils.verifyPassword(password, user.password)) {
                Result.success(user)
            } else {
                Result.failure(Exception("用户名或密码错误"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getUserByUsername(username: String): User? {
        return try {
            userDao.getUserByUsername(username)
        } catch (e: Exception) {
            null
        }
    }
}