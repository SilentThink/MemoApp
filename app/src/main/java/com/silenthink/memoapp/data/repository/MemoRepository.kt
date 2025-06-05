package com.silenthink.memoapp.data.repository

import androidx.lifecycle.LiveData
import com.silenthink.memoapp.data.dao.MemoDao
import com.silenthink.memoapp.data.model.Memo
import com.silenthink.memoapp.util.CategoryUtils

class MemoRepository(private val memoDao: MemoDao) {

    val allMemos: LiveData<List<Memo>> = memoDao.getAllMemos()

    suspend fun insert(memo: Memo): Long {
        return memoDao.insertMemo(memo)
    }

    suspend fun update(memo: Memo) {
        memoDao.updateMemo(memo)
    }

    suspend fun delete(memo: Memo) {
        memoDao.deleteMemo(memo)
    }

    suspend fun deleteById(id: Long) {
        memoDao.deleteMemoById(id)
    }

    fun getMemoById(id: Long): LiveData<Memo> {
        return memoDao.getMemoById(id)
    }

    fun searchMemos(query: String): LiveData<List<Memo>> {
        return memoDao.searchMemos(query)
    }
    
    // 分类相关方法
    fun getMemosByCategory(category: String): LiveData<List<Memo>> {
        return memoDao.getMemosByCategory(category)
    }
    
    suspend fun getAllCategories(): List<String> {
        return memoDao.getAllCategories()
    }
    
    fun searchMemosByCategory(category: String, query: String): LiveData<List<Memo>> {
        return memoDao.searchMemosByCategory(category, query)
    }
    
    // 排序相关方法
    fun getMemosSorted(sortOption: CategoryUtils.SortOption): LiveData<List<Memo>> {
        return when (sortOption) {
            CategoryUtils.SortOption.MODIFIED_DATE_DESC -> memoDao.getAllMemosSortedByModifiedDateDesc()
            CategoryUtils.SortOption.MODIFIED_DATE_ASC -> memoDao.getAllMemosSortedByModifiedDateAsc()
            CategoryUtils.SortOption.CREATED_DATE_DESC -> memoDao.getAllMemosSortedByCreatedDateDesc()
            CategoryUtils.SortOption.CREATED_DATE_ASC -> memoDao.getAllMemosSortedByCreatedDateAsc()
            CategoryUtils.SortOption.TITLE_ASC -> memoDao.getAllMemosSortedByTitleAsc()
            CategoryUtils.SortOption.TITLE_DESC -> memoDao.getAllMemosSortedByTitleDesc()
            CategoryUtils.SortOption.PRIORITY_DESC -> memoDao.getAllMemosSortedByPriorityDesc()
            CategoryUtils.SortOption.PRIORITY_ASC -> memoDao.getAllMemosSortedByPriorityAsc()
            CategoryUtils.SortOption.CATEGORY_ASC -> memoDao.getAllMemosSortedByCategory()
        }
    }
    
    // 按分类和排序查询
    fun getMemosByCategorySorted(category: String, sortOption: CategoryUtils.SortOption): LiveData<List<Memo>> {
        return when (sortOption) {
            CategoryUtils.SortOption.MODIFIED_DATE_DESC -> memoDao.getMemosByCategorySortedByModifiedDateDesc(category)
            CategoryUtils.SortOption.MODIFIED_DATE_ASC -> memoDao.getMemosByCategorySortedByModifiedDateAsc(category)
            CategoryUtils.SortOption.CREATED_DATE_DESC -> memoDao.getMemosByCategorySortedByCreatedDateDesc(category)
            CategoryUtils.SortOption.CREATED_DATE_ASC -> memoDao.getMemosByCategorySortedByCreatedDateAsc(category)
            CategoryUtils.SortOption.TITLE_ASC -> memoDao.getMemosByCategorySortedByTitleAsc(category)
            CategoryUtils.SortOption.TITLE_DESC -> memoDao.getMemosByCategorySortedByTitleDesc(category)
            CategoryUtils.SortOption.PRIORITY_DESC -> memoDao.getMemosByCategorySortedByPriorityDesc(category)
            CategoryUtils.SortOption.PRIORITY_ASC -> memoDao.getMemosByCategorySortedByPriorityAsc(category)
            CategoryUtils.SortOption.CATEGORY_ASC -> memoDao.getMemosByCategorySortedByModifiedDateDesc(category) // 分类内按修改时间排序
        }
    }
} 