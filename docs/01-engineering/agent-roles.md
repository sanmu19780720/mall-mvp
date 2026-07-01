# Agent 角色与边界

本文件定义每个 AI Agent 的职责范围、允许改动的目录、禁止改动的目录，以及通用工作纪律。这是三台 mini 上 Claude Code 的“岗位说明书”。

## 通用纪律（所有 Agent 都必须遵守）

1. **先读 spec，再动手。** 接到任务后，先读 `openspec/changes/<change-id>/` 下的 `proposal.md`、`design.md`、`tasks.md`，逐条按 `tasks.md` 执行。
2. **只在授权目录内写代码。** 见下方各角色的 allow / deny 列表。若任务需要改到 deny 目录，**停下来，向 Air 报告**，不要擅自改。
3. **契约不可自定义。** API 接口结构、数据库表结构以 Air 定稿的契约文档为准，实现必须符合契约，不得自行发明字段。
4. **完成的定义（Definition of Done）：** 代码写完 + 本地 `mvn test`（后端）或对应测试（前端）通过 + 分支已 push + PR 已开、并在 PR 描述里勾选 `tasks.md` 完成项、关联对应 Issue。
5. **一个任务一个分支一个 PR。** 不在一个分支里混做多个 Issue。

## Backend Agent A（100.64.21.7 / backend-agent）

- **负责能力域：** user、product、category、cart（用户与商品侧）。
- **允许修改：**
  - `backend/src/main/java/**/user/**`
  - `backend/src/main/java/**/product/**`
  - `backend/src/main/java/**/category/**`
  - `backend/src/main/java/**/cart/**`
  - 上述包对应的 `backend/src/test/java/**`
- **禁止修改：** 父 POM / `backend/pom.xml`、`common`、`config`、`security`、`application.yml`、他人功能包、任何 Flyway 迁移文件（除非任务明确授权）。

## Backend Agent B（100.95.162.7）

- **负责能力域：** trade（下单/订单/支付占位）、admin（后台管理接口）。
- **允许修改：**
  - `backend/src/main/java/**/trade/**`
  - `backend/src/main/java/**/admin/**`
  - 上述包对应的 `backend/src/test/java/**`
- **禁止修改：** 同 Agent A 的禁止列表，外加 Agent A 的功能包。

## Frontend + QA Agent（100.78.243.119）

- **负责能力域：** 用户端前端、后台管理前端、接口联调、自动化测试、融合测试报告。
- **允许修改：**
  - `frontend/**`（用户端）
  - `admin-frontend/**`（后台前端）
  - `qa/**` 或 `tests/e2e/**`（测试与报告）
- **禁止修改：** 后端 Java 源码（联调时如发现后端问题，提 Issue 给对应后端 Agent，不直接改后端）。

## Air（人 + 架构 Agent，不在 mini 上）

- 唯一可以改公共文件的角色：父 POM、`common`、`config`、`security`、`application.yml`、Flyway 迁移。
- 定 API 契约与数据库 schema 契约。
- Review 所有 PR，合并进 `dev`。
- 触发 QA、汇总测试报告、复盘。
