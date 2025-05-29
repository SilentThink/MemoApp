package com.silenthink.memoapp.data.repository

import androidx.lifecycle.LiveData
import com.silenthink.memoapp.data.dao.MemoDao
import com.silenthink.memoapp.data.model.Memo

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
} 