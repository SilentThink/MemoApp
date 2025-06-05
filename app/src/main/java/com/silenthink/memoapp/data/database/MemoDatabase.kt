package com.silenthink.memoapp.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.silenthink.memoapp.data.dao.MemoDao
import com.silenthink.memoapp.data.dao.UserDao
import com.silenthink.memoapp.data.model.Memo
import com.silenthink.memoapp.data.model.User
import com.silenthink.memoapp.util.DateConverter

@Database(entities = [Memo::class, User::class], version = 3, exportSchema = false)
@TypeConverters(DateConverter::class)
abstract class MemoDatabase : RoomDatabase() {

    abstract fun memoDao(): MemoDao
    abstract fun userDao(): UserDao

    companion object {
        @Volatile
        private var INSTANCE: MemoDatabase? = null

        fun getDatabase(context: Context): MemoDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MemoDatabase::class.java,
                    "memo_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}