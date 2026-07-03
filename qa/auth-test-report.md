# 认证模块 QA 测试报告（Issue #8）

- **日期**: 2026-07-03
- **执行人**: 测试/运维 Agent
- **被测服务**: mall-mvp backend `/api/auth/*` + JWT 鉴权
- **目标环境**: Air `http://100.66.95.102:8080`
- **依据规格**:
  - `openspec/changes/add-login-page/specs/user-auth/spec.md`
  - `openspec/changes/add-login-page/specs/sms-code/spec.md`
  - `mall-shared/.../openspec/user-auth/spec.md`
- **被测代码版本**: `dev` @ `3e59a44`
- **本轮执行时间**: 2026-07-03（Air 后端启动后复验）

---

## ✅ 执行结论：PASS（全部通过）

**后端服务已在 Air 上就绪，7 个场景全部执行并通过（7/7），无失败、无阻塞。**

### 连通性诊断

| 检查项 | 命令 | 结果 |
|---|---|---|
| TCP 8080 端口 | `nc -z 100.66.95.102 8080` | ✅ OPEN（`succeeded`） |
| 健康检查 | `curl http://100.66.95.102:8080/api/health` | ✅ `200` `{"status":"ok"}` |

**判定**: Spring Boot 后端服务已在 Air（`100.66.95.102:8080`）正常运行，接口可达，前次的环境/部署阻塞已解除。

---

## 测试计划与逐场景状态

> 期望结果依据规格 + 代码契约（`AuthController` / `AuthService` / `JwtFilter`）推导，本轮已经运行时验证。
> 状态图例：✅ PASS = 实际结果与期望一致。

`BASE=http://100.66.95.102:8080`

### 1. 账号密码登录 —— 正确凭证
- **状态**: ✅ PASS
- **命令**:
  ```bash
  curl -s -w '\n[%{http_code}]\n' -X POST "$BASE/api/auth/login" \
    -H 'Content-Type: application/json' \
    -d '{"username":"demo","password":"password123"}'
  ```
- **期望**: `200`，body 含 `token`（JWT 字符串）与 `expiresIn`（秒数）。
- **实际**: ✅ `200`，`{"expiresIn":86400,"token":"eyJhbGciOiJIUzM4NCJ9.eyJzdWIiOiJkZW1vIiwidWlkIjoxLCJpYXQiOjE3ODMwNjM5NDUsImV4cCI6MTc4MzE1MDM0NX0.joHpt6YIcIhrCxPx1eXCuaMlIRUcymN-8vXBQ1yg_aHDi2VziYfBojNrkgeGNWGA"}`。JWT 解出 `sub=demo`、`uid=1`、`exp-iat=86400s`，与 `expiresIn` 一致。

### 2. 账号密码登录 —— 密码错误
- **状态**: ✅ PASS
- **命令**:
  ```bash
  curl -s -w '\n[%{http_code}]\n' -X POST "$BASE/api/auth/login" \
    -H 'Content-Type: application/json' \
    -d '{"username":"demo","password":"wrong-password"}'
  ```
- **期望**: `401`，`{"error":"INVALID_CREDENTIALS"}`。
- **实际**: ✅ `401`，`{"error":"INVALID_CREDENTIALS"}`。

### 3. 账号密码登录 —— 用户不存在
- **状态**: ✅ PASS
- **命令**:
  ```bash
  curl -s -w '\n[%{http_code}]\n' -X POST "$BASE/api/auth/login" \
    -H 'Content-Type: application/json' \
    -d '{"username":"nobody-xyz","password":"password123"}'
  ```
- **期望**: `401`，`{"error":"INVALID_CREDENTIALS"}`（不区分用户名/密码错误，防枚举）。
- **实际**: ✅ `401`，`{"error":"INVALID_CREDENTIALS"}`。与场景 2 返回体一致，未泄露"用户是否存在"，防枚举生效。

### 4. 发送短信验证码 —— 合法手机号
- **状态**: ✅ PASS
- **命令**:
  ```bash
  curl -s -w '\n[%{http_code}]\n' -X POST "$BASE/api/auth/sms-code" \
    -H 'Content-Type: application/json' \
    -d '{"phone":"13800138000"}'
  ```
- **期望**: `200`，`{"sent":true}`；验证码写入服务端内存 Map，5 分钟过期（代码通过日志 `SMS verification code for {} is {}` 输出，无短信网关）。
- **实际**: ✅ `200`，`{"sent":true}`（手机号 `13800138000`，无残留计时窗口）。

