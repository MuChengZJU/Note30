# 技术规格说明 (Tech Spec) - 灵感菇

## 1. 技术栈

- **语言**: **Kotlin**
  - *理由*: Google 官方首推的现代化安卓开发语言，相比 Java 更安全、简洁。
- **UI 框架**: **Jetpack Compose**
  - *理由*: 现代声明式 UI 工具包，能够以更少的代码、更高的效率构建精美、响应迅速的用户界面。
- **架构模式**: **MVVM (Model-View-ViewModel)**
  - *理由*: Google 官方推荐的架构模式。通过将界面 (View)、业务逻辑 (ViewModel) 和数据 (Model) 分离，保证代码结构清晰、可测试性强。
- **数据库**: **Room**
  - *理由*: 官方持久化库，对 SQLite 提供了更高层级的抽象，提供编译时 SQL 校验。
- **异步处理**: **Kotlin Coroutines (协程)**
  - *理由*: Kotlin 语言内置的轻量级并发方案，用于简化数据库读写、网络请求等异步代码。
- **网络请求**: **Retrofit2 & OkHttp3**
  - *理由*: 业界标准、稳定可靠的 HTTP 客户端，与协程集成良好，方便定义和调用 REST API。
- **数据序列化**: **kotlinx.serialization**
  - *理由*: Kotlin 官方的序列化库，性能优秀，能方便地在 Kotlin 对象和 JSON 之间进行转换。
- **依赖注入**: **Hilt (可选)**
  - *理由*: 简化依赖注入的实现，方便管理 `Repository`, `AIService` 等组件的生命周期与依赖关系。

## 2. 核心模块实现思路

### 2.1 AI 分析服务 (`AIService`)
- **API 定义**: 使用 Retrofit 接口定义 OpenAI 兼容的 Chat Completions API (`/v1/chat/completions`)。
- **请求构建**:
    - `Authorization`: 从用户配置中读取 API Key，并添加到请求头的 `Authorization: Bearer <key>` 中。
    - `Body`: 构建请求体，包含用户选择的模型名称和 `messages` 数组。
    - `Prompt`: `messages` 数组将包含一个 `system` 角色的消息（用于设定 AI 的任务和输出格式，如强制要求返回 JSON）和一个 `user` 角色的消息（用户的日记原文）。
- **响应处理**:
    - 定义与 API 响应匹配的 Kotlin 数据类（使用 `@Serializable`）。
    - 使用 `kotlinx.serialization` 解析返回的 JSON 字符串，提取出 AI 生成的内容。
- **配置管理**: AI 的 URL 和 Key 将通过 `SharedPreferences` 或 `DataStore` 进行本地加密存储。

### 2.2 数据存储方案
- **实体 (Entity)**: `Record` 数据类将重构为：
    - `id: Int` (主键)
    - `timestamp: Long` (时间戳)
    - `content: String` (用户输入的原文)
    - `ai_analysis: String?` (可空，用于存储 AI 返回的完整 JSON 字符串)
    - `status: String` (记录状态，如 `PENDING_ANALYSIS`, `ANALYZED`, `ANALYSIS_FAILED`)
- **DAO (Data Access Object)**: `RecordDao` 接口将更新，以支持对新 `Record` 结构的操作。
- **数据库迁移**: 需要为 Room 创建一个 `Migration`，以平滑地从旧表结构过渡到新表结构，避免用户数据丢失。

### 2.3 UI 与状态管理
- **ViewModel**:
    - `RecordViewModel`: 负责保存用户输入，并调用 `Repository` 触发 AI 分析流程。
    - `HistoryViewModel`: 负责从 `Repository` 获取所有记录，并解析 `ai_analysis` JSON 字符串，将其转换为 UI 可以直接展示的数据模型。
    - `SettingsViewModel`: 负责处理 AI 配置的保存、读取和测试逻辑。
- **Repository**: `RecordRepository` 作为单一数据来源，封装与 `RecordDao` 和 `AIService` 的交互。例如，`saveRecordAndAnalyze` 方法会先将记录插入本地数据库，然后调用 `AIService`，最后更新数据库中的分析结果。

