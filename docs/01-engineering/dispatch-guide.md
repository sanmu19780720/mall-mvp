# 任务派发指南

本文件规范 Air 派发任务给三台 mini 的完整流程，包括 Issue 拆分规则和启动 prompt 模板。

## 原则

1. **Issue 只包含该角色的子任务**，不引用完整 tasks.md，避免 Agent 越界执行其他角色的工作。
2. **prompt 里必须显式声明职责边界**，不能假设 Agent 会自己读 `agent-roles.md`。
3. **前置条件要写清楚**，前端依赖后端接口、测试依赖前后端都合并后才能开始。
4. **派发前必须先 push change 文件**：Air 产出 change 后立即 `git push origin dev`，再创建 Issue。mini `git pull` 后才能读到 `openspec/changes/<change-name>/` 的 proposal/design/tasks。不得先建 Issue 再推文件。
5. **归档前必须先 `git pull origin dev`**：mini 在实现过程中会更新 tasks.md 并 commit，Air 若不 pull 直接归档会看到旧版本，误报任务未完成。

---

## 一、Issue 拆分规则

每个 change 的 `tasks.md` 按角色拆成独立 Issue：

| 角色 | Label | 包含的任务 |
|---|---|---|
| 后端工程师（100.95.162.7） | `role:backend` | 数据库迁移、后端域实现、单元测试 |
| 前端工程师（100.64.21.7） | `role:frontend` | 前端页面、交互逻辑 |
| 测试/运维（100.78.243.119） | `role:qa` | 环境搭建、接口测试、验收报告 |

**注意**：`backend/pom.xml`、`application.yml`、Flyway 迁移文件等公共文件由 Air 提前处理好，不放进后端 Issue 的任务清单，在 Issue 里注明「已由 Air 完成」。

---

## 二、启动 prompt 模板

SSH 进 mini 后执行：

```bash
export PATH=$HOME/.local/bin:$PATH && cd ~/mall-mvp && git pull origin dev
claude --dangerously-skip-permissions -p '<prompt>'
```

### 后端工程师 prompt 模板

```
你是后端工程师 Agent，负责处理 GitHub Issue #<号码>。

【职责边界】
- 只能修改：backend/src/main/java/**/（公共包除外）、backend/src/test/java/**/
- 禁止修改：frontend/、backend/pom.xml、application.yml、Flyway 迁移文件、common/、config/、security/
- 如需改动禁止目录，停下来在 Issue 里留言说明，等 Air 处理

【设计文档】
先阅读以下文件再动手：
- openspec/changes/<change-name>/proposal.md
- openspec/changes/<change-name>/design.md
- openspec/changes/<change-name>/specs/（对应能力域）

【任务】
<从 tasks.md 中复制该角色对应的子任务清单>

【交付】
- 分支名：task/<id>-backend
- commit 带 #<issue号>
- PR base: dev，描述写 Closes #<issue号> 并粘贴 mvn test 结果
```

### 前端工程师 prompt 模板

```
你是前端工程师 Agent，负责处理 GitHub Issue #<号码>。

【职责边界】
- 只能修改：frontend/
- 禁止修改：backend/、public/、任何后端配置文件
- 如发现后端接口与 spec 不符，在 Issue 里留言，不要自行改后端

【设计文档】
先阅读以下文件再动手：
- openspec/changes/<change-name>/proposal.md
- ~/mnt/mall-shared/projects/mall-mvp/prd/（PRD 文档）
- ~/mnt/mall-shared/projects/mall-mvp/prototypes/（HTML 原型图）
- ~/mnt/mall-shared/projects/mall-mvp/openspec/（API 约定）

【后端接口】
后端已实现，base URL：http://100.66.95.102:8080（或本地启动后 localhost:8080）

【任务】
<从 tasks.md 中复制该角色对应的子任务清单>

【交付】
- 分支名：task/<id>-frontend
- commit 带 #<issue号>
- PR base: dev，描述写 Closes #<issue号>
```

### 测试/运维 prompt 模板

```
你是测试/运维 Agent，负责处理 GitHub Issue #<号码>。

【职责边界】
- 只能创建或修改：qa/、tests/e2e/
- 禁止修改：backend/ 业务代码、frontend/ 业务代码
- 发现 bug 在对应 Issue 留言或新开 Issue，不直接改代码

【验收标准】
先阅读以下文件作为验收依据：
- openspec/changes/<change-name>/specs/（接口约定）
- ~/mnt/mall-shared/projects/mall-mvp/openspec/（API 约定）

【后端服务】
运行在 Air：http://100.66.95.102:8080

【任务】
<列出需要验证的接口场景和验收条件>

【交付】
- 测试报告保存到 qa/<report-name>.md
- 分支名：task/<id>-qa
- PR base: dev，描述写 Closes #<issue号>
```

---

## 三、并行工作时序

```
Air 产出设计文档 → 推送到共享目录 + 创建 Issues
        ↓
后端 Issue 开始（Mini 2）
        ↓ 后端 PR 合并进 dev
前端 Issue 开始（Mini 1）
        ↓ 前后端 PR 都合并进 dev
测试 Issue 开始（Mini 3）
```

前端可以在后端 PR **合并前**提前开始写静态页面结构，但接口联调需等后端合并后进行。

---

## 四、QA 报告处理流程

QA PR 合并后，Air 必须逐条过报告，对每个 bug **立刻开 Issue** 并派发给对应角色，不得搁置等到联调时再处理。

```
QA PR 合并
    ↓
Air 逐条读报告
    ↓
每个 bug → gh issue create（打对应 role 标签）→ 立刻派发给 mini
    ↓
全部 bug Issue 开完 → 等修复 PR 合并 → 再推进联调
```

**注意**：跳过这一步直接联调，等于把 QA 的价值抛掉了，bug 只会在联调时重新被发现。

---

## 五、Air 合并 PR 前的检查清单

- [ ] CI 通过（`mvn test` 绿）
- [ ] diff 未越界（没有碰 deny 目录）
- [ ] 实现符合 openspec 约定
- [ ] PR 描述里有 `Closes #<issue号>`
