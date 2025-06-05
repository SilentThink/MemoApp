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
    
    // 按分类查询
    @Query("SELECT * FROM memos WHERE category = :category ORDER BY modifiedDate DESC")
    fun getMemosByCategory(category: String): LiveData<List<Memo>>
    
    // 获取所有分类
    @Query("SELECT DISTINCT category FROM memos ORDER BY category ASC")
    suspend fun getAllCategories(): List<String>
    
    // 按分类搜索
    @Query("SELECT * FROM memos WHERE category = :category AND (title LIKE '%' || :searchQuery || '%' OR content LIKE '%' || :searchQuery || '%') ORDER BY modifiedDate DESC")
    fun searchMemosByCategory(category: String, searchQuery: String): LiveData<List<Memo>>
    
    // 各种排序方式
    @Query("SELECT * FROM memos ORDER BY modifiedDate DESC")
    fun getAllMemosSortedByModifiedDateDesc(): LiveData<List<Memo>>
    
    @Query("SELECT * FROM memos ORDER BY modifiedDate ASC")
    fun getAllMemosSortedByModifiedDateAsc(): LiveData<List<Memo>>
    
    @Query("SELECT * FROM memos ORDER BY createdDate DESC")
    fun getAllMemosSortedByCreatedDateDesc(): LiveData<List<Memo>>
    
    @Query("SELECT * FROM memos ORDER BY createdDate ASC")
    fun getAllMemosSortedByCreatedDateAsc(): LiveData<List<Memo>>
    
    @Query("SELECT * FROM memos ORDER BY title ASC")
    fun getAllMemosSortedByTitleAsc(): LiveData<List<Memo>>
    
    @Query("SELECT * FROM memos ORDER BY title DESC")
    fun getAllMemosSortedByTitleDesc(): LiveData<List<Memo>>
    
    @Query("SELECT * FROM memos ORDER BY priority DESC, modifiedDate DESC")
    fun getAllMemosSortedByPriorityDesc(): LiveData<List<Memo>>
    
    @Query("SELECT * FROM memos ORDER BY priority ASC, modifiedDate DESC")
    fun getAllMemosSortedByPriorityAsc(): LiveData<List<Memo>>
    
    @Query("SELECT * FROM memos ORDER BY category ASC, modifiedDate DESC")
    fun getAllMemosSortedByCategory(): LiveData<List<Memo>>
    
    // 按分类和排序查询
    @Query("SELECT * FROM memos WHERE category = :category ORDER BY modifiedDate DESC")
    fun getMemosByCategorySortedByModifiedDateDesc(category: String): LiveData<List<Memo>>
    
    @Query("SELECT * FROM memos WHERE category = :category ORDER BY modifiedDate ASC")
    fun getMemosByCategorySortedByModifiedDateAsc(category: String): LiveData<List<Memo>>
    
    @Query("SELECT * FROM memos WHERE category = :category ORDER BY createdDate DESC")
    fun getMemosByCategorySortedByCreatedDateDesc(category: String): LiveData<List<Memo>>
    
    @Query("SELECT * FROM memos WHERE category = :category ORDER BY createdDate ASC")
    fun getMemosByCategorySortedByCreatedDateAsc(category: String): LiveData<List<Memo>>
    
    @Query("SELECT * FROM memos WHERE category = :category ORDER BY title ASC")
    fun getMemosByCategorySortedByTitleAsc(category: String): LiveData<List<Memo>>
    
    @Query("SELECT * FROM memos WHERE category = :category ORDER BY title DESC")
    fun getMemosByCategorySortedByTitleDesc(category: String): LiveData<List<Memo>>
    
    @Query("SELECT * FROM memos WHERE category = :category ORDER BY priority DESC, modifiedDate DESC")
    fun getMemosByCategorySortedByPriorityDesc(category: String): LiveData<List<Memo>>
    
    @Query("SELECT * FROM memos WHERE category = :category ORDER BY priority ASC, modifiedDate DESC")
    fun getMemosByCategorySortedByPriorityAsc(category: String): LiveData<List<Memo>>
} 