package com.silenthink.memoapp.data.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.silenthink.memoapp.data.model.Memo

@Dao
interface MemoDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMemo(memo: Memo): Long

    @Update
    suspend fun updateMemo(memo: Memo)

    @Delete
    suspend fun deleteMemo(memo: Memo)

    @Query("SELECT * FROM memos ORDER BY modifiedDate DESC")
    fun getAllMemos(): LiveData<List<Memo>>

    @Query("SELECT * FROM memos WHERE id = :memoId")
    fun getMemoById(memoId: Long): LiveData<Memo>

    @Query("DELETE FROM memos WHERE id = :memoId")
    suspend fun deleteMemoById(memoId: Long)

    @Query("SELECT * FROM memos WHERE title LIKE '%' || :searchQuery || '%' OR content LIKE '%' || :searchQuery || '%' ORDER BY modifiedDate DESC")
    fun searchMemos(searchQuery: String): LiveData<List<Memo>>
} 