# Agent 角色与边界

本文件定义每个 AI Agent 的职责范围、允许改动的目录、禁止改动的目录，以及通用工作纪律。这是三台 mini 上 Claude Code 的"岗位说明书"。

## 通用纪律（所有 Agent 都必须遵守）

1. **先读设计产物，再动手。** 接到任务后，先读 GitHub Issue 的任务说明，再读 `~/mnt/mall-shared/projects/mall-mvp/` 下对应的 PRD、OpenSpec、原型图，以及仓库 `openspec/` 下对应 change 的 proposal.md、design.md、tasks.md。
2. **只在授权目录内写代码。** 见下方各角色的 allow / deny 列表。若任务需要改到 deny 目录，**停下来，向 Air 报告**，不要擅自改。
3. **契约不可自定义。** API 接口结构、数据库表结构以 Air 定稿的 OpenSpec 为准，实现必须符合契约，不得自行发明字段。
4. **完成的定义（Definition of Done）：** 代码写完 + 本地测试通过 + 分支已 push + PR 已开，PR 描述里关联对应 Issue。
5. **一个任务一个分支一个 PR。** 不在一个分支里混做多个 Issue。

---

## 前端工程师 Agent（100.64.21.7）

**职责：** 根据 PRD、HTML 原型图和 OpenSpec 开发前端页面。

- **允许修改：**
  - `frontend/**`
  - 对应的前端测试文件
- **禁止修改：** `backend/`、公共配置文件、Flyway 迁移文件。
- **工作输入：** `~/mnt/mall-shared/projects/mall-mvp/prd/`、`~/mnt/mall-shared/projects/mall-mvp/prototypes/`、`~/mnt/mall-shared/projects/mall-mvp/openspec/`

---

## 后端工程师 Agent（100.95.162.7）

**职责：** 根据 PRD 和 OpenSpec 开发后端服务。

- **允许修改：**
  - `backend/src/main/java/com/mall/mvp/**`（公共包除外）
  - `backend/src/test/java/com/mall/mvp/**`
- **禁止修改：** `backend/pom.xml`、`common`、`config`、`security`、`application.yml`、Flyway 迁移文件（除非任务明确授权）、`frontend/`。
- **工作输入：** `~/mnt/mall-shared/projects/mall-mvp/prd/`、`~/mnt/mall-shared/projects/mall-mvp/openspec/`

---

## 测试/运维 Agent（100.78.243.119）

**职责：** 搭建前后端服务运行环境，执行测试，输出测试报告。

- **允许修改：**
  - `qa/**` 或 `tests/e2e/**`（测试脚本、Apifox 配置、测试报告）
- **禁止修改：** `backend/` 业务代码、`frontend/` 业务代码。如发现 bug，提 Issue 给对应 Agent，不直接改。
- **工作输入：** `~/mnt/mall-shared/projects/mall-mvp/openspec/`（作为接口验收标准）、PRD 和原型图（作为功能验收参考）

---

## Air（架构/设计，不在 mini 上）

- 唯一可以改公共文件的角色：父 POM、`common`、`config`、`security`、`application.yml`、Flyway 迁移。
- 负责需求解析，产出 OpenSpec、PRD、HTML 原型图并同步到共享目录。
- 创建 GitHub Issue，打好 Label 和 Assignee 后指派给对应 mini。
- Review 所有 PR，合并进 `dev`。
