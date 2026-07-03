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

---

## ⛔ 执行结论：BLOCKED（阻塞，未能执行）

**目标后端服务不可达，所有 7 个场景均无法执行。**

### 连通性诊断

| 检查项 | 命令 | 结果 |
|---|---|---|
| 主机可达（ICMP） | `ping 100.66.95.102` | ✅ 0% 丢包，RTT ≈ 6ms（Tailscale 正常） |
| TCP 8080 端口 | `nc -z 100.66.95.102 8080` | ❌ CLOSED |
| HTTP 请求 | `curl http://100.66.95.102:8080/api/health` | ❌ `Connection refused`（HTTP 000） |
| 登录接口重试 ×3 | `POST /api/auth/login` | ❌ 3/3 全部 HTTP 000 |

```
* Trying 100.66.95.102:8080...
* connect to 100.66.95.102 port 8080 from <this-host> port ... failed: Connection refused
* Failed to connect to 100.66.95.102 port 8080 after 12 ms: Couldn't connect to server
```

**判定**: 主机在线（Tailscale 网络通），但 **8080 端口无进程监听** —— Spring Boot 后端服务未在 Air 上运行（或已崩溃/未启动）。这是**环境/部署阻塞**，非代码缺陷。

### 本地兜底执行尝试（均不可行）

为拿到真实 QA 信号，尝试用同一 commit 在本机拉起后端，但本机（macOS，QA/运维机）缺少运行时：

- ❌ 无 Java Runtime（`java -version` 失败）
- ❌ 无 Maven（`mvn` / `./mvnw` 不存在，pom 存在但无 wrapper）
- ❌ 无 Docker，仓库亦无 `Dockerfile` / `docker-compose.yml`

故无法本地复现执行。**需先在 Air 上启动后端服务后重跑本报告的测试套件。**

---

## 测试计划与逐场景状态

> 期望结果依据规格 + 代码契约（`AuthController` / `AuthService` / `JwtFilter`）静态推导，尚未经运行时验证。
> 状态图例：⏳ BLOCKED = 因服务不可达未执行。

`BASE=http://100.66.95.102:8080`

### 1. 账号密码登录 —— 正确凭证
- **状态**: ⏳ BLOCKED
- **命令**:
  ```bash
  curl -s -w '\n[%{http_code}]\n' -X POST "$BASE/api/auth/login" \
    -H 'Content-Type: application/json' \
    -d '{"username":"demo","password":"password123"}'
  ```
- **期望**: `200`，body 含 `token`（JWT 字符串）与 `expiresIn`（秒数）。
- **实际**: 无响应（Connection refused）。

### 2. 账号密码登录 —— 密码错误
- **状态**: ⏳ BLOCKED
- **命令**:
  ```bash
  curl -s -w '\n[%{http_code}]\n' -X POST "$BASE/api/auth/login" \
    -H 'Content-Type: application/json' \
    -d '{"username":"demo","password":"wrong-password"}'
  ```
- **期望**: `401`，`{"error":"INVALID_CREDENTIALS"}`。
- **实际**: 无响应。

### 3. 账号密码登录 —— 用户不存在
- **状态**: ⏳ BLOCKED
- **命令**:
  ```bash
  curl -s -w '\n[%{http_code}]\n' -X POST "$BASE/api/auth/login" \
    -H 'Content-Type: application/json' \
    -d '{"username":"nobody-xyz","password":"password123"}'
  ```
- **期望**: `401`，`{"error":"INVALID_CREDENTIALS"}`（不区分用户名/密码错误，防枚举）。
- **实际**: 无响应。

### 4. 发送短信验证码 —— 合法手机号
- **状态**: ⏳ BLOCKED
- **命令**:
  ```bash
  curl -s -w '\n[%{http_code}]\n' -X POST "$BASE/api/auth/sms-code" \
    -H 'Content-Type: application/json' \
    -d '{"phone":"13800138000"}'
  ```
- **期望**: `200`，`{"sent":true}`；验证码写入服务端内存 Map，5 分钟过期（代码通过日志 `SMS verification code for {} is {}` 输出，无短信网关）。
- **实际**: 无响应。

### 5. 发送短信验证码 —— 手机号格式错误
- **状态**: ⏳ BLOCKED
- **命令**:
  ```bash
  curl -s -w '\n[%{http_code}]\n' -X POST "$BASE/api/auth/sms-code" \
    -H 'Content-Type: application/json' \
    -d '{"phone":"12345"}'
  ```
- **期望**: `400`，`{"error":"INVALID_PHONE"}`（正则 `^1[3-9]\d{9}$`）。
- **实际**: 无响应。

### 6. 发送短信验证码 —— 60s 内频率限制
- **状态**: ⏳ BLOCKED
- **命令**（连续两次）:
  ```bash
  curl -s -w '\n[%{http_code}]\n' -X POST "$BASE/api/auth/sms-code" \
    -H 'Content-Type: application/json' -d '{"phone":"13800138000"}'
  curl -s -w '\n[%{http_code}]\n' -X POST "$BASE/api/auth/sms-code" \
    -H 'Content-Type: application/json' -d '{"phone":"13800138000"}'
  ```
- **期望**: 第 1 次 `200`；第 2 次（60s 内）`429`，`{"error":"TOO_MANY_REQUESTS"}`。
- **实际**: 无响应。

### 7. 未携带 token 访问受保护接口
- **状态**: ⏳ BLOCKED
- **说明**: `JwtFilter` 拦截除 `/api/auth/*`、`/api/health`、OPTIONS、非 `/api/` 之外的所有 `/api/**`。当前无业务受保护 Controller，故取任一受保护路径（会先被过滤器 401 拦截，不触达 Controller）。
- **命令**:
  ```bash
  curl -s -w '\n[%{http_code}]\n' "$BASE/api/me"
  ```
- **期望**: `401`，`{"error":"UNAUTHORIZED"}`。
- **实际**: 无响应。

---

## 结果汇总

| # | 场景 | 期望 | 状态 |
|---|---|---|---|
| 1 | 密码登录成功 | 200 + token/expiresIn | ⏳ BLOCKED |
| 2 | 密码错误 | 401 INVALID_CREDENTIALS | ⏳ BLOCKED |
| 3 | 用户不存在 | 401 INVALID_CREDENTIALS | ⏳ BLOCKED |
| 4 | 发送验证码 | 200 sent:true | ⏳ BLOCKED |
| 5 | 手机号格式错误 | 400 INVALID_PHONE | ⏳ BLOCKED |
| 6 | 频率限制 | 429 TOO_MANY_REQUESTS | ⏳ BLOCKED |
| 7 | 无 token 访问受保护接口 | 401 UNAUTHORIZED | ⏳ BLOCKED |

**通过 0 / 失败 0 / 阻塞 7**

---

## 后续行动

1. **[阻塞根因]** 在 Air（100.66.95.102）上启动 Spring Boot 后端并确认 `curl http://100.66.95.102:8080/api/health` 返回 200。
2. 服务就绪后，重跑上述 7 条 curl 命令并回填「实际」结果与状态。
3. 需确认测试数据前置条件：用户 `demo`（密码 `password123`，BCrypt）与手机号 `13800138000` 已在数据库/种子数据中存在——否则场景 1、6 结果会偏离期望。
4. 频率限制用例（场景 6）依赖服务端 60s 计时状态；重跑前需确保该手机号无残留计时窗口，或换用未使用过的号码。

> 本报告未修改任何 `backend/` 业务代码（QA 职责边界）。阻塞项属环境/部署问题，已在 PR 中标注，待运维在 Air 启动服务后由本套件复验。
