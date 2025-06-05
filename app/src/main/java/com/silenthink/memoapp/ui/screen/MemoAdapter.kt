package com.silenthink.memoapp.ui.screen

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.silenthink.memoapp.data.model.Memo
import com.silenthink.memoapp.databinding.ItemMemoBinding
import com.silenthink.memoapp.util.ImageUtils
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale

class MemoAdapter(
    private val onItemClick: (Memo) -> Unit,
    private val onItemDelete: (Memo) -> Unit
) : ListAdapter<Memo, MemoAdapter.MemoViewHolder>(MemoDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MemoViewHolder {
        val binding = ItemMemoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MemoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MemoViewHolder, position: Int) {
        val memo = getItem(position)
        holder.bind(memo)
    }

    inner class MemoViewHolder(private val binding: ItemMemoBinding) : 
        RecyclerView.ViewHolder(binding.root) {
        
        init {
            binding.root.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemClick(getItem(position))
                }
            }
        }
        
        fun bind(memo: Memo) {
            binding.tvTitle.text = memo.title
            binding.tvContent.text = memo.content
            
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
            binding.tvDate.text = dateFormat.format(memo.modifiedDate)
            
            // 显示图片缩略图
            if (memo.imagePath != null && ImageUtils.imageExists(memo.imagePath)) {
                binding.ivThumbnail.visibility = View.VISIBLE
                Glide.with(binding.root.context)
                    .load(File(memo.imagePath))
                    .centerCrop()
                    .into(binding.ivThumbnail)
            } else {
                binding.ivThumbnail.visibility = View.GONE
            }
        }
    }
    
    class MemoDiffCallback : DiffUtil.ItemCallback<Memo>() {
        override fun areItemsTheSame(oldItem: Memo, newItem: Memo): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Memo, newItem: Memo): Boolean {
            return oldItem == newItem
        }
    }
}