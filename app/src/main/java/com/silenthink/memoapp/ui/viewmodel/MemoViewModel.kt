package com.silenthink.memoapp.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.silenthink.memoapp.data.database.MemoDatabase
import com.silenthink.memoapp.data.model.Memo
import com.silenthink.memoapp.data.repository.MemoRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Date

class MemoViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: MemoRepository
    val allMemos: LiveData<List<Memo>>

    init {
        val memoDao = MemoDatabase.getDatabase(application).memoDao()
        repository = MemoRepository(memoDao)
        allMemos = repository.allMemos
    }

    fun insert(title: String, content: String) = viewModelScope.launch(Dispatchers.IO) {
        val currentTime = Date()
        val memo = Memo(
            title = title,
            content = content,
            createdDate = currentTime,
            modifiedDate = currentTime
        )
        repository.insert(memo)
    }

    fun update(memo: Memo) = viewModelScope.launch(Dispatchers.IO) {
        val updatedMemo = memo.copy(modifiedDate = Date())
        repository.update(updatedMemo)
    }

    fun delete(memo: Memo) = viewModelScope.launch(Dispatchers.IO) {
        repository.delete(memo)
    }

    fun getMemoById(id: Long): LiveData<Memo> {
        return repository.getMemoById(id)
    }
} 