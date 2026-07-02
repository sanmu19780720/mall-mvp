# Design: Add Health Endpoint

## Technical Approach
在后端骨架（Spring Boot，已在 `dev`）中新增一个 REST 控制器，暴露 `GET /api/health`。无 Service、无 Repository、无数据库依赖，纯控制器 + 单测，最大限度降低第一轮的实现复杂度。

## Architecture Decisions

### Decision: 自定义 /api/health 而非直接用 Spring Boot Actuator
第一轮的目的是验证协作流程，需要一个 Agent 亲手写、亲手测的最小代码单元。Actuator 的 `/actuator/health` 是开箱即用的，不需要写代码，无法体现“Agent 写代码 + 测试”的闭环。故本轮手写一个极简接口。Actuator 可在后续 change 引入。

### Decision: 归属 Backend Agent A
health 接口不属于任何业务域，但为了让第一轮走通“派活给一台 mini”，指定由 Backend Agent A（100.64.21.7）承担。控制器放在一个中性的 `health` 包内，属于本次 change 明确授权 Agent A 修改的目录。

## File Changes（预期）
- `backend/src/main/java/**/health/HealthController.java`（新增）
- `backend/src/test/java/**/health/HealthControllerTest.java`（新增）

> 注意：Agent A 本轮**仅**被授权修改上述 `health` 包。父 POM、`common`、`config`、`application.yml` 不得改动（骨架已提供 web starter）。

## Response Contract
```
GET /api/health
200 OK
Content-Type: application/json
{ "status": "ok" }
```
