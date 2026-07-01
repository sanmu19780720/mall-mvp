# mall-mvp

购物商城一期 —— AI 多机协作开发平台的试跑项目。

- 平台文档与工程规范见 `docs/01-engineering/`。
- 任务规范（OpenSpec）见 `openspec/changes/`。
- 后端骨架见 `backend/`。

## 阶段 0：由 Air 建立骨架并推到 dev

空仓库先由 Air 完成一次性初始化，之后各 Agent 才从 `dev` 拉分支开发。

```bash
# 0. 前置：JDK 17、Maven、已配好共享 MySQL（库名 mall_mvp）
# 1. 把本目录内容放入仓库根，验证后端骨架能编译测试
cd backend
mvn test            # 使用 H2 内存库跑 contextLoads，应当 BUILD SUCCESS

# 2. 本地连真实 MySQL 起服务（可选自检）
export DB_HOST=100.x.x.x DB_NAME=mall_mvp DB_USER=mall DB_PASSWORD=***
mvn spring-boot:run
# 骨架此时没有业务接口；health 接口是第一个派活任务，尚未实现

# 3. 建立分支基线
git add -A
git commit -m "chore: stage-0 backend skeleton + docs + openspec + ci"
git branch -M main
git remote add origin git@github.com:<you>/mall-mvp.git
git push -u origin main
git checkout -b dev
git push -u origin dev
```

## 分支保护（在 GitHub 仓库 Settings > Branches 配置）

对 `dev` 与 `main`：

- Require a pull request before merging
- Require status checks to pass（勾选 `backend-ci`）
- 禁止 force push、禁止直接 push

## 骨架说明

- **技术栈：** JDK 17 + Spring Boot 3.2，单模块按包分域。
- **数据库：** 生产/本地连 Air 上的共享 MySQL（经环境变量注入）；测试/CI 用 H2 内存库，无需外部 MySQL。
- **迁移：** Flyway，`backend/src/main/resources/db/migration`，`V1__baseline.sql` 起步；后续表结构变化只加 `V2/V3...`，不改已合并迁移。
- **CI：** 每个针对 `dev`/`main` 的 PR 触发 `mvn test`，绿灯才允许 Air 合并。
- **故意留白：** 骨架不含 health 接口——它是 `openspec/changes/add-health-endpoint` 定义的第一个派活任务，留给 Backend Agent A 实现，用来跑通第一轮协作闭环（见 `docs/01-engineering/round-1-manual-runbook.md`）。
