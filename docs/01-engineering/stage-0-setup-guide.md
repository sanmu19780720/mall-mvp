# 阶段 0 上手指南：把骨架推上 GitHub

目标：在 Air 上，把这一整套文件初始化成 `mall-mvp` 仓库，推到 GitHub，建好 `main` / `dev` 分支和分支保护，为第一轮派活做好准备。全程在 **Air** 上操作。

---

## 0. 前置检查（在 Air 上跑一遍）

```bash
git --version          # 有 git
gh auth status         # 已登录 GitHub CLI（你之前已完成）
java -version          # 需要 JDK 17（Spring Boot 3.2 要求）
mvn -v                 # 有 Maven
```

若 `java -version` 不是 17，先切到 17 再继续（骨架用 3.2，JDK 11 会编译失败）。

---

## 1. 把文件放进一个仓库文件夹

我生成的文件已经是**正确的仓库目录结构**，直接作为仓库根即可。在 Air 上准备一个空文件夹 `mall-mvp/`，把下列内容原样放进去（保持相对路径不变）：

```
mall-mvp/
├── README.md
├── .gitignore
├── .github/workflows/ci.yml
├── docs/01-engineering/
│   ├── machines.md
│   ├── agent-roles.md
│   ├── git-workflow.md
│   ├── round-1-manual-runbook.md
│   └── stage-0-setup-guide.md   ← 本文件
├── openspec/changes/add-health-endpoint/
│   ├── proposal.md
│   ├── design.md
│   ├── tasks.md
│   └── specs/health/spec.md
└── backend/
    ├── pom.xml
    └── src/...（启动类 / application.yml / V1__baseline.sql / 测试）
```

> 注意 `.github` 和 `.gitignore` 是隐藏文件（点开头），拷贝时别漏。检查：
> ```bash
> cd mall-mvp
> ls -la          # 应能看到 .github 和 .gitignore
> find . -type f | sort
> ```

---

## 2. 先本地验证骨架能编译测试

推之前先确认骨架是好的（CI 之后会自动做同样的事）：

```bash
cd mall-mvp/backend
mvn test
# 期望结尾出现 BUILD SUCCESS；测试用 H2 内存库，不需要连 MySQL
cd ..
```

若这里失败，先解决（多半是 JDK 版本），不要带着红的骨架往上推。

---

## 3. 初始化 git 并首次提交

```bash
cd mall-mvp          # 仓库根
git init
git add -A
git status           # 核对该进的文件都在（尤其 .github/、backend/）
git commit -m "chore: stage-0 backend skeleton + docs + openspec + ci"
```

---

## 4. 创建 GitHub 仓库并推送 main

用 GitHub CLI 一步创建 + 关联 + 推送：

```bash
git branch -M main
gh repo create mall-mvp --private --source=. --remote=origin --push
```

- `--private` 私有仓库；要公开就换 `--public`。
- 若提示已存在同名仓库，改个名或先在网页删掉旧的。
- 没有 `gh` 也可手动：先在网页建空仓库，再
  ```bash
  git remote add origin git@github.com:<你的用户名>/mall-mvp.git
  git push -u origin main
  ```

---

## 5. 建立 dev 集成分支

所有 Agent 从 `dev` 拉分支，所以必须有 `dev`：

```bash
git checkout -b dev
git push -u origin dev
```

推完后 GitHub 上应有 `main` 和 `dev` 两个分支。

---

## 6. 配置分支保护（网页操作，很重要）

浏览器打开仓库 → **Settings → Branches → Add branch ruleset**（或旧界面 Add rule），对 `dev` 和 `main` 各配一条：

- ✅ Require a pull request before merging
- ✅ Require status checks to pass → 搜索并勾选 **`backend-ci`**
  （注：这个检查名要等第 8 步第一个 PR 触发过一次 CI 后才会出现在列表里；可以先建规则、之后回来补勾）
- ✅ Block force pushes
- ✅ 不允许直接 push（只能通过 PR 合并）

这条规则就是把 `git-workflow.md` 里"只有 Air 能合并、禁 force push、CI 绿灯才合"落地成硬约束。

---

## 7. 建看板和第一张任务单

**GitHub Project 看板：**
仓库页 → Projects → New project → Board 视图，命名 `Mall MVP AI Collaboration Board`，建列：`Backlog / Ready / In Progress / Review / Done`。

**Issue #2（第一个派活）：**
```bash
gh issue create \
  --title "Backend Agent A: add health endpoint" \
  --body "施工图见 openspec/changes/add-health-endpoint/tasks.md。分支 task/base-001-health，Agent 100.64.21.7。Closes 目标：GET /api/health 返回 {\"status\":\"ok\"} 并过 mvn test。"
```
把这张 Issue 拖到看板 `Ready` 列。（Issue #1 可留给"初始化平台文档"，本次已随骨架一起提交，可直接建好即关。）

---

## 8. 完成

现在仓库已就绪：`main` + `dev` + 骨架 + CI + 文档 + 第一张任务单。
下一步照 `docs/01-engineering/round-1-manual-runbook.md` 的"第一轮闭环"，SSH 到 100.64.21.7 派 health 任务，跑通第一次 AI 协作 PR。

> 第一个 PR 合并前，CI 会首次运行；运行过一次后，回到第 6 步把 `backend-ci` 勾进必需状态检查。

---

## 常见卡点速查

| 现象 | 处理 |
|------|------|
| `mvn test` 报 Java 版本不符 | 切换到 JDK 17（`java -version` 确认） |
| `git push` 被拒 / 没权限 | `gh auth status` 看是否登录；SSH key 是否加到 GitHub |
| 推上去后看不到 `.github/` | 拷贝时漏了隐藏文件，`ls -la` 检查后补 `git add .github` |
| 分支保护里找不到 `backend-ci` | 得先有一个 PR 触发过 CI，检查名才会出现，之后再回来勾 |
| Agent 改了不该改的公共文件 | 见 `agent-roles.md` 的禁止目录；PR review 时打回 |
