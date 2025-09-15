# 更新日志 (Changelog)

所有此项目的显著变动都将被记录在此文件中。

格式遵循 [Keep a Changelog](https://keepachangelog.com/en/1.0.0/) 规范, 项目版本遵循 [Semantic Versioning](https://semver.org/spec/v2.0.0.html) 规范。

## [未发布]

### 变更

- **项目重塑为 "灵感菇"**:
  - 核心功能转向由用户配置的 AI 大模型进行文本分析，取代了手动的效率和情绪打分。
  - 数据库模型重构，引入 `ai_analysis` 字段以 JSON 格式存储分析结果。
  - 简化主录入界面，聚焦于纯文本输入。
  - 新增 AI 配置界面，支持自定义 OpenAI 兼容 API 端点、密钥和模型。

### 新增

- 项目初始化。
- 产品需求文档 (PRD) v1.0。

### 修复

- 修正 `Note30Application` 实现 `Configuration.Provider` 的方式（属性 `workManagerConfiguration`）。
- 统一通知渠道使用与权限声明，修复部分机型仅入栏不弹窗的问题。
