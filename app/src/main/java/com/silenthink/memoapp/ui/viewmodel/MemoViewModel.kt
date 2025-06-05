package com.silenthink.memoapp.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.silenthink.memoapp.data.database.MemoDatabase
import com.silenthink.memoapp.data.model.Memo
import com.silenthink.memoapp.data.model.CategorySuggestion
import com.silenthink.memoapp.data.repository.MemoRepository
import com.silenthink.memoapp.util.CategoryUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Date

class MemoViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: MemoRepository
    val allMemos: LiveData<List<Memo>>
    
    private val _searchQuery = MutableLiveData<String>()
    private val _selectedCategory = MutableLiveData<String>()
    private val _sortOption = MutableLiveData<CategoryUtils.SortOption>()
    private val _displayedMemos = MediatorLiveData<List<Memo>>()
    val displayedMemos: LiveData<List<Memo>> = _displayedMemos
    
    private val _categories = MutableLiveData<List<String>>()
    val categories: LiveData<List<String>> = _categories
    
    // AI分类相关状态
    private val _aiCategorySuggestion = MutableLiveData<CategorySuggestion?>()
    val aiCategorySuggestion: LiveData<CategorySuggestion?> = _aiCategorySuggestion
    
    private val _isAiCategoryLoading = MutableLiveData<Boolean>()
    val isAiCategoryLoading: LiveData<Boolean> = _isAiCategoryLoading
    
    private val _aiCategoryError = MutableLiveData<String?>()
    val aiCategoryError: LiveData<String?> = _aiCategoryError

    init {
        val memoDao = MemoDatabase.getDatabase(application).memoDao()
        repository = MemoRepository(memoDao, application.applicationContext)
        allMemos = repository.allMemos
        
        // 初始化默认值
        _selectedCategory.value = "全部"
        _sortOption.value = CategoryUtils.SortOption.MODIFIED_DATE_DESC
        _isAiCategoryLoading.value = false
        
        setupObservers()
        loadCategories()
    }
    
    private fun setupObservers() {
        _displayedMemos.addSource(allMemos) { 
            updateDisplayedMemos() 
        }
        
        _searchQuery.observeForever { 
            updateDisplayedMemos() 
        }
        
        _selectedCategory.observeForever { 
            updateDisplayedMemos() 
        }
        
        _sortOption.observeForever { 
            updateDisplayedMemos() 
        }
    }
    
    private fun updateDisplayedMemos() {
        val searchQuery = _searchQuery.value ?: ""
        val selectedCategory = _selectedCategory.value ?: "全部"
        val sortOption = _sortOption.value ?: CategoryUtils.SortOption.MODIFIED_DATE_DESC
        
        // 移除所有当前数据源
        _displayedMemos.removeSource(allMemos)
        _displayedMemos.removeSource(repository.searchMemos(""))
        _displayedMemos.removeSource(repository.getMemosSorted(sortOption))
        _displayedMemos.removeSource(repository.getMemosByCategory(selectedCategory))
        _displayedMemos.removeSource(repository.searchMemosByCategory(selectedCategory, searchQuery))
        _displayedMemos.removeSource(repository.getMemosByCategorySorted(selectedCategory, sortOption))
        
        // 根据条件选择数据源
        val dataSource = when {
            searchQuery.isNotEmpty() && selectedCategory != "全部" -> {
                repository.searchMemosByCategory(selectedCategory, searchQuery)
            }
            searchQuery.isNotEmpty() -> {
                repository.searchMemos(searchQuery)
            }
            selectedCategory != "全部" -> {
                repository.getMemosByCategorySorted(selectedCategory, sortOption)
            }
            else -> {
                repository.getMemosSorted(sortOption)
            }
        }
        
        _displayedMemos.addSource(dataSource) { memos ->
            _displayedMemos.value = memos
        }
    }

    fun search(query: String) {
        _searchQuery.value = query
    }
    
    fun selectCategory(category: String) {
        _selectedCategory.value = category
    }
    
    fun setSortOption(sortOption: CategoryUtils.SortOption) {
        _sortOption.value = sortOption
    }
    
    fun getCurrentCategory(): String {
        return _selectedCategory.value ?: "全部"
    }
    
    fun getCurrentSortOption(): CategoryUtils.SortOption {
        return _sortOption.value ?: CategoryUtils.SortOption.MODIFIED_DATE_DESC
    }

    fun clearSearch() {
        _searchQuery.value = ""
    }
    
    // AI分类相关方法
    fun suggestCategory(title: String, content: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _isAiCategoryLoading.postValue(true)
            _aiCategoryError.postValue(null)
            
            try {
                val result = repository.suggestCategory(title, content)
                if (result.isSuccess) {
                    _aiCategorySuggestion.postValue(result.getOrThrow())
                } else {
                    _aiCategoryError.postValue(result.exceptionOrNull()?.message ?: "AI分类建议获取失败")
                }
            } catch (e: Exception) {
                _aiCategoryError.postValue("网络错误：${e.message}")
            } finally {
                _isAiCategoryLoading.postValue(false)
            }
        }
    }
    
    fun clearAiSuggestion() {
        _aiCategorySuggestion.value = null
        _aiCategoryError.value = null
    }
    
    private fun loadCategories() {
        viewModelScope.launch(Dispatchers.IO) {
            val dbCategories = repository.getAllCategories()
            val allCategories = mutableListOf("全部").apply {
                addAll(dbCategories)
                // 确保预定义分类都在列表中
                CategoryUtils.DEFAULT_CATEGORIES.forEach { category ->
                    if (!contains(category)) {
                        add(category)
                    }
                }
            }
            _categories.postValue(allCategories.distinct())
        }
    }

    fun insert(title: String, content: String, imagePath: String? = null, category: String = "默认", priority: Int = 0) = viewModelScope.launch(Dispatchers.IO) {
        val currentTime = Date()
        val memo = Memo(
            title = title,
            content = content,
            createdDate = currentTime,
            modifiedDate = currentTime,
            imagePath = imagePath,
            category = category,
            priority = priority
        )
        repository.insert(memo)
        loadCategories() // 重新加载分类
    }
    
    // 使用AI自动分类插入备忘录
    fun insertWithAiCategory(title: String, content: String, imagePath: String? = null, priority: Int = 0) = viewModelScope.launch(Dispatchers.IO) {
        val currentTime = Date()
        val memo = Memo(
            title = title,
            content = content,
            createdDate = currentTime,
            modifiedDate = currentTime,
            imagePath = imagePath,
            category = "默认", // 将由AI服务自动分类
            priority = priority
        )
        repository.insertWithAiCategory(memo)
        loadCategories() // 重新加载分类
    }

    fun update(memo: Memo) = viewModelScope.launch(Dispatchers.IO) {
        val updatedMemo = memo.copy(modifiedDate = Date())
        repository.update(updatedMemo)
        loadCategories() // 重新加载分类
    }

    fun delete(memo: Memo) = viewModelScope.launch(Dispatchers.IO) {
        repository.delete(memo)
        loadCategories() // 重新加载分类
    }

    fun getMemoById(id: Long): LiveData<Memo> {
        return repository.getMemoById(id)
    }
} 