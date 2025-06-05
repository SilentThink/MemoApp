package com.silenthink.memoapp

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.silenthink.memoapp.databinding.ActivityMainBinding
import com.silenthink.memoapp.ui.screen.LoginActivity
import com.silenthink.memoapp.ui.screen.MemoAdapter
import com.silenthink.memoapp.ui.screen.MemoDetailActivity
import com.silenthink.memoapp.ui.screen.SwipeToDeleteCallback
import com.silenthink.memoapp.ui.viewmodel.MemoViewModel
import com.silenthink.memoapp.util.CategoryUtils
import com.silenthink.memoapp.util.SessionManager

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var memoViewModel: MemoViewModel
    private lateinit var adapter: MemoAdapter
    private lateinit var sessionManager: SessionManager
    private lateinit var categoryAdapter: ArrayAdapter<String>
    private lateinit var sortAdapter: ArrayAdapter<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // 设置Toolbar
        setSupportActionBar(binding.toolbar)
        
        // 初始化SessionManager
        sessionManager = SessionManager(this)
        
        // 检查登录状态
        if (!sessionManager.isLoggedIn) {
            navigateToLogin()
            return
        }
        
        // 设置RecyclerView
        adapter = MemoAdapter(
            onItemClick = { memo ->
                val intent = Intent(this, MemoDetailActivity::class.java)
                intent.putExtra(MemoDetailActivity.EXTRA_MEMO_ID, memo.id)
                startActivity(intent)
            },
            onItemDelete = { memo ->
                showDeleteConfirmDialog(memo)
            }
        )
        
        binding.rvMemos.layoutManager = LinearLayoutManager(this)
        binding.rvMemos.adapter = adapter
        
        // 设置滑动删除
        val swipeToDeleteCallback = SwipeToDeleteCallback(adapter, this) { memo ->
            showDeleteConfirmDialog(memo)
        }
        val itemTouchHelper = ItemTouchHelper(swipeToDeleteCallback)
        itemTouchHelper.attachToRecyclerView(binding.rvMemos)
        
        // 初始化ViewModel
        memoViewModel = ViewModelProvider(this)[MemoViewModel::class.java]
        
        // 设置分类Spinner
        setupCategorySpinner()
        
        // 设置排序Spinner
        setupSortSpinner()
        
        // 观察备忘录数据变化 - 使用displayedMemos而不是allMemos
        memoViewModel.displayedMemos.observe(this) { memos ->
            adapter.submitList(memos)
            
            // 显示或隐藏空视图
            if (memos.isEmpty()) {
                binding.tvEmptyView.visibility = View.VISIBLE
                binding.rvMemos.visibility = View.GONE
            } else {
                binding.tvEmptyView.visibility = View.GONE
                binding.rvMemos.visibility = View.VISIBLE
            }
        }
        
        // 设置搜索功能
        binding.searchView.setOnQueryTextListener(object : androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }
            
            override fun onQueryTextChange(newText: String?): Boolean {
                memoViewModel.search(newText ?: "")
                return true
            }
        })
        
        // 设置添加按钮点击事件
        binding.fabAddMemo.setOnClickListener {
            val intent = Intent(this, MemoDetailActivity::class.java)
            startActivity(intent)
        }
    }
    
    private fun setupCategorySpinner() {
        categoryAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, mutableListOf<String>())
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerCategory.adapter = categoryAdapter
        
        // 观察分类数据变化
        memoViewModel.categories.observe(this) { categories ->
            categoryAdapter.clear()
            categoryAdapter.addAll(categories)
            categoryAdapter.notifyDataSetChanged()
            
            // 设置当前选中的分类
            val currentCategory = memoViewModel.getCurrentCategory()
            val position = categories.indexOf(currentCategory)
            if (position >= 0) {
                binding.spinnerCategory.setSelection(position)
            }
        }
        
        // 设置分类选择监听器
        binding.spinnerCategory.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedCategory = categoryAdapter.getItem(position) ?: "全部"
                memoViewModel.selectCategory(selectedCategory)
            }
            
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }
    
    private fun setupSortSpinner() {
        val sortOptions = CategoryUtils.SortOption.values().map { it.displayName }
        sortAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, sortOptions)
        sortAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerSort.adapter = sortAdapter
        
        // 设置当前选中的排序选项
        val currentSortOption = memoViewModel.getCurrentSortOption()
        val position = CategoryUtils.SortOption.values().indexOf(currentSortOption)
        if (position >= 0) {
            binding.spinnerSort.setSelection(position)
        }
        
        // 设置排序选择监听器
        binding.spinnerSort.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedSortOption = CategoryUtils.SortOption.values()[position]
                memoViewModel.setSortOption(selectedSortOption)
            }
            
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }
    
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }
    
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_logout -> {
                sessionManager.logout()
                navigateToLogin()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
    
    private fun navigateToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }
    
    private fun showDeleteConfirmDialog(memo: com.silenthink.memoapp.data.model.Memo) {
        AlertDialog.Builder(this)
            .setTitle("删除备忘录")
            .setMessage("确定要删除备忘录 \"${memo.title}\" 吗？")
            .setPositiveButton("删除") { _, _ ->
                memoViewModel.delete(memo)
                Toast.makeText(this, "备忘录已删除", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("取消") { dialog, _ ->
                dialog.dismiss()
                // 恢复被滑动的项目
                adapter.notifyDataSetChanged()
            }
            .setOnCancelListener {
                // 恢复被滑动的项目
                adapter.notifyDataSetChanged()
            }
            .show()
    }
}