package com.silenthink.memoapp.util

/**
 * 备忘录分类工具类
 */
object CategoryUtils {
    
    // 预定义的分类
    val DEFAULT_CATEGORIES = listOf(
        "默认",
        "工作",
        "生活",
        "学习",
        "健康",
        "旅行",
        "购物",
        "重要"
    )
    
    // 优先级定义
    object Priority {
        const val NORMAL = 0    // 普通
        const val IMPORTANT = 1 // 重要
        const val URGENT = 2    // 紧急
        
        fun getPriorityText(priority: Int): String {
            return when (priority) {
                IMPORTANT -> "重要"
                URGENT -> "紧急"
                else -> "普通"
            }
        }
        
        fun getPriorityColor(priority: Int): String {
            return when (priority) {
                IMPORTANT -> "#FF9800" // 橙色
                URGENT -> "#F44336"    // 红色
                else -> "#4CAF50"      // 绿色
            }
        }
    }
    
    /**
     * 排序选项
     */
    enum class SortOption(val displayName: String) {
        MODIFIED_DATE_DESC("修改时间↓"),
        MODIFIED_DATE_ASC("修改时间↑"),
        CREATED_DATE_DESC("创建时间↓"),
        CREATED_DATE_ASC("创建时间↑"),
        TITLE_ASC("标题A-Z"),
        TITLE_DESC("标题Z-A"),
        PRIORITY_DESC("优先级↓"),
        PRIORITY_ASC("优先级↑"),
        CATEGORY_ASC("分类A-Z")
    }
} 