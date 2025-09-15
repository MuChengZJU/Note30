# 灵感菇 - AI 重构开发计划

本计划将“Note30”应用重构为“灵感菇”AI随手日记的开发过程分解为四个清晰的、循序渐进的里程碑。我们将采用 AI 辅助编程的方式，专注地、一次一个地完成每个任务，直至应用功能完整。

---

## 里程碑 1: 基础重构与品牌更新 (预计用时: 2-3 小时)

**目标**: 搭建一个可运行的、符合新品牌和新数据结构的应用骨架。

- [ ] **任务 1.1: 更新品牌标识**
    -   [ ] 将所有文档和代码中的项目名称从 "Note30" 更新为 "灵感菇"。
    -   [ ] 修改 App 在安卓系统中的显示名称 (`app_name` in `strings.xml`)。
    -   [ ] (可选) 设计并替换 App 图标。

- [ ] **任务 1.2: 改造数据模型 (Room)**
    -   [ ] 重构 `Record` 数据实体 (Entity)，移除 `efficiency`, `mood` 等旧字段。
    -   [ ] 新增 `ai_analysis: String?` (可空) 和 `status: String` 字段。
    -   [ ] 创建一个 Room `Migration` 以处理数据库从旧到新版本的平滑升级，确保用户数据不丢失。
    -   [ ] 更新 `RecordDao` 以匹配新的 `Record` 结构。

- [ ] **任务 1.3: 引入网络依赖**
    -   [ ] 在 `build.gradle.kts` 文件中，添加 `Retrofit`, `OkHttp`, `kotlinx.serialization` 的依赖。

---

## 里程碑 2: AI 配置与服务对接 (预计用时: 3-4 小时)

**目标**: 实现 AI 服务连接的完整配置流程，让 App 具备与大模型对话的能力。

-   [ ] **任务 2.1: 构建 AI 配置界面 (SettingsScreen)**
    -   [ ] 创建一个新的 Composable 屏幕 `SettingsScreen`。
    -   [ ] 设计并实现 API 地址、API 密钥的输入框，以及“获取模型列表”按钮和模型选择下拉菜单。

-   [ ] **任务 2.2: 实现配置的持久化存储**
    -   [ ] 创建 `SettingsViewModel` 用于处理配置页面的逻辑。
    -   [ ] 使用 `DataStore` 或 `EncryptedSharedPreferences` 安全地保存和读取用户的 API 配置。

-   [ ] **任务 2.3: 实现 AI 服务模块 (`AIService`)**
    -   [ ] 创建 `AIService` 类和 Retrofit 接口，定义对 OpenAI 兼容 API 的调用。
    -   [ ] 实现从 `SettingsViewModel` 或 Repository 获取配置并发起网络请求的逻辑。
    -   [ ] 实现调用 `/v1/models` 端点并解析返回模型列表的功能。

---

## 里程碑 3: 核心记录流程改造 (预计用时: 3-4 小时)

**目标**: 将 App 的核心记录流程与 AI 分析功能完全打通。

-   [ ] **任务 3.1: 简化记录界面 (RecordScreen)**
    -   [ ] 移除 `RecordScreen` 上所有旧的输入控件（效率、情绪等）。
    -   [ ] 将界面聚焦于一个大的文本输入框和一个“保存”按钮。

-   [ ] **任务 3.2: 更新记录逻辑 (`RecordViewModel` 和 `RecordRepository`)**
    -   [ ] 修改“保存”按钮的逻辑：
        -   a. 首先，立即将 `Record` (包含原文, 时间戳, status=`PENDING_ANALYSIS`) 存入 Room 数据库。
        -   b. 然后，在协程中异步调用 `AIService` 发起分析请求。
        -   c. 请求成功后，将返回的 JSON 结果更新到数据库中对应记录的 `ai_analysis` 字段，并将 status 更新为 `ANALYZED`。
        -   d. 如果请求失败，将 status 更新为 `ANALYSIS_FAILED`，并提供重试机制。

-   [ ] **任务 3.3: 设计核心 Prompt**
    -   [ ] 在代码中（或资源文件里）定义一个默认的系统 Prompt，指导 AI 将分析结果以稳定、可解析的 JSON 格式返回。

---

## 里程碑 4: 历史回顾与最终完善 (预计用时: 2-3 小时)

**目标**: 以友好的方式向用户展示 AI 的分析结果，并完成应用的核心体验闭环。

-   [ ] **任务 4.1: 改造历史记录列表 (HistoryScreen)**
    -   [ ] 更新 `HistoryViewModel`，使其在获取 `Record` 列表后，能够解析每条记录的 `ai_analysis` JSON 字符串。
    -   [ ] 在 `HistoryScreen` 的列表项 UI 中，除了展示原文，还要以标签、摘要、Emoji 等形式，清晰地展示 AI 分析出的关键信息。
    -   [ ] 为分析失败的记录提供一个明显的标识和重试按钮。

-   [ ] **任务 4.2: 更新 Markdown 导出逻辑**
    -   [ ] 重写 `exportToMarkdown` 函数，使其能够解析 `ai_analysis` 字段，并将结构化的分析结果格式化到 Markdown 文本中。

-   [ ] **任务 4.3: 全面测试与 UI 优化**
    -   [ ] 测试所有核心功能，特别是 AI 配置、网络请求、数据库迁移等环节。
    -   [ ] 优化 UI 细节和整体交互流畅度。
