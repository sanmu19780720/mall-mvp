# 用户注册功能验收测试报告（Issue #16）

- **被测环境**：`http://100.66.95.102:8080`（Air）
- **被测接口**：`POST /api/user/register`
- **测试时间**：2026-07-03（服务器时间 `Fri, 03 Jul 2026 09:28 GMT`）
- **测试工具**：curl
- **参照规格**：`openspec/changes/user-register/specs/user-register/spec.md`
- **代码基线**：dev @ `d4a931f`

## 结论：❌ 验收未通过 — 环境阻塞（Blocker）

**所有 8 条用例均无法通过，根因为部署阻塞而非业务逻辑缺陷。**

部署在 Air 上的后端构建**尚未包含 `/api/user/register` 的 JWT 白名单放行**，导致该接口被 `JwtFilter` 拦截，任何请求（无论 body 是否合法、是否带 Token）都先返回 `401 UNAUTHORIZED`，请求根本到不了 `UserController`。

dev 分支源码中 `backend/src/main/java/com/mall/mvp/auth/JwtFilter.java:41` 已将 `/api/user/register` 列入白名单，但**线上运行的是旧构建**，需要将 dev 重新部署到 Air 后才能完成验收。

### 环境诊断证据

| 探测 | 结果 | 说明 |
|------|------|------|
| `GET /api/health` | `200 {"status":"ok"}` | 白名单路径，正常 |
| `POST /api/auth/login`（空 body） | `400 {"error":"INVALID_REQUEST"}` | 白名单路径，能到达 Controller，说明 JWT 过滤器与白名单机制本身工作正常 |
| `POST /api/user/register`（任意 body） | `401 {"error":"UNAUTHORIZED"}` | **未被放行** —— 线上构建缺少 register 白名单条目 |

`/api/auth/*` 与 `/api/health` 均能到达业务层，唯独 `/api/user/register` 被 401 拦截，可确认这是部署版本落后（stale build），而非过滤器配置或网络问题。

---

## 用例逐条记录（实际响应）

> 所有用例的实际响应均为 `401 {"error":"UNAUTHORIZED"}`，因请求在 JWT 过滤器处被拦截，未进入注册逻辑。

| # | 用例 | 期望 | 实际响应 | 状态码 | 结果 |
|---|------|------|----------|--------|------|
| 1 | 注册成功 | `201` + 返回 `id` | `{"error":"UNAUTHORIZED"}` | `401` | ❌ 阻塞 |
| 2 | 用户名重复 | `409 USERNAME_TAKEN` | `{"error":"UNAUTHORIZED"}` | `401` | ❌ 阻塞 |
| 3 | 手机号重复 | `409 PHONE_TAKEN` | `{"error":"UNAUTHORIZED"}` | `401` | ❌ 阻塞 |
| 4 | 邮箱重复 | `409 EMAIL_TAKEN` | `{"error":"UNAUTHORIZED"}` | `401` | ❌ 阻塞 |
| 5 | 缺少 password | `400 INVALID_REQUEST` | `{"error":"UNAUTHORIZED"}` | `401` | ❌ 阻塞 |
| 6 | 三个唯一标识均为空 | `400 INVALID_REQUEST` | `{"error":"UNAUTHORIZED"}` | `401` | ❌ 阻塞 |
| 7 | 手机号格式错误 | `400 INVALID_PHONE` | `{"error":"UNAUTHORIZED"}` | `401` | ❌ 阻塞 |
| 8 | 不带 Token 直接请求 | `201`（不被 JWT 拦截） | `{"error":"UNAUTHORIZED"}` | `401` | ❌ 阻塞（**关键**：接口仍被 JWT 拦截） |

### 测试请求示例

```bash
# CASE1 注册成功（实际返回 401）
curl -s -X POST http://100.66.95.102:8080/api/user/register \
  -H 'Content-Type: application/json' \
  -d '{"username":"qauser_...","password":"secret123","phone":"13xxxxxxxxx","nickname":"QA","email":"qa_...@test.com"}'
# → 401  {"error":"UNAUTHORIZED"}

# CASE8 不带 Token（实际返回 401，未放行）
curl -s -X POST http://100.66.95.102:8080/api/user/register \
  -H 'Content-Type: application/json' \
  -d '{"username":"nt_...","password":"secret123","phone":"16xxxxxxxxx","nickname":"QA"}'
# → 401  {"error":"UNAUTHORIZED"}
```

---

## 附：代码契约与规格/任务预期的差异（重新部署后需复测确认）

在部署问题修复、接口可达后，以下差异需要在复测时留意——`UserController.java` 的实际实现与本 Issue 用例的预期**并不完全一致**：

1. **用例 5「缺少 password → `400 INVALID_REQUEST`」**：源码 `UserController.java:55-56` 实际返回 `PASSWORD_REQUIRED`，非 `INVALID_REQUEST`。
2. **用例 6「三个唯一标识均为空 → `400 INVALID_REQUEST`」**：源码实际**强制要求 `username` 与 `phone` 均非空**（`UserController.java:52-53` 返回 `USERNAME_REQUIRED`、`61-62` 返回 `PHONE_REQUIRED`），并非规格描述的「username / phone / email 三选一」，也不会返回 `INVALID_REQUEST`。
3. 规格 `spec.md` 中「username（或 phone 或 email）三选一即可」的语义在当前实现中**未落地**：`username` 与 `phone` 是必填项。

以上属于**实现与规格/用例预期的契约不一致**，超出 QA 修改边界（不改 backend/、openspec/），已如实记录，建议由后端/规格负责人确认是调整实现还是修订用例预期。

---

## 建议行动项

1. **【阻塞，必须先做】** 将 dev @ `d4a931f`（含 `JwtFilter` register 白名单）重新构建并部署到 Air（100.66.95.102:8080），使 `/api/user/register` 可达。
2. 重新部署后重跑本报告全部 8 条用例。
3. 复测时按上文「附：差异」核对用例 5、6 的预期错误码，与后端/规格负责人对齐。
