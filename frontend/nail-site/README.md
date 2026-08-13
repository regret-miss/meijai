# 前台美甲站点

这里是 LikeAdmin 项目内的前台静态源码。发布内容位于 `../../public/nail-site`，由 Spring Boot 与后台管理端共用同一后端服务。

## 开发与发布

在项目根目录执行：

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\sync-nail-site.ps1
```

发布后从 `/nail-site/` 访问前台：

- `/nail-site/首页.html`
- `/nail-site/AI.html`

页面通过 `/api/nail/public` 调用同一套 AI 工作流；不要把 `public/admin`、`public/uploads` 或根 `public/index.html` 当成前台站点的发布目标。
