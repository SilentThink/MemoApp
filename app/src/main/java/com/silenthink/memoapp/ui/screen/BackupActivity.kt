package com.silenthink.memoapp.ui.screen

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.silenthink.memoapp.databinding.ActivityBackupBinding
import com.silenthink.memoapp.ui.viewmodel.BackupViewModel
import com.silenthink.memoapp.util.BackupUtils
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

class BackupActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBackupBinding
    private lateinit var backupViewModel: BackupViewModel
    private lateinit var adapter: BackupFileAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBackupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupRecyclerView()
        setupViewModel()
        setupListeners()
        
        // 加载备份文件列表
        backupViewModel.loadBackupFiles()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "数据备份与恢复"
    }

    private fun setupRecyclerView() {
        adapter = BackupFileAdapter(
            onRestoreClick = { backupFile ->
                showRestoreConfirmDialog(backupFile)
            },
            onDeleteClick = { backupFile ->
                showDeleteConfirmDialog(backupFile)
            },
            onDetailsClick = { backupFile ->
                showBackupDetailsDialog(backupFile)
            }
        )
        
        binding.rvBackupFiles.layoutManager = LinearLayoutManager(this)
        binding.rvBackupFiles.adapter = adapter
    }

    private fun setupViewModel() {
        backupViewModel = ViewModelProvider(this)[BackupViewModel::class.java]

        // 观察加载状态
        backupViewModel.isLoading.observe(this) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.btnCreateBackup.isEnabled = !isLoading
        }

        // 观察备份结果
        backupViewModel.backupResult.observe(this) { result ->
            result?.let {
                if (it.isSuccess) {
                    Toast.makeText(this, "备份创建成功", Toast.LENGTH_SHORT).show()
                    backupViewModel.loadBackupFiles() // 刷新列表
                } else {
                    Toast.makeText(
                        this,
                        "备份创建失败: ${it.exceptionOrNull()?.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
                backupViewModel.clearResults()
            }
        }

        // 观察恢复结果
        backupViewModel.restoreResult.observe(this) { result ->
            result?.let {
                if (it.isSuccess) {
                    val backupData = it.getOrNull()
                    val message = "数据恢复成功！\n" +
                            "恢复了 ${backupData?.memos?.size ?: 0} 条备忘录\n" +
                            "恢复了 ${backupData?.users?.size ?: 0} 个用户"
                    Toast.makeText(this, message, Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(
                        this,
                        "数据恢复失败: ${it.exceptionOrNull()?.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
                backupViewModel.clearResults()
            }
        }

        // 观察备份文件列表
        backupViewModel.backupFiles.observe(this) { files ->
            adapter.submitList(files)
            
            // 显示或隐藏空视图
            if (files.isEmpty()) {
                binding.tvEmptyView.visibility = View.VISIBLE
                binding.rvBackupFiles.visibility = View.GONE
            } else {
                binding.tvEmptyView.visibility = View.GONE
                binding.rvBackupFiles.visibility = View.VISIBLE
            }
        }

        // 观察删除结果
        backupViewModel.deleteResult.observe(this) { result ->
            result?.let {
                if (it.isSuccess && it.getOrNull() == true) {
                    Toast.makeText(this, "备份文件已删除", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "删除失败", Toast.LENGTH_SHORT).show()
                }
                backupViewModel.clearResults()
            }
        }
    }

    private fun setupListeners() {
        binding.btnCreateBackup.setOnClickListener {
            showCreateBackupConfirmDialog()
        }
    }

    private fun showCreateBackupConfirmDialog() {
        AlertDialog.Builder(this)
            .setTitle("创建备份")
            .setMessage("确定要创建数据备份吗？这将备份所有备忘录和用户数据。")
            .setPositiveButton("创建") { _, _ ->
                backupViewModel.createBackup()
            }
            .setNegativeButton("取消", null)
            .show()
    }

    private fun showRestoreConfirmDialog(backupFile: BackupUtils.BackupFileInfo) {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val backupTimeStr = dateFormat.format(backupFile.backupTime)
        
        val message = "确定要从此备份文件恢复数据吗？\n\n" +
                "备份时间: $backupTimeStr\n" +
                "备忘录数量: ${backupFile.memoCount}\n" +
                "用户数量: ${backupFile.userCount}\n\n" +
                "注意：这将添加备份中的数据到现有数据中，不会删除现有数据。"

        AlertDialog.Builder(this)
            .setTitle("恢复数据")
            .setMessage(message)
            .setPositiveButton("恢复") { _, _ ->
                backupViewModel.restoreFromBackup(backupFile.filePath, false)
            }
            .setNeutralButton("替换现有数据") { _, _ ->
                showReplaceDataConfirmDialog(backupFile)
            }
            .setNegativeButton("取消", null)
            .show()
    }

    private fun showReplaceDataConfirmDialog(backupFile: BackupUtils.BackupFileInfo) {
        AlertDialog.Builder(this)
            .setTitle("替换现有数据")
            .setMessage("警告：这将删除所有现有数据并用备份数据替换！\n\n此操作不可撤销，确定继续吗？")
            .setPositiveButton("确定替换") { _, _ ->
                backupViewModel.restoreFromBackup(backupFile.filePath, true)
            }
            .setNegativeButton("取消", null)
            .show()
    }

    private fun showDeleteConfirmDialog(backupFile: BackupUtils.BackupFileInfo) {
        AlertDialog.Builder(this)
            .setTitle("删除备份")
            .setMessage("确定要删除备份文件 \"${backupFile.fileName}\" 吗？\n\n此操作不可撤销。")
            .setPositiveButton("删除") { _, _ ->
                backupViewModel.deleteBackupFile(backupFile.filePath)
            }
            .setNegativeButton("取消", null)
            .show()
    }

    private fun showBackupDetailsDialog(backupFile: BackupUtils.BackupFileInfo) {
        lifecycleScope.launch {
            val result = backupViewModel.getBackupFileDetails(backupFile.filePath)
            if (result.isSuccess) {
                val backupData = result.getOrNull()!!
                val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                
                val details = "备份详情\n\n" +
                        "文件名: ${backupFile.fileName}\n" +
                        "备份时间: ${dateFormat.format(backupFile.backupTime)}\n" +
                        "文件大小: ${formatFileSize(backupFile.fileSize)}\n" +
                        "版本: ${backupFile.version}\n\n" +
                        "数据统计:\n" +
                        "备忘录数量: ${backupFile.memoCount}\n" +
                        "用户数量: ${backupFile.userCount}"

                AlertDialog.Builder(this@BackupActivity)
                    .setTitle("备份详情")
                    .setMessage(details)
                    .setPositiveButton("关闭", null)
                    .show()
            } else {
                Toast.makeText(this@BackupActivity, "无法读取备份文件详情", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun formatFileSize(bytes: Long): String {
        return when {
            bytes < 1024 -> "$bytes B"
            bytes < 1024 * 1024 -> "${bytes / 1024} KB"
            else -> "${bytes / (1024 * 1024)} MB"
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
} 