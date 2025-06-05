package com.silenthink.memoapp.data.service

import android.content.Context
import com.google.gson.Gson
import com.silenthink.memoapp.data.api.DeepSeekApiService
import com.silenthink.memoapp.data.model.*
import com.silenthink.memoapp.util.ApiConfig
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class AiCategoryService(private val context: Context) {
    
    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        })
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()
    
    private val retrofit = Retrofit.Builder()
        .baseUrl(ApiConfig.DEEPSEEK_BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    
    private val apiService = retrofit.create(DeepSeekApiService::class.java)
    
    // 预定义的分类列表
    private val predefinedCategories = listOf(
        "工作", "学习", "生活", "健康", "旅行", "购物", 
        "娱乐", "人际关系", "财务", "计划", "想法", "其他"
    )
    
    suspend fun suggestCategory(title: String, content: String): Result<CategorySuggestion> {
        val apiKey = ApiConfig.getDeepSeekApiKey(context)
        
        if (!ApiConfig.isApiKeyConfigured(context)) {
            return Result.failure(Exception("API密钥未配置，请在设置中配置DeepSeek API密钥"))
        }
        
        return try {
            val prompt = buildPrompt(title, content)
            val request = DeepSeekRequest(
                model = ApiConfig.DEEPSEEK_MODEL,
                messages = listOf(
                    Message(role = "user", content = prompt)
                ),
                temperature = 0.3f,
                max_tokens = 100
            )
            
            val response = apiService.chatCompletion(
                authorization = "Bearer $apiKey",
                request = request
            )
            
            if (response.isSuccessful && response.body() != null) {
                val deepSeekResponse = response.body()!!
                val aiResponse = deepSeekResponse.choices.firstOrNull()?.message?.content
                
                if (aiResponse != null) {
                    parseAiResponse(aiResponse)
                } else {
                    Result.failure(Exception("AI响应为空"))
                }
            } else {
                val errorMsg = when (response.code()) {
                    401 -> "API密钥无效，请检查密钥是否正确"
                    429 -> "API调用频率过高，请稍后再试"
                    500 -> "服务器内部错误，请稍后再试"
                    else -> "API调用失败: ${response.code()} ${response.message()}"
                }
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    private fun buildPrompt(title: String, content: String): String {
        val categoriesStr = predefinedCategories.joinToString(", ")
        return """
            请分析以下备忘录内容，并从这些分类中选择最合适的一个：$categoriesStr
            
            备忘录标题：$title
            备忘录内容：$content
            
            请以JSON格式回复，包含以下字段：
            - category: 最合适的分类
            - confidence: 置信度（0-1之间的小数）
            - reason: 选择该分类的简短理由
            
            示例格式：
            {"category": "工作", "confidence": 0.85, "reason": "包含会议和项目相关内容"}
        """.trimIndent()
    }
    
    private fun parseAiResponse(aiResponse: String): Result<CategorySuggestion> {
        return try {
            // 尝试提取JSON部分
            val jsonStart = aiResponse.indexOf("{")
            val jsonEnd = aiResponse.lastIndexOf("}") + 1
            
            if (jsonStart != -1 && jsonEnd > jsonStart) {
                val jsonStr = aiResponse.substring(jsonStart, jsonEnd)
                val gson = Gson()
                val suggestion = gson.fromJson(jsonStr, CategorySuggestion::class.java)
                
                // 验证分类是否在预定义列表中
                val validCategory = if (predefinedCategories.contains(suggestion.category)) {
                    suggestion.category
                } else {
                    // 使用关键词匹配找到最相似的分类
                    findSimilarCategory(suggestion.category) ?: "其他"
                }
                
                Result.success(
                    CategorySuggestion(
                        category = validCategory,
                        confidence = suggestion.confidence.coerceIn(0f, 1f),
                        reason = suggestion.reason
                    )
                )
            } else {
                // 如果无法解析JSON，尝试简单的关键词匹配
                val category = extractCategoryFromText(aiResponse)
                Result.success(
                    CategorySuggestion(
                        category = category,
                        confidence = 0.5f,
                        reason = "基于关键词分析"
                    )
                )
            }
        } catch (e: Exception) {
            // 降级到关键词匹配
            val category = extractCategoryFromText(aiResponse)
            Result.success(
                CategorySuggestion(
                    category = category,
                    confidence = 0.3f,
                    reason = "解析失败，使用关键词匹配"
                )
            )
        }
    }
    
    private fun findSimilarCategory(suggestedCategory: String): String? {
        // 简单的相似度匹配
        val suggestions = mapOf(
            "工作" to listOf("工作", "办公", "会议", "项目", "任务", "职场"),
            "学习" to listOf("学习", "教育", "课程", "考试", "作业", "知识"),
            "生活" to listOf("生活", "日常", "家庭", "家务", "生活用品"),
            "健康" to listOf("健康", "医疗", "运动", "锻炼", "养生", "身体"),
            "旅行" to listOf("旅行", "旅游", "出行", "度假", "景点"),
            "购物" to listOf("购物", "买", "商店", "商品", "消费"),
            "娱乐" to listOf("娱乐", "电影", "游戏", "音乐", "休闲"),
            "人际关系" to listOf("朋友", "家人", "同事", "聚会", "社交"),
            "财务" to listOf("财务", "金钱", "理财", "投资", "支出", "收入"),
            "计划" to listOf("计划", "安排", "日程", "目标", "待办"),
            "想法" to listOf("想法", "创意", "灵感", "思考", "点子")
        )
        
        for ((category, keywords) in suggestions) {
            if (keywords.any { suggestedCategory.contains(it, ignoreCase = true) }) {
                return category
            }
        }
        return null
    }
    
    private fun extractCategoryFromText(text: String): String {
        val categoryKeywords = mapOf(
            "工作" to listOf("工作", "办公", "会议", "项目", "任务", "职场", "同事", "上班"),
            "学习" to listOf("学习", "教育", "课程", "考试", "作业", "知识", "书籍", "阅读"),
            "生活" to listOf("生活", "日常", "家庭", "家务", "做饭", "清洁", "居住"),
            "健康" to listOf("健康", "医疗", "运动", "锻炼", "养生", "身体", "医院", "药物"),
            "旅行" to listOf("旅行", "旅游", "出行", "度假", "景点", "酒店", "机票"),
            "购物" to listOf("购物", "买", "商店", "商品", "消费", "超市", "网购"),
            "娱乐" to listOf("娱乐", "电影", "游戏", "音乐", "休闲", "看剧", "娱乐"),
            "人际关系" to listOf("朋友", "家人", "同事", "聚会", "社交", "约会", "聊天"),
            "财务" to listOf("财务", "金钱", "理财", "投资", "支出", "收入", "账单", "预算"),
            "计划" to listOf("计划", "安排", "日程", "目标", "待办", "规划", "准备"),
            "想法" to listOf("想法", "创意", "灵感", "思考", "点子", "记录", "感想")
        )
        
        for ((category, keywords) in categoryKeywords) {
            if (keywords.any { text.contains(it, ignoreCase = true) }) {
                return category
            }
        }
        
        return "其他"
    }
} 