package com.silenthink.memoapp.ui.screen

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.silenthink.memoapp.databinding.ActivityMemoDetailBinding
import com.silenthink.memoapp.ui.viewmodel.MemoViewModel
import com.silenthink.memoapp.util.ImageUtils
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MemoDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMemoDetailBinding
    private lateinit var viewModel: MemoViewModel
    private var memoId: Long = 0
    private var isNewMemo = true
    private var currentImagePath: String? = null

    companion object {
        const val EXTRA_MEMO_ID = "extra_memo_id"
    }

    // 图片选择器
    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            handleImageSelection(it)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMemoDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 设置Toolbar
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // 初始化ViewModel
        viewModel = ViewModelProvider(this)[MemoViewModel::class.java]

        // 检查是否是编辑现有备忘录
        if (intent.hasExtra(EXTRA_MEMO_ID)) {
            memoId = intent.getLongExtra(EXTRA_MEMO_ID, 0)
            isNewMemo = false
            loadMemoData()
        } else {
            // 新建备忘录
            supportActionBar?.title = "新建备忘录"
            binding.tvLastModified.text = "创建于 ${getCurrentTimeFormatted()}"
        }

        // 设置保存按钮
        binding.btnSave.setOnClickListener {
            saveMemo()
        }

        // 设置图片选择按钮
        binding.btnSelectImage.setOnClickListener {
            imagePickerLauncher.launch("image/*")
        }

        // 设置图片移除按钮
        binding.btnRemoveImage.setOnClickListener {
            removeCurrentImage()
        }
    }

    private fun loadMemoData() {
        viewModel.getMemoById(memoId).observe(this) { memo ->
            memo?.let {
                binding.etTitle.setText(it.title)
                binding.etContent.setText(it.content)
                
                val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                binding.tvLastModified.text = "最后修改: ${dateFormat.format(it.modifiedDate)}"
                
                // 加载图片
                currentImagePath = it.imagePath
                updateImageDisplay()
            }
        }
    }

    private fun saveMemo() {
        val title = binding.etTitle.text.toString().trim()
        val content = binding.etContent.text.toString().trim()

        if (title.isEmpty()) {
            Toast.makeText(this, "标题不能为空", Toast.LENGTH_SHORT).show()
            return
        }

        if (isNewMemo) {
            viewModel.insert(title, content, currentImagePath)
            Toast.makeText(this, "备忘录已保存", Toast.LENGTH_SHORT).show()
        } else {
            viewModel.getMemoById(memoId).value?.let {
                val updatedMemo = it.copy(
                    title = title, 
                    content = content,
                    imagePath = currentImagePath
                )
                viewModel.update(updatedMemo)
                Toast.makeText(this, "备忘录已更新", Toast.LENGTH_SHORT).show()
            }
        }
        
        finish()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                // 如果有未保存的更改，提示用户
                if (hasUnsavedChanges()) {
                    showUnsavedChangesDialog()
                } else {
                    finish()
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onBackPressed() {
        if (hasUnsavedChanges()) {
            showUnsavedChangesDialog()
        } else {
            super.onBackPressed()
        }
    }

    private fun hasUnsavedChanges(): Boolean {
        // 检查是否有未保存的更改
        if (isNewMemo) {
            return binding.etTitle.text.toString().isNotEmpty() || 
                   binding.etContent.text.toString().isNotEmpty()
        } else {
            viewModel.getMemoById(memoId).value?.let {
                return it.title != binding.etTitle.text.toString() || 
                       it.content != binding.etContent.text.toString()
            }
        }
        return false
    }

    private fun showUnsavedChangesDialog() {
        AlertDialog.Builder(this)
            .setTitle("未保存的更改")
            .setMessage("您有未保存的更改，是否要保存？")
            .setPositiveButton("保存") { _, _ -> saveMemo() }
            .setNegativeButton("丢弃") { _, _ -> finish() }
            .setNeutralButton("取消", null)
            .show()
    }

    private fun getCurrentTimeFormatted(): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        return dateFormat.format(Date())
    }

    /**
     * 处理选择的图片
     */
    private fun handleImageSelection(uri: Uri) {
        val savedPath = ImageUtils.saveImageToInternalStorage(this, uri)
        if (savedPath != null) {
            // 删除旧图片
            currentImagePath?.let { ImageUtils.deleteImage(it) }
            
            currentImagePath = savedPath
            updateImageDisplay()
            Toast.makeText(this, "图片已添加", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "图片添加失败", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * 更新图片显示状态
     */
    private fun updateImageDisplay() {
        if (currentImagePath != null && ImageUtils.imageExists(currentImagePath)) {
            binding.ivImage.visibility = View.VISIBLE
            binding.btnRemoveImage.visibility = View.VISIBLE
            
            Glide.with(this)
                .load(File(currentImagePath!!))
                .centerCrop()
                .into(binding.ivImage)
        } else {
            binding.ivImage.visibility = View.GONE
            binding.btnRemoveImage.visibility = View.GONE
            currentImagePath = null
        }
    }

    /**
     * 移除当前图片
     */
    private fun removeCurrentImage() {
        AlertDialog.Builder(this)
            .setTitle("移除图片")
            .setMessage("确定要移除这张图片吗？")
            .setPositiveButton("移除") { _, _ ->
                currentImagePath?.let { ImageUtils.deleteImage(it) }
                currentImagePath = null
                updateImageDisplay()
                Toast.makeText(this, "图片已移除", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("取消", null)
            .show()
    }
} 