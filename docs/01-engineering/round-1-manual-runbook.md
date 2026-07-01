# 第一轮手动闭环操作手册（方式 A）

目标：用**纯手动 SSH**，让一台 mini 完整跑完一个任务的全流程，亲眼看清每一步。这一轮不追求代码复杂度，只追求“流程通”。跑通后再脚本化成方式 B。

任务载体：`add-health-endpoint`（给后端加 `GET /api/health`），派给 **Backend Agent A（100.64.21.7）**。

---

## 前置：阶段 0（Air 本机做，不派 mini）

在让任何 mini 干活之前，Air 先把仓库和骨架准备好。

1. 建 GitHub 仓库 `mall-mvp`，克隆到 Air。
2. 放入本平台文档：把 `docs/01-engineering/` 下 machines / agent-roles / git-workflow 三份，以及 `openspec/changes/add-health-endpoint/` 整个目录提交到仓库。
3. 建后端骨架：一个能 `mvn compile` 通过的 Spring Boot 父/子工程，引入 web starter、配置好共享 MySQL 连接（`application.yml`）。**此时不写任何业务接口，也不写 health。**
4. 建分支：
   ```bash
   git checkout -b dev
   git push -u origin dev
   ```
5. （可选但推荐）定契约：把 API 契约、数据库 schema 契约各写一份 md 提交。health 这轮用不到，但把习惯立起来。
6. 建 GitHub Project 看板，列：`Backlog / Ready / In Progress / Review / Done`。
7. 建 Issue #2「Backend Agent A：add health endpoint」，正文贴一行指向 `openspec/changes/add-health-endpoint/tasks.md` 的仓库链接，卡片放到 `Ready`。

> 阶段 0 完成后，`dev` 上有一个能编译、能连库、但没有 health 接口的干净骨架。

---

## 第一轮闭环：手动跑完 Issue #2

### 步骤 1｜Air 派活（把卡片拖到 In Progress）
在 GitHub Project 上把 Issue #2 从 `Ready` 拖到 `In Progress`。这一步是给你自己看的，代表“这个任务开始被某台机器执行”。

### 步骤 2｜SSH 到 Agent A
在 Air 终端：
```bash
ssh 100.64.21.7
cd ~/mall-mvp          # 你在 mini 上克隆仓库的路径
git checkout dev
git pull origin dev
```

### 步骤 3｜启动 Claude Code，交付任务
在 mini 上（仍在 SSH 会话里）启动 Claude Code，给它这一段指令（把 `#2` 换成真实 Issue 号）：

```
你是 Backend Agent A，工作在主机 100.64.21.7。
请先读这几份文件并严格遵守：
- docs/01-engineering/agent-roles.md（你的授权目录与禁止目录）
- docs/01-engineering/git-workflow.md（分支、commit、PR 规范）
- openspec/changes/add-health-endpoint/proposal.md
- openspec/changes/add-health-endpoint/design.md
- openspec/changes/add-health-endpoint/tasks.md
- openspec/changes/add-health-endpoint/specs/health/spec.md

然后从 dev 切分支 task/base-001-health，按 tasks.md 逐条执行：
新增 HealthController，暴露 GET /api/health 返回 {"status":"ok"}，
写单元测试，运行 mvn test 直到全部通过。
只允许改动 health 包，禁止改 pom / common / config / application.yml。
完成后 git commit（信息带 #2）、git push，然后用 gh pr create 开 PR 到 dev，
PR 描述写 "Closes #2" 并附上 mvn test 结果摘要。
每一步开始前先告诉我你要做什么。
```

> 提示：第一次尽量让它“每步先说再做”，方便你观察它是否越界、是否按 spec 走。

### 步骤 4｜观察与验收（在 mini 上）
盯着它：
- 是否只改了 `health` 包（可让它 `git status` / `git diff --stat` 给你看）。
- `mvn test` 是否真的绿。
- 是否成功 `git push` 并 `gh pr create`。

拿到 PR 链接后，回到 Air，把看板卡片从 `In Progress` 拖到 `Review`。

### 步骤 5｜Air Review 并合并
在 Air（或浏览器）上打开 PR：
```bash
gh pr view <pr-number> --web       # 或在网页看 diff
```
检查清单：
- [ ] diff 没碰 deny 目录（pom / common / config / application.yml）。
- [ ] 接口符合契约：`GET /api/health` → 200 + `{"status":"ok"}`。
- [ ] 有对应单元测试且通过。
- [ ] PR 描述有 `Closes #2` 和测试摘要。

通过后合并：
```bash
gh pr merge <pr-number> --squash --delete-branch
```
合并后 Issue #2 因 `Closes #2` 自动关闭，看板卡片自动进 `Done`。

### 步骤 6｜复盘
拉最新 dev 验证：
```bash
git checkout dev && git pull
mvn spring-boot:run      # 起服务
curl -s localhost:8080/api/health   # 期望 {"status":"ok"}
```
记下这一轮里所有“卡壳/手动介入”的点（比如 Claude Code 越权改了文件、gh 没登录、mvn 报错），这些就是方式 B 脚本必须自动处理的地方。

---

## 跑通后：升级到方式 B 的信号
当你能顺畅重复上面这一圈、且清楚每一步的输入输出后，方式 B 的 `dispatch.sh` 本质就是把**步骤 2–4**打包成一条命令：
```
dispatch.sh 100.64.21.7 add-health-endpoint task/base-001-health 2
```
内部做：ssh → git pull → 切分支 → 用 Claude Code headless（`claude -p`）喂 tasks.md → mvn test → commit/push → gh pr create。
把这份手册跑顺，再来找我要脚本。
