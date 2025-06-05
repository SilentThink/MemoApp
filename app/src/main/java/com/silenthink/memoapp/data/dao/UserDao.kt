package com.silenthink.memoapp.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.silenthink.memoapp.data.model.User

@Dao
interface UserDao {
    
    @Insert
    suspend fun insertUser(user: User): Long
    
    @Query("SELECT * FROM users WHERE username = :username LIMIT 1")
    suspend fun getUserByUsername(username: String): User?
    
    @Query("SELECT * FROM users ORDER BY createdDate DESC")
    suspend fun getAllUsersList(): List<User>
    
    @Query("SELECT COUNT(*) FROM users WHERE username = :username")
    suspend fun isUsernameExists(username: String): Int
    
    @Query("SELECT COUNT(*) FROM users WHERE email = :email")
    suspend fun isEmailExists(email: String): Int
}