### 2.4 Markdown 导出引擎
- 在 `HistoryViewModel` 中实现导出函数。
- 查询指定日期范围的 `Record` 列表。
- 遍历列表，对于每条记录，检查 `ai_analysis` 字段。如果该字段不为空，则使用 `kotlinx.serialization` 将其从 JSON 字符串解析为数据对象。
- 使用 `StringBuilder` 将原文和解析后的 AI 分析数据格式化为预定义的 Markdown 字符串。
- 通过安卓的 `Share` Intent 或 `ClipboardManager` 将生成的字符串提供给用户。

## 3. 依赖库 (初步)
- **Jetpack Compose**: `androidx.compose.*`
- **ViewModel**: `androidx.lifecycle:lifecycle-viewmodel-ktx`
- **Room**: `androidx.room:room-runtime`, `androidx.room:room-ktx`
- **Navigation**: `androidx.navigation:navigation-compose`
- **Coroutines**: `org.jetbrains.kotlinx:kotlinx-coroutines-android`
- **Retrofit**: `com.squareup.retrofit2:retrofit`, `com.squareup.okhttp3:okhttp3`
- **kotlinx.serialization**: `org.jetbrains.kotlinx:kotlinx-serialization-json`, `com.jakewharton.retrofit:retrofit2-kotlinx-serialization-converter`
- **DataStore/EncryptedSharedPreferences**: `androidx.datastore:datastore-preferences` or `androidx.security:security-crypto`
 
## 5. 详细编码方案

本章节将提供从 Note30 重构为灵感菇的具体编码步骤和代码示例。

### 5.1 Gradle 依赖配置

在 `app/build.gradle.kts` 的 `dependencies` 代码块中，添加以下网络与序列化相关的库：

```kotlin
// Retrofit & OkHttp
implementation("com.squareup.retrofit2:retrofit:2.9.0")
implementation("com.squareup.okhttp3:okhttp3:4.12.0")
implementation("com.squareup.okhttp3:logging-interceptor:4.12.0") // 用于调试

// Kotlinx Serialization
implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")
implementation("com.jakewharton.retrofit:retrofit2-kotlinx-serialization-converter:1.0.0")

// DataStore for API Key Storage
implementation("androidx.datastore:datastore-preferences:1.0.0")
```

### 5.2 数据库迁移 (Room Migration)

这是本次升级最关键的一步，用于保证用户旧数据的平滑过渡。

**1. 修改 `Record.kt` 实体:**

```kotlin
// app/src/main/kotlin/com/example/note30/Record.kt

package com.example.note30

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

enum class RecordStatus {
    PENDING_ANALYSIS,
    ANALYZED,
    ANALYSIS_FAILED
}

@Entity(tableName = "records")
data class Record(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Date = Date(),
    val content: String,
    val aiAnalysis: String? = null, // 存储 AI 返回的 JSON
    val status: RecordStatus = RecordStatus.PENDING_ANALYSIS
)
```
*   **改动点**:
    *   移除了 `efficiency` 和 `mood`。
    *   `id` 改为 `autoGenerate = true` 以避免潜在的冲突。
    *   新增 `aiAnalysis: String?` 和 `status: RecordStatus`。

**2. 在 `AppDatabase.kt` 中定义并添加迁移逻辑:**

```kotlin
// app/src/main/kotlin/com/example/note30/AppDatabase.kt

// ... imports ...
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

// ...

@Database(entities = [Record::class], version = 2) // 版本号从 1 -> 2
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    // ... Dao abstract fun ...

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // 1. 添加新列，并允许为空
                database.execSQL("ALTER TABLE records ADD COLUMN aiAnalysis TEXT")
                database.execSQL("ALTER TABLE records ADD COLUMN status TEXT NOT NULL DEFAULT 'PENDING_ANALYSIS'")
                
                // 2. (可选) 可以创建一个临时表来重命名和删除旧列，但更简单的方式是直接在代码层面放弃旧列
                // 为了简化，我们只添加新列，旧的 efficiency 和 mood 列将不再被实体类映射，相当于被废弃。
            }
        }

        // ... Volatile instance and getInstance() ...
        fun getInstance(context: Context): AppDatabase {
            return Room.databaseBuilder(context, AppDatabase::class.java, "note30-db")
                .addMigrations(MIGRATION_1_2) // 应用迁移
                .build()
        }
    }
}
```
*   **改动点**:
    *   `@Database` 注解的 `version` 升级到 2。
    *   创建 `MIGRATION_1_2` 对象，使用 `ALTER TABLE` 语句为 `records` 表添加 `aiAnalysis` 和 `status` 两个新列。
    *   在构建数据库实例时，通过 `.addMigrations(MIGRATION_1_2)` 应用这个迁移计划。

