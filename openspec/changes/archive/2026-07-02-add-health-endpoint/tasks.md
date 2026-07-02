# Tasks

## 1. 准备
- [ ] 1.1 从最新 `dev` 切分支 `task/base-001-health`
- [ ] 1.2 阅读 `openspec/changes/add-health-endpoint/` 下 proposal / design / 本 delta spec
- [ ] 1.3 确认只会改动 `health` 包（不碰 pom / common / config / application.yml）

## 2. 实现
- [ ] 2.1 在 `backend/src/main/java/**/health/` 新增 `HealthController`
- [ ] 2.2 暴露 `GET /api/health`，返回 `{"status":"ok"}`，HTTP 200，Content-Type application/json

## 3. 测试
- [ ] 3.1 新增 `HealthControllerTest`，断言状态码 200 且响应体含 `status = ok`
- [ ] 3.2 本地运行 `mvn test`，全部通过

## 4. 交付
- [ ] 4.1 `git commit`（Conventional Commits，带 `#<issue号>`）
- [ ] 4.2 `git push -u origin task/base-001-health`
- [ ] 4.3 `gh pr create --base dev`，PR 描述写 `Closes #<issue号>` 并粘贴 `mvn test` 结果摘要
- [ ] 4.4 在 GitHub Project 看板把卡片从 In Progress 移到 Review
