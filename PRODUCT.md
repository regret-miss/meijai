# Product

<!-- impeccable:product-schema 1 -->

## Platform

web

## Users

- 美甲平台管理员和内容运营人员：在 LikeAdmin 管理后台创建、审核与管理美甲图片资产。
- 美甲网站访客：在现有静态网站的 AI 页面中提交文字或参考图，查看生成任务与结果。

## Product Purpose

在现有 LikeAdmin Java 项目中建立可追溯的美甲 AI 图片创作闭环：支持文生图、图生图、异步任务状态、结果预览和资产采纳，同时让后台与 C 端共用一致的生成能力。

## Positioning

围绕美甲场景提供受控的图片生成和资产治理，而非通用聊天工具；参考图必须来自可用且允许 AI 衍生的资产，AI 结果在采纳前保持候选状态。

## Operating Context

- 管理后台位于本仓库的 Vue 3 + Element Plus 应用，路由和权限由 LikeAdmin 动态菜单管理。
- 服务端为 Java 21、Spring Boot、MyBatis-Plus 和 Sa-Token 的现有模块化单体。
- 消费端为 `C:\Users\15367\Desktop\美甲` 下的静态 HTML/CSS/JavaScript 网站。
- 图片生成使用火山引擎方舟官方 Java SDK；密钥和模型 ID 由运行环境提供。

## Capabilities and Constraints

- 支持文字生成图片和单张参考图生成图片。
- HTTP 创建接口只返回任务，不同步等待模型生成完成；页面轮询任务状态。
- 模型 ID、Provider Key、并发和提示词模板版本不得硬编码。
- 不复用 `la_album` 作为美甲资产表。
- 只有 `ACTIVE`、版权允许且启用 AI 的资产可以作为图生图参考。
- AI 结果必须逐张持久化，未经人工采纳不能成为正式资产。
- 首版在现有应用内实现轻量异步执行，保留后续替换为 Temporal/OSS 的适配边界。

## Brand Commitments

- 管理端继续使用现有 LikeAdmin 视觉系统，并参考即梦的提示词编排面板和比例/清晰度/数量选择方式。
- C 端延续“湘韵甲艺”的克制东方品牌语言，不引入通用 AI 渐变模板风格。

## Evidence on Hand

- `F:\wx\xwechat_files\wxid_3szv25p9ugir22_553b\msg\file\2026-08\美甲平台后端实施方案.md`
- `F:\wx\xwechat_files\wxid_3szv25p9ugir22_553b\msg\file\2026-08\nail-platform-final-implementation-blueprint.md`
- 用户提供的 LikeAdmin 页面和即梦创作面板截图。
- 现有静态网站中的美甲图片和品牌素材；不得虚构客户、生成成功率或商业背书。

## Product Principles

1. 先形成可运行、可追踪的生成与资产闭环，再扩展复杂编排能力。
2. 付费模型调用必须异步、可失败、可重试并保留错误原因。
3. 图片来源、版权、参考关系和采纳动作都必须可审计。
4. 两端共享接口语义，但管理能力与公开访问边界保持分离。
5. 页面优先让用户清楚地描述、配置、提交并理解当前任务状态。
