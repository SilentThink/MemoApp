package com.silenthink.memoapp.ui.screen

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.silenthink.memoapp.data.model.Memo
import com.silenthink.memoapp.databinding.ActivityMemoDetailBinding
import com.silenthink.memoapp.ui.viewmodel.MemoViewModel
import com.silenthink.memoapp.util.CategoryUtils
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
    private var currentMemo: Memo? = null
    private var selectedCategory: String = "默认"
    private var selectedPriority: Int = CategoryUtils.Priority.NORMAL

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

        // 设置分类和优先级选择器
        setupCategoryDropdown()
        setupPriorityDropdown()

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
        
        // 设置AI分类建议按钮
        binding.btnAiSuggest.setOnClickListener {
            requestAiCategorySuggestion()
        }
        
        // 设置AI建议应用按钮
        binding.btnApplyAi.setOnClickListener {
            applyAiSuggestion()
        }
        
        // 设置AI建议忽略按钮
        binding.btnIgnoreAi.setOnClickListener {
            hideAiSuggestion()
        }
        
        // 观察AI分类状态
        setupAiObservers()
    }

    private fun setupCategoryDropdown() {
        val categories = CategoryUtils.DEFAULT_CATEGORIES
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, categories)
        binding.actvCategory.setAdapter(adapter)
        binding.actvCategory.setText(selectedCategory, false)
        
        binding.actvCategory.setOnItemClickListener { _, _, position, _ ->
            selectedCategory = categories[position]
        }
    }

    private fun setupPriorityDropdown() {
        val priorities = listOf("普通", "重要", "紧急")
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, priorities)
        binding.actvPriority.setAdapter(adapter)
        binding.actvPriority.setText(CategoryUtils.Priority.getPriorityText(selectedPriority), false)
        
        binding.actvPriority.setOnItemClickListener { _, _, position, _ ->
            selectedPriority = when (position) {
                1 -> CategoryUtils.Priority.IMPORTANT
                2 -> CategoryUtils.Priority.URGENT
                else -> CategoryUtils.Priority.NORMAL
            }
        }
    }

    private fun loadMemoData() {
        viewModel.getMemoById(memoId).observe(this) { memo ->
            memo?.let {
                currentMemo = it
                binding.etTitle.setText(it.title)
                binding.etContent.setText(it.content)
                
                // 设置分类和优先级
                selectedCategory = it.category
                selectedPriority = it.priority
                binding.actvCategory.setText(selectedCategory, false)
                binding.actvPriority.setText(CategoryUtils.Priority.getPriorityText(selectedPriority), false)
                
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
            viewModel.insert(title, content, currentImagePath, selectedCategory, selectedPriority)
            Toast.makeText(this, "备忘录已保存", Toast.LENGTH_SHORT).show()
        } else {
            currentMemo?.let {
                val updatedMemo = it.copy(
                    title = title, 
                    content = content,
                    imagePath = currentImagePath,
                    category = selectedCategory,
                    priority = selectedPriority
                )
                viewModel.update(updatedMemo)
                Toast.makeText(this, "备忘录已更新", Toast.LENGTH_SHORT).show()
            } ?: run {
                Toast.makeText(this, "更新失败：数据未加载完成", Toast.LENGTH_SHORT).show()
                return
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

    private fun hasUnsavedChanges(): Boolean {
        // 检查是否有未保存的更改
        if (isNewMemo) {
            return binding.etTitle.text.toString().isNotEmpty() || 
                   binding.etContent.text.toString().isNotEmpty()
        } else {
            currentMemo?.let {
                return it.title != binding.etTitle.text.toString() || 
                       it.content != binding.etContent.text.toString() ||
                       it.category != selectedCategory ||
                       it.priority != selectedPriority
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
        currentImagePath?.let { 
            ImageUtils.deleteImage(it)
        }
        currentImagePath = null
        updateImageDisplay()
        Toast.makeText(this, "图片已移除", Toast.LENGTH_SHORT).show()
    }

    private fun setupAiObservers() {
        // 观察AI加载状态
        viewModel.isAiCategoryLoading.observe(this) { isLoading ->
            binding.pbAiLoading.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.btnAiSuggest.isEnabled = !isLoading
        }
        
        // 观察AI分类建议
        viewModel.aiCategorySuggestion.observe(this) { suggestion ->
            if (suggestion != null) {
                showAiSuggestion(suggestion)
            }
        }
        
        // 观察AI错误
        viewModel.aiCategoryError.observe(this) { error ->
            if (error != null) {
                Toast.makeText(this, error, Toast.LENGTH_LONG).show()
                viewModel.clearAiSuggestion()
            }
        }
    }
    
    private fun requestAiCategorySuggestion() {
        val title = binding.etTitle.text.toString().trim()
        val content = binding.etContent.text.toString().trim()
        
        if (title.isEmpty() && content.isEmpty()) {
            Toast.makeText(this, "请先输入标题或内容", Toast.LENGTH_SHORT).show()
            return
        }
        
        viewModel.suggestCategory(title, content)
    }
    
    private fun showAiSuggestion(suggestion: com.silenthink.memoapp.data.model.CategorySuggestion) {
        binding.cvAiSuggestion.visibility = View.VISIBLE
        binding.tvAiCategory.text = suggestion.category
        binding.tvAiConfidence.text = getString(com.silenthink.memoapp.R.string.ai_category_confidence, suggestion.confidence * 100)
        binding.tvAiReason.text = getString(com.silenthink.memoapp.R.string.ai_category_reason, suggestion.reason)
    }
    
    private fun applyAiSuggestion() {
        val suggestion = viewModel.aiCategorySuggestion.value
        if (suggestion != null) {
            selectedCategory = suggestion.category
            binding.actvCategory.setText(selectedCategory, false)
            hideAiSuggestion()
            Toast.makeText(this, "已应用AI分类建议: ${suggestion.category}", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun hideAiSuggestion() {
        binding.cvAiSuggestion.visibility = View.GONE
        viewModel.clearAiSuggestion()
    }
} 