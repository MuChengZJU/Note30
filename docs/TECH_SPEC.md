# 技术规格说明 (Tech Spec) - Note30

## 1. 技术栈

- **语言**: **Kotlin**
  - *理由*: Google 官方首推的现代化安卓开发语言，相比 Java 更安全、简洁。与 Java 100% 兼容，学习曲线平缓。

- **UI 框架**: **Jetpack Compose**
  - *理由*: 现代声明式 UI 工具包，工作模式与 React 类似。能够以更少的代码、更高的效率构建精美、响应迅速的用户界面，完美契合对 UI 的高要求。

- **架构模式**: **MVVM (Model-View-ViewModel)**
  - *理由*: Google 官方
  推荐的架构模式。通过将界面 (View)、业务逻辑 (ViewModel) 和数据 (Model) 分离，保证代码结构清晰、可测试性强，易于未来扩展。

- **数据库**: **Room**
  - *理由*: 官方持久化库，简化了本地 SQLite 数据库的操作。提供编译时 SQL 校验，提高了数据操作的健壮性。

- **异步处理**: **Kotlin Coroutines (协程)**
  - *理由*: Kotlin 语言内置的轻量级并发方案，用于简化异步代码，如数据库读写、网络请求等，避免界面卡顿。

## 2. 核心模块实现思路

### 2.1 后台计时与提醒服务
- 使用安卓的 `WorkManager` 来实现稳定、省电的后台 30 分钟定时任务；3 分钟后使用 `OneTimeWorkRequest` 进行二次提醒。
- 发送通知遵循以下策略：
  - Android 8.0+：创建高优先级通知渠道（IMPORTANCE_HIGH），启用振动与默认铃声，尝试 `setBypassDnd(true)`。
  - 首次提醒采用 `NotificationCompat.CATEGORY_ALARM` + `setFullScreenIntent` 实现高可见性（需 `USE_FULL_SCREEN_INTENT` 权限）。
  - 旧系统同时调用 `setDefaults(NotificationCompat.DEFAULT_ALL)` 与 `setVibrate(...)` 作为兼容补充。
  - 所有通知统一归于单一渠道 `note30_reminder_channel`。
  - 主动在 Android 13+ 请求 `POST_NOTIFICATIONS` 权限。

### 2.2 数据存储方案
- **实体 (Entity)**: 创建一个 `Record` 数据类，包含 `id`, `timestamp`, `duration`, `efficiency`, `mood`, `content` 等字段。
- **DAO (Data Access Object)**: 定义 `RecordDao` 接口，包含对 `Record` 表的增、删、改、查方法。
- **Database**: 创建 `AppDatabase` 类，继承自 `RoomDatabase`，用于实例化数据库。

### 2.3 UI 与状态管理
- **View (Compose)**: 所有界面都由 Composable 函数构成。
- **ViewModel**: 每个屏幕对应一个 ViewModel，负责处理业务逻辑，并通过 `StateFlow` 或 `LiveData` 向 UI 暴露状态。UI 层观察状态变化并自动重绘。
- **导航**: 使用 `Compose Navigation` 在不同屏幕间进行切换。

### 2.4 Markdown 导出引擎
- 在 ViewModel 中实现一个导出函数，根据用户选择的日期范围从数据库查询 `Record` 列表。
- 遍历列表，使用 `StringBuilder` 或模板引擎将数据格式化为预定义的 Markdown 字符串。
- 通过安卓的 `Share` Intent 或 `ClipboardManager` 将生成的字符串提供给用户。

## 3. 依赖库 (初步)
- **Jetpack Compose**: `androidx.compose.*`
- **ViewModel**: `androidx.lifecycle:lifecycle-viewmodel-ktx`
- **Room**: `androidx.room:room-runtime`, `androidx.room:room-ktx`
- **Navigation**: `androidx.navigation:navigation-compose`
- **Coroutines**: `org.jetbrains.kotlinx:kotlinx-coroutines-android`
- **WorkManager**: `androidx.work:work-runtime-ktx`
- **Accompanist Permissions**: `com.google.accompanist:accompanist-permissions`

## 4. ColorOS / 厂商系统兼容策略
- 在应用内调试页提供系统设置直达入口：
  - 应用通知设置（`ACTION_APP_NOTIFICATION_SETTINGS`）
  - 渠道设置（`ACTION_CHANNEL_NOTIFICATION_SETTINGS`）
  - 忽略电池优化（`ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS`）
- 指引用户开启：允许通知、横幅、铃声、振动、自启动、不限制耗电、后台锁定。
- 通知类别设为 `CATEGORY_ALARM`，并在渠道层面启用声音与振动。
