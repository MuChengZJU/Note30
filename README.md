# Note30 - 30分钟回顾App

Note30 是一款极简的安卓应用，旨在通过每 30 分钟一次的周期性提醒，帮助用户对抗"时间黑洞"，并记录工作与情绪，以便后续复盘分析。

## 核心特性

- **周期性提醒**：每 30 分钟通过温和的系统通知提醒用户进行记录。
- **快速记录**：提供极简的输入界面，支持文本、效率评级和可选的情绪标签。
- **免打扰设计**：支持"暂停后总结"模式，确保在休息或会议期间不受打扰，同时保证时间记录的完整性。
- **强大的导出功能**：支持将任意日期范围的记录导出为结构化的 Markdown 文件，方便使用 AI 等外部工具进行深度分析。

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