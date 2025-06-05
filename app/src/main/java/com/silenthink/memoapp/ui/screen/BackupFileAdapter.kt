package com.silenthink.memoapp.ui.screen

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.silenthink.memoapp.databinding.ItemBackupFileBinding
import com.silenthink.memoapp.util.BackupUtils
import java.text.SimpleDateFormat
import java.util.Locale

class BackupFileAdapter(
    private val onRestoreClick: (BackupUtils.BackupFileInfo) -> Unit,
    private val onDeleteClick: (BackupUtils.BackupFileInfo) -> Unit,
    private val onDetailsClick: (BackupUtils.BackupFileInfo) -> Unit
) : ListAdapter<BackupUtils.BackupFileInfo, BackupFileAdapter.BackupFileViewHolder>(BackupFileDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BackupFileViewHolder {
        val binding = ItemBackupFileBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return BackupFileViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BackupFileViewHolder, position: Int) {
        val backupFile = getItem(position)
        holder.bind(backupFile)
    }

    inner class BackupFileViewHolder(private val binding: ItemBackupFileBinding) : 
        RecyclerView.ViewHolder(binding.root) {
        
        fun bind(backupFile: BackupUtils.BackupFileInfo) {
            val dateFormat = SimpleDateFormat("MM-dd HH:mm", Locale.getDefault())
            val fullDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            
            binding.tvFileName.text = backupFile.fileName
            binding.tvBackupTime.text = dateFormat.format(backupFile.backupTime)
            binding.tvMemoCount.text = "${backupFile.memoCount} 条备忘录"
            binding.tvFileSize.text = formatFileSize(backupFile.fileSize)
            
            // 设置完整的备份时间作为副标题
            binding.tvFullTime.text = fullDateFormat.format(backupFile.backupTime)
            
            // 设置按钮点击事件
            binding.btnRestore.setOnClickListener {
                onRestoreClick(backupFile)
            }
            
            binding.btnDelete.setOnClickListener {
                onDeleteClick(backupFile)
            }
            
            binding.btnDetails.setOnClickListener {
                onDetailsClick(backupFile)
            }
            
            // 设置卡片点击事件（显示详情）
            binding.root.setOnClickListener {
                onDetailsClick(backupFile)
            }
        }
        
        private fun formatFileSize(bytes: Long): String {
            return when {
                bytes < 1024 -> "$bytes B"
                bytes < 1024 * 1024 -> "${bytes / 1024} KB"
                else -> "${bytes / (1024 * 1024)} MB"
            }
        }
    }
    
    class BackupFileDiffCallback : DiffUtil.ItemCallback<BackupUtils.BackupFileInfo>() {
        override fun areItemsTheSame(
            oldItem: BackupUtils.BackupFileInfo, 
            newItem: BackupUtils.BackupFileInfo
        ): Boolean {
            return oldItem.filePath == newItem.filePath
        }

        override fun areContentsTheSame(
            oldItem: BackupUtils.BackupFileInfo, 
            newItem: BackupUtils.BackupFileInfo
        ): Boolean {
            return oldItem == newItem
        }
    }
} 