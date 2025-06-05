package com.silenthink.memoapp.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
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
    
    private val _searchQuery = MutableLiveData<String>()
    private val _displayedMemos = MediatorLiveData<List<Memo>>()
    val displayedMemos: LiveData<List<Memo>> = _displayedMemos

    init {
        val memoDao = MemoDatabase.getDatabase(application).memoDao()
        repository = MemoRepository(memoDao)
        allMemos = repository.allMemos
        
        _displayedMemos.addSource(allMemos) { memos ->
            if (_searchQuery.value.isNullOrEmpty()) {
                _displayedMemos.value = memos
            }
        }
        
        _searchQuery.observeForever { query ->
            if (query.isNullOrEmpty()) {
                _displayedMemos.removeSource(repository.searchMemos(""))
                _displayedMemos.addSource(allMemos) { memos ->
                    _displayedMemos.value = memos
                }
            } else {
                _displayedMemos.removeSource(allMemos)
                _displayedMemos.addSource(repository.searchMemos(query)) { searchResults ->
                    _displayedMemos.value = searchResults
                }
            }
        }
    }

    fun search(query: String) {
        _searchQuery.value = query
    }

    fun clearSearch() {
        _searchQuery.value = ""
    }

    fun insert(title: String, content: String, imagePath: String? = null) = viewModelScope.launch(Dispatchers.IO) {
        val currentTime = Date()
        val memo = Memo(
            title = title,
            content = content,
            createdDate = currentTime,
            modifiedDate = currentTime,
            imagePath = imagePath
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