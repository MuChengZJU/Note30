package com.example.inspirationmushroom

import com.example.inspirationmushroom.ai.AIService
import com.example.inspirationmushroom.ai.ChatRequest
import com.example.inspirationmushroom.ai.Message
import com.example.inspirationmushroom.ai.Prompts
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class RecordRepository(private val recordDao: RecordDao) {

    fun getAllRecords() = recordDao.getAllRecords()

    fun getRecordsByDateRange(startTime: Long, endTime: Long) = recordDao.getRecordsByDateRange(startTime, endTime)

    suspend fun insert(record: Record) {
        recordDao.insert(record)
    }

    suspend fun update(record: Record) {
        recordDao.update(record)
    }

    suspend fun delete(record: Record) {
        recordDao.delete(record)
    }

    suspend fun saveRecordAndTriggerAnalysis(record: Record) {
        // 1. 立即保存到数据库
        recordDao.insert(record)

        // 2. 在后台触发AI分析
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // TODO: 从SettingsViewModel获取API配置
                // 暂时使用硬编码的配置进行测试
                val apiUrl = "https://api.openai.com"
                val apiKey = "your-api-key-here"
                val model = "gpt-3.5-turbo"

                val api = AIService.createApi(apiUrl)
                val chatRequest = ChatRequest(
                    model = model,
                    messages = listOf(
                        Message(role = "system", content = Prompts.SYSTEM_PROMPT),
                        Message(role = "user", content = Prompts.createUserMessage(record.content))
                    )
                )

                val response = api.getChatCompletions("Bearer $apiKey", chatRequest)

                if (response.isSuccessful) {
                    val analysisJson = response.body()?.choices?.first()?.message?.content
                    if (analysisJson != null) {
                        // 更新数据库记录
                        val updatedRecord = record.copy(
                            aiAnalysis = analysisJson,
                            status = RecordStatus.ANALYZED
                        )
                        recordDao.update(updatedRecord)
                    }
                } else {
                    // 分析失败
                    val updatedRecord = record.copy(status = RecordStatus.ANALYSIS_FAILED)
                    recordDao.update(updatedRecord)
                }
            } catch (e: Exception) {
                // 分析失败
                val updatedRecord = record.copy(status = RecordStatus.ANALYSIS_FAILED)
                recordDao.update(updatedRecord)
            }
        }
    }
}