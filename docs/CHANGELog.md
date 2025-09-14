# 更新日志 (Changelog)

所有此项目的显著变动都将被记录在此文件中。

格式遵循 [Keep a Changelog](https://keepachangelog.com/en/1.0.0/) 规范, 项目版本遵循 [Semantic Versioning](https://semver.org/spec/v2.0.0.html) 规范。

## [未发布]

### 新增

- 项目初始化。
- 产品需求文档 (PRD) v1.0。

### 变更

- 提醒与通知强化（ColorOS 兼容）：
  - 创建高优先级通知渠道：IMPORTANCE_HIGH，启用声音与振动，尝试 bypass DND。
  - 首次提醒采用 CATEGORY_ALARM + FullScreenIntent（需 USE_FULL_SCREEN_INTENT 权限），提升提醒到达可见性。
  - `ReminderWorker`/`FollowUpWorker` 明确设定振动模式与 `DEFAULT_ALL`（兼容旧系统），并统一遵循渠道配置。
  - 在调试页新增系统设置直达入口：应用通知设置、渠道设置、忽略电池优化。
  - 添加 Accompanist Permissions 并在 Android 13+ 主动请求 POST_NOTIFICATIONS 权限。
  - 在 `DebugScreen` 展示 WorkManager 状态与倒计时，支持手动触发提醒。

### 修复

- 修正 `Note30Application` 实现 `Configuration.Provider` 的方式（属性 `workManagerConfiguration`）。
- 统一通知渠道使用与权限声明，修复部分机型仅入栏不弹窗的问题。
