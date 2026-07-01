# 机器与网络拓扑

本文件描述本平台的物理机器、网络连接方式和各机器职责。所有 Agent（Claude Code）在接任务前应先读此文件，明确自己在哪台机器、能访问什么。

## 机器清单

| 角色 | 主机名 | Tailscale IP | 职责 |
|------|--------|--------------|------|
| 总控 / 架构 / Review / MySQL 主机 | Air | （本机，你操作的机器） | 定契约与 schema、派发任务、Review PR、合并、运行共享 MySQL |
| 后端功能 Agent A | backend-agent | 100.64.21.7 | 后端功能包（user / product / cart 等），只写自己 owns 的包 |
| 后端功能 Agent B | （待命名） | 100.95.162.7 | 后端功能包（trade / admin 等），只写自己 owns 的包 |
| 前端 + QA Agent | （待命名） | 100.78.243.119 | 用户端 + 后台前端、接口联调、自动化测试、融合测试报告 |

## 连接方式

- 三台 mini 通过 **Tailscale** 组网，Air 通过 **免密 SSH** 远程登录。
- Air 上派任务的基本入口：`ssh <mini-ip>`（已配置免密）。
- 三台 mini 均已安装：Java / Maven / Node / GitHub CLI（`gh`）/ Claude Code CLI，并已完成 GitHub 登录。

## 共享 MySQL

- MySQL 运行在 **Air** 上，三台 mini 通过 Tailscale 网络访问。
- 连接信息（host / port / user / 库名）统一写在仓库的 `application.yml` 或环境变量中，由 Air 在阶段 0 定稿，**Agent 不得擅自改动连接配置**。
- 数据库结构变更一律走 **Flyway 迁移文件**，不允许 Agent 手动改库。详见 `git-workflow.md` 与数据库 schema 契约文档。

## 铁律

1. 每台 mini 只在自己 own 的功能包内写代码，不碰公共文件（父 POM、`common`、`config`、`security`、`application.yml`）。这些只有 Air 能改。
2. 任何数据库结构变化只能通过 Flyway 迁移文件表达。
3. 每台 mini 独立跑单元测试；只有合并进 `dev` 后的代码才被视为“真实”。
