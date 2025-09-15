# 灵感菇 - 你的 AI 随手日记

灵感菇是一款主打 AI 驱动的极简日记应用。它将记录体验简化到极致——只保留一个纯粹的文本输入框。用户只需随手写下想法、感受或工作内容，应用便会自动通过用户自己配置的大语言模型（LLM）API 进行智能分析，提取情绪、生成摘要、打上关键词标签。

它旨在成为用户最无压力的数字伙伴，安全地捕捉每一个闪过的灵感，并通过 AI 的视角帮助用户更好地理解自己。

## 核心特性

- **极致简约的输入**: 摒弃所有繁杂的选项，提供一个沉浸式的文本输入界面，让记录回归纯粹。
- **强大的 AI 内核**: 连接用户指定的 OpenAI 兼容 API，自动分析每条记录的情绪、状态、关键词，并生成精炼摘要。
- **完全的用户控制与隐私**: 用户的 API Key 和所有日记内容都仅存储在本地设备上，确保了数据的绝对隐私和安全。
- **灵活的模型配置**: 用户可以在设置中自由配置 API 地址、密钥和选择模型，让应用适配自己最称手的 AI 服务。
- **结构化导出**: 支持将包含 AI 分析结果的记录导出为结构化的 Markdown 文件，方便二次利用和长期存档。

## 文档

- **产品需求文档 (PRD)**: [./docs/PRD.md](./docs/PRD.md)
- **技术规格说明**: [./docs/TECH_SPEC.md](./docs/TECH_SPEC.md)
- **开发计划**: [./docs/DEVELOPMENT_PLAN.md](./docs/DEVELOPMENT_PLAN.md)
- **更新日志**: [./docs/CHANGELOG.md](./docs/CHANGELOG.md)

## 项目结构

```
.
├── app/
│   ├── build.gradle.kts
│   └── src/
│       └── main/
│           ├── kotlin/com/example/note30/
│           │   ├── AppDatabase.kt
│           │   ├── HistoryScreen.kt
│           │   ├── HistoryViewModel.kt
│           │   ├── MainActivity.kt
│           │   ├── Record.kt
│           │   ├── RecordDao.kt
│           │   ├── RecordRepository.kt
│           │   ├── RecordScreen.kt
│           │   ├── RecordViewModel.kt
│           │   ├── ReminderManager.kt
│           │   ├── ReminderWorker.kt
│           │   └── ServiceLocator.kt
│           └── res/
├── docs/
│   ├── PRD.md
│   ├── TECH_SPEC.md
│   ├── DEVELOPMENT_PLAN.md
│   └── CHANGELOG.md
├── build.gradle.kts
├── settings.gradle.kts
└── README.md
```

## 在Android Studio中打开项目

1. 打开Android Studio
2. 选择"Open an existing Android Studio project"
3. 导航到项目根目录并选择`build.gradle.kts`文件或项目文件夹
4. 等待Android Studio同步项目依赖

## 测试和构建

### 运行应用

1. 连接Android设备或启动模拟器
2. 点击Android Studio工具栏中的"Run"按钮或按`Shift + F10`
3. 选择目标设备并点击"OK"

### 构建APK

1. 在Android Studio中，选择"Build" > "Build Bundle(s) / APK(s)" > "Build APK(s)"
2. 等待构建完成
3. 点击"locate"链接查看生成的APK文件

### 运行测试

1. 点击Android Studio工具栏中的"Run Tests"按钮或按`Ctrl + Shift + F10`
2. 选择要运行的测试配置

## 技术栈

- **语言**: Kotlin
- **UI 框架**: Jetpack Compose
- **架构模式**: MVVM (Model-View-ViewModel)
- **数据库**: Room
- **异步处理**: Kotlin Coroutines
- **后台任务**: WorkManager