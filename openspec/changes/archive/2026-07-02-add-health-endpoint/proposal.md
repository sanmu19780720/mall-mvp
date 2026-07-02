# Proposal: Add Health Endpoint

## Intent
第一轮 AI 协作闭环需要一个**最小、无依赖、单机可完成**的任务，用来验证整条流水线：
OpenSpec 施工图 → Issue → Air 派活给一台 mini → Claude Code 执行 → PR → Air Review → 合并 `dev`。
`GET /api/health` 是最合适的载体：逻辑极简，重点在于跑通**流程**而非业务复杂度。

## Scope
In scope:
- 新增一个健康检查 HTTP 接口 `GET /api/health`。
- 返回固定 JSON `{"status":"ok"}`，HTTP 200。
- 覆盖该接口的单元测试。

Out of scope:
- 数据库连通性检查、依赖组件探活（后续 change 再做）。
- 认证鉴权（health 接口公开）。
- 任何 user / product / trade 等业务逻辑。

## Approach
在后端骨架（阶段 0 已由 Air 建好、已在 `dev`）之上，由 **Backend Agent A（100.64.21.7）** 在其授权的包内新增一个 `HealthController`，暴露 `GET /api/health`。使用 Spring Boot（骨架已引入 web starter），配套一个 MockMvc / WebTestClient 单元测试。完成后 `mvn test` 必须通过，push 分支 `task/base-001-health`，开 PR 到 `dev`。

本 change 的验收核心不在代码，而在于：一张 Issue 能否被完整地、按规范地、单机跑完一整圈。
