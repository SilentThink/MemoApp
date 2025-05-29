package com.silenthink.memoapp

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.silenthink.memoapp.databinding.ActivityMainBinding
import com.silenthink.memoapp.ui.screen.LoginActivity
import com.silenthink.memoapp.ui.screen.MemoAdapter
import com.silenthink.memoapp.ui.screen.MemoDetailActivity
import com.silenthink.memoapp.ui.viewmodel.MemoViewModel
import com.silenthink.memoapp.util.SessionManager

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var memoViewModel: MemoViewModel
    private lateinit var adapter: MemoAdapter
    private lateinit var sessionManager: SessionManager

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
        adapter = MemoAdapter { memo ->
            val intent = Intent(this, MemoDetailActivity::class.java)
            intent.putExtra(MemoDetailActivity.EXTRA_MEMO_ID, memo.id)
            startActivity(intent)
        }
        
        binding.rvMemos.layoutManager = LinearLayoutManager(this)
        binding.rvMemos.adapter = adapter
        
        // 初始化ViewModel
        memoViewModel = ViewModelProvider(this)[MemoViewModel::class.java]
        
        // 观察备忘录数据变化
        memoViewModel.allMemos.observe(this) { memos ->
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
        
        // 设置添加按钮点击事件
        binding.fabAddMemo.setOnClickListener {
            val intent = Intent(this, MemoDetailActivity::class.java)
            startActivity(intent)
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
}