### 5.3 AI 服务层 (`AIService`)

创建新的文件来处理网络请求。

**1. 定义数据类 (DTOs):**

```kotlin
// app/src/main/kotlin/com/example/note30/ai/DTOs.kt

package com.example.note30.ai
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ChatRequest(
    val model: String,
    val messages: List<Message>,
    @SerialName("response_format") val responseFormat: ResponseFormat? = null // For JSON mode
)

@Serializable
data class Message(
    val role: String, // "system", "user"
    val content: String
)

@Serializable
data class ResponseFormat(
    val type: String = "json_object"
)

@Serializable
data class ChatResponse(
    val choices: List<Choice>
)

@Serializable
data class Choice(
    val message: Message
)
```

**2. 创建 Retrofit 接口 `OpenAIApi.kt`:**
```kotlin
// app/src/main/kotlin/com/example/note30/ai/OpenAIApi.kt
package com.example.note30.ai

import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.Response

interface OpenAIApi {
    @POST("v1/chat/completions")
    suspend fun getChatCompletions(
        @Header("Authorization") apiKey: String,
        @Body request: ChatRequest
    ): Response<ChatResponse>
}
```

**3. 创建服务实例 `AIService.kt`:**
```kotlin
// app/src/main/kotlin/com/example/note30/ai/AIService.kt
// ... imports ...
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit

object AIService {
    fun createApi(baseUrl: String): OpenAIApi {
        val client = OkHttpClient.Builder()
            .addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
            .build()
            
        val retrofit = Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(client)
            .addConverterFactory(Json.asConverterFactory("application/json".toMediaType()))
            .build()
            
        return retrofit.create(OpenAIApi::class.java)
    }
}
```

### 5.4 更新 `RecordRepository`

`RecordRepository` 将成为连接 ViewModel、数据库和 AI 服务的枢纽。

```kotlin
// app/src/main/kotlin/com/example/note30/RecordRepository.kt

class RecordRepository(private val recordDao: RecordDao) {

    // ... fun getAllRecords() ...

    suspend fun saveRecordAndTriggerAnalysis(record: Record) {
        // 1. 立即保存到数据库，UI可以马上刷新
        recordDao.insert(record)

        // 2. 在后台开始分析 (这里需要获取用户配置的 API Key 和 URL)
        // val userApiKey = ...
        // val userApiUrl = ...
        // val userModel = ...
        
        // try {
        //     val api = AIService.createApi(userApiUrl)
        //     val prompt = "..." // 设计好的 System Prompt
        //     val request = ChatRequest(...)
        //     val response = api.getChatCompletions("Bearer $userApiKey", request)
        //     
        //     if (response.isSuccessful) {
        //         val analysisJson = response.body()?.choices?.first()?.message?.content
        //         if (analysisJson != null) {
        //             // 3. 更新数据库记录
        //             val updatedRecord = record.copy(aiAnalysis = analysisJson, status = RecordStatus.ANALYZED)
        //             recordDao.update(updatedRecord)
        //         }
        //     } else {
        //         val updatedRecord = record.copy(status = RecordStatus.ANALYSIS_FAILED)
        //         recordDao.update(updatedRecord)
        //     }
        // } catch (e: Exception) {
        //     val updatedRecord = record.copy(status = RecordStatus.ANALYSIS_FAILED)
        //     recordDao.update(updatedRecord)
        // }
    }
}
```
*   **注意**: 上述代码为伪代码，完整的实现需要接入用户配置的读取逻辑。
