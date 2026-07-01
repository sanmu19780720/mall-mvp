# Git 工作流

本文件定义分支模型、命名规范、PR 规则和合并规则。所有 Agent 与 Air 都遵守。

## 分支模型

- `main`：稳定分支，只从 `dev` 合并，代表可发布状态。第一轮暂不动。
- `dev`：集成分支。所有任务分支从 `dev` 切出，完成后 PR 回 `dev`。**这是三台 mini 的共同基线。**
- `task/*`：任务分支。一个 Issue 对应一个任务分支、一个 PR。

## 分支命名

```
task/<批次>-<序号>-<能力域简述>
```

示例：

```
task/base-001-health              # 第一轮 health 接口
task/base-002-backend-user        # 后端 user 包
task/base-003-frontend-skeleton   # 前端骨架
```

## 标准流程（每个任务）

```bash
git checkout dev
git pull origin dev
git checkout -b task/base-001-health
# ...Claude Code 按 tasks.md 写代码...
mvn test                          # 后端；必须绿
git add -A
git commit -m "feat(health): add GET /api/health endpoint (#2)"
git push -u origin task/base-001-health
gh pr create --base dev --title "..." --body "Closes #2" 
```

## Commit 规范

采用 Conventional Commits：

```
feat(scope): ...     新功能
fix(scope): ...      修 bug
chore(scope): ...    杂项/构建
test(scope): ...     测试
docs: ...            文档
```

Commit / PR 信息里带上 Issue 号（`#2`），保证任务可追溯。

## PR 规则

- **base 一律是 `dev`**，不是 `main`。
- PR 描述必须：
  1. `Closes #<issue号>` 关联 Issue（合并后自动关卡片到 Done）。
  2. 勾选对应 `tasks.md` 的完成项，或粘贴清单。
  3. 说明本地测试结果（`mvn test` 输出摘要）。
- **只有 Air 能合并 PR。** Agent 负责开 PR，不自行合并。

## 合并规则（Air 执行）

1. 看 CI（若已配置 GitHub Actions）是否绿。
2. 看 diff 是否越界（有没有碰 deny 目录）。
3. 检查是否符合契约。
4. Squash merge 进 `dev`，删除任务分支。

## 并发与冲突纪律

多 Agent / 多 session 并行时，git 的三方合并**不会静默覆盖**改动：改到不同行会自动合并，改到同一行会报冲突并卡住合并。真正的丢代码风险来自解冲突解错、语义冲突、以及 `push -f`。为把风险压到最低，强制以下纪律：

1. **一分支 · 一工作目录 · 一 session。** 同一时刻一个分支只允许一个 Claude Code session 在写。同一台 mini 若要并行两个任务，必须用 `git worktree`（或两份独立 clone）+ 两个不同分支做物理隔离，**禁止两个 session 共用同一个检出目录同时写**（会在文件系统层面互相覆盖，git 保护不了）。
2. **每台 mini 会话数上限 = 2。** 第一轮（流程验证期）每台 mini 只开 1 个 session。方式 B 稳定后最多 2 个 session，且必须 worktree 隔离。不建议超过 2——瓶颈在 Air 的 review 吞吐与构建负载，不在 mini 进程数。
3. **禁止 force push。** 任务分支与 `dev` / `main` 一律不得 `git push -f`。`dev` / `main` 在 GitHub 开启 branch protection 禁止 force push 与直接 push。
4. **解冲突只能由 Air 做或经 Air review。** Agent 不得自行用 `-X ours/theirs` 草率解冲突。第二个待合并的 PR 必须先 rebase 最新 `dev`、本地测试通过，再由 Air 合并。
5. **靠目录隔离而非靠 git 兜底。** 冲突频繁出现说明任务拆分让 Agent 边界重叠了——回到 `agent-roles.md` 重新划清 ownership，而不是反复解冲突。

## 数据库变更

- 一律新增 Flyway 迁移文件（如 `V2__add_user_table.sql`），**不改已合并的迁移**。
- 迁移文件由 Air 或明确授权的 Agent 编写，避免多台并发建表冲突。