### 5. 发送短信验证码 —— 手机号格式错误
- **状态**: ✅ PASS
- **命令**:
  ```bash
  curl -s -w '\n[%{http_code}]\n' -X POST "$BASE/api/auth/sms-code" \
    -H 'Content-Type: application/json' \
    -d '{"phone":"12345"}'
  ```
- **期望**: `400`，`{"error":"INVALID_PHONE"}`（正则 `^1[3-9]\d{9}$`）。
- **实际**: ✅ `400`，`{"error":"INVALID_PHONE"}`。

### 6. 发送短信验证码 —— 60s 内频率限制
- **状态**: ✅ PASS
- **命令**（连续两次）:
  ```bash
  curl -s -w '\n[%{http_code}]\n' -X POST "$BASE/api/auth/sms-code" \
    -H 'Content-Type: application/json' -d '{"phone":"13800138000"}'
  curl -s -w '\n[%{http_code}]\n' -X POST "$BASE/api/auth/sms-code" \
    -H 'Content-Type: application/json' -d '{"phone":"13800138000"}'
  ```
- **期望**: 第 1 次 `200`；第 2 次（60s 内）`429`，`{"error":"TOO_MANY_REQUESTS"}`。
- **实际**: ✅ 第 1 次 `200` `{"sent":true}`；第 2 次 `429` `{"error":"TOO_MANY_REQUESTS"}`。
  > 注：为避开场景 4 已在 `13800138000` 上占用的 60s 计时窗口，本场景改用未使用过的号码 `13900139000` 执行，两次调用连续发出，验证结果与期望一致。

### 7. 未携带 token 访问受保护接口
- **状态**: ✅ PASS
- **说明**: `JwtFilter` 拦截除 `/api/auth/*`、`/api/health`、OPTIONS、非 `/api/` 之外的所有 `/api/**`。当前无业务受保护 Controller，故取任一受保护路径（会先被过滤器 401 拦截，不触达 Controller）。
- **命令**:
  ```bash
  curl -s -w '\n[%{http_code}]\n' "$BASE/api/me"
  ```
- **期望**: `401`，`{"error":"UNAUTHORIZED"}`。
- **实际**: ✅ `401`，`{"error":"UNAUTHORIZED"}`。`JwtFilter` 在无 `Authorization` 头时拦截并返回，未触达 Controller。

---

## 结果汇总

| # | 场景 | 期望 | 实际 | 状态 |
|---|---|---|---|---|
| 1 | 密码登录成功 | 200 + token/expiresIn | 200 + token/expiresIn=86400 | ✅ PASS |
| 2 | 密码错误 | 401 INVALID_CREDENTIALS | 401 INVALID_CREDENTIALS | ✅ PASS |
| 3 | 用户不存在 | 401 INVALID_CREDENTIALS | 401 INVALID_CREDENTIALS | ✅ PASS |
| 4 | 发送验证码 | 200 sent:true | 200 sent:true | ✅ PASS |
| 5 | 手机号格式错误 | 400 INVALID_PHONE | 400 INVALID_PHONE | ✅ PASS |
| 6 | 频率限制 | 429 TOO_MANY_REQUESTS | 200 → 429 TOO_MANY_REQUESTS | ✅ PASS |
| 7 | 无 token 访问受保护接口 | 401 UNAUTHORIZED | 401 UNAUTHORIZED | ✅ PASS |

**通过 7 / 失败 0 / 阻塞 0**

---

## 后续行动

1. ✅ **[已解除]** Air（100.66.95.102）后端已启动，`/api/health` 返回 `200 {"status":"ok"}`，8080 端口 OPEN。
2. ✅ 7 条 curl 套件已全部重跑并回填「实际」结果，7/7 通过。
3. 测试数据前置条件已确认满足：用户 `demo`（密码 `password123`）存在且可登录（`uid=1`）；短信验证码写入内存 Map 正常。
4. 频率限制用例（场景 6）已改用未使用过的号码 `13900139000`，规避了场景 4 在 `13800138000` 上的残留计时窗口。

> 本报告未修改任何 `backend/` 业务代码（QA 职责边界）。本轮所有场景均在 Air 真实运行环境验证通过，认证模块（登录 + 短信 + JWT 鉴权）功能符合规格。
