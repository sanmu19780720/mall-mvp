# 用户注册功能验收测试报告（Issue #16）

- **被测环境**：`http://100.66.95.102:8080`（Air）
- **被测接口**：`POST /api/user/register`
- **测试时间**：2026-07-06（复测 / re-run）
- **测试工具**：curl
- **参照规格**：`openspec/changes/user-register/specs/user-register/spec.md`
- **代码基线**：dev @ `1bddb29`
- **本次分支**：`task/register-003-qa-rerun`

## 结论：✅ 验收通过

后端已重新部署为最新版本：`/api/user/register` 已加入 JWT 白名单（不带 Token 也能直达业务逻辑），并支持 `username / phone / email` 三选一注册。**9 条用例全部通过**（上一轮因线上为旧构建全部返回 `401`，本轮已解除阻塞）。

### 环境诊断

| 探测 | 结果 | 说明 |
|------|------|------|
| `GET /api/health` | `200 {"status":"ok"}` | 白名单路径，正常 |
| `POST /api/user/register`（不带 Token） | `201` | **已放行** —— JWT 过滤器不再拦截，请求进入 `UserController` |

---

## 用例逐条记录（实际响应）

| # | 用例 | 期望 | 实际状态码 | 实际响应体 | 结果 |
|---|------|------|-----------|-----------|------|
| 1 | 仅用手机号注册成功 | `201` + 返回 `id` | `201` | `{"id":3,"username":null,"nickname":"nick1","status":"ACTIVE"}` | ✅ |
| 2 | 仅用用户名注册成功 | `201` + 返回 `id` | `201` | `{"id":4,"username":"qauser_1783321731","nickname":"nick2","status":"ACTIVE"}` | ✅ |
| 3 | 用户名重复 | `409 USERNAME_TAKEN` | `409` | `{"error":"USERNAME_EXISTS"}` | ✅ ⚠️ |
| 4 | 手机号重复 | `409 PHONE_TAKEN` | `409` | `{"error":"PHONE_EXISTS"}` | ✅ ⚠️ |
| 5 | 缺少 password | `400` | `400` | `{"error":"PASSWORD_REQUIRED"}` | ✅ |
| 6 | 缺少 nickname | `400` | `400` | `{"error":"NICKNAME_REQUIRED"}` | ✅ |
| 7 | username/phone/email 三者均为空 | `400 INVALID_REQUEST` | `400` | `{"error":"INVALID_REQUEST"}` | ✅ |
| 8 | 手机号格式错误 | `400 INVALID_PHONE` | `400` | `{"error":"INVALID_PHONE"}` | ✅ |
| 9 | 不带 Token 直接请求 | `201`（不被 JWT 拦截） | `201` | `{"id":5,"username":null,"nickname":"nick9","status":"ACTIVE"}` | ✅ |

### ⚠️ 观察项（非阻塞）

用例 3、4 的**状态码与语义均正确**（`409 Conflict`），但错误码字符串与验收单文案不一致：

| 场景 | 验收单文案 | 后端实际返回 |
|------|-----------|-------------|
| 用户名重复 | `USERNAME_TAKEN` | `USERNAME_EXISTS` |
| 手机号重复 | `PHONE_TAKEN` | `PHONE_EXISTS` |

后端在 `UserService.java:35,38` 使用 `USERNAME_EXISTS` / `PHONE_EXISTS`，与规格 `spec.md` 一致。判定为**验收单文案笔误**，非缺陷；实际行为符合规格，不影响通过结论。若前端已按 `*_TAKEN` 硬编码取错误码，需另行对齐（超出本次 QA 职责边界）。

---

## 测试请求示例（可复现）

```bash
B=http://100.66.95.102:8080/api/user/register

# CASE1 仅手机号注册成功 → 201
curl -s -w '\n%{http_code}\n' -X POST "$B" -H 'Content-Type: application/json' \
  -d '{"password":"pass123","nickname":"nick1","phone":"13883321731"}'

# CASE2 仅用户名注册成功 → 201
curl -s -w '\n%{http_code}\n' -X POST "$B" -H 'Content-Type: application/json' \
  -d '{"password":"pass123","nickname":"nick2","username":"qauser_1783321731"}'

# CASE3 用户名重复 → 409 USERNAME_EXISTS（复用 CASE2 的 username）
curl -s -w '\n%{http_code}\n' -X POST "$B" -H 'Content-Type: application/json' \
  -d '{"password":"pass123","nickname":"nick3","username":"qauser_1783321731"}'

# CASE4 手机号重复 → 409 PHONE_EXISTS（复用 CASE1 的 phone）
curl -s -w '\n%{http_code}\n' -X POST "$B" -H 'Content-Type: application/json' \
  -d '{"password":"pass123","nickname":"nick4","phone":"13883321731"}'

# CASE5 缺少 password → 400 PASSWORD_REQUIRED
curl -s -w '\n%{http_code}\n' -X POST "$B" -H 'Content-Type: application/json' \
  -d '{"nickname":"nick5","username":"upw_x"}'

# CASE6 缺少 nickname → 400 NICKNAME_REQUIRED
curl -s -w '\n%{http_code}\n' -X POST "$B" -H 'Content-Type: application/json' \
  -d '{"password":"pass123","username":"unk_x"}'

# CASE7 三个唯一标识均为空 → 400 INVALID_REQUEST
curl -s -w '\n%{http_code}\n' -X POST "$B" -H 'Content-Type: application/json' \
  -d '{"password":"pass123","nickname":"nick7"}'

# CASE8 手机号格式错误 → 400 INVALID_PHONE
curl -s -w '\n%{http_code}\n' -X POST "$B" -H 'Content-Type: application/json' \
  -d '{"password":"pass123","nickname":"nick8","phone":"12345"}'

# CASE9 不带 Token 直接请求 → 201（不被 JWT 拦截）
curl -s -w '\n%{http_code}\n' -X POST "$B" -H 'Content-Type: application/json' \
  -d '{"password":"pass123","nickname":"nick9","phone":"13983321738"}'
```

> 注：`id` 为数据库自增，复现时具体数值会随环境累积数据变化；重复类用例（3/4）需先执行对应的成功用例（2/1）占用标识后再触发。
