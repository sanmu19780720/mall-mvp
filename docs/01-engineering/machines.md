# 机器与网络拓扑

本文件描述本平台的物理机器、网络连接方式和各机器职责。所有 Agent（Claude Code）在接任务前应先读此文件，明确自己在哪台机器、能访问什么。

## 机器清单

| 角色 | 主机名 | Tailscale IP | 职责 |
|------|--------|--------------|------|
| 总控 / 架构 / Review | Air | 100.66.95.102 | 需求解析、OpenSpec、PRD、HTML原型图、派发任务、Review PR、合并 |
| 前端工程师 Agent | frontend-agent | 100.64.21.7 | 前端页面开发，读共享目录的 PRD + 原型图 + OpenSpec |
| 后端工程师 Agent | backend-agent | 100.95.162.7 | 后端服务开发，读共享目录的 PRD + OpenSpec |
| 测试/运维 Agent | qa-agent | 100.78.243.119 | 搭建前后端运行环境、执行测试（Apifox CLI 等）、输出测试报告 |

## 连接方式

- 四台机器通过 **Tailscale** 组成虚拟局域网，Air 通过 **免密 SSH** 远程登录各 mini。
- Air 上派任务的基本入口：`ssh zxyw@<mini-ip>`（已配置免密）。
- 三台 mini 均已安装：Java / Maven / Node / GitHub CLI（`gh`）/ Claude Code CLI，并已完成 GitHub 登录。

## 共享目录

- Air 通过 **SMB** 共享 `~/mall-shared`，三台 mini 开机自动挂载到 `~/mnt/mall-shared`。
- 共享目录结构：
  ```
  mall-shared/
  ├── projects/
  │   └── mall-mvp/
  │       ├── prd/          # PRD 文档
  │       ├── prototypes/   # HTML 静态原型图
  │       └── openspec/     # API 约定、数据模型、协议
  └── templates/            # 文档模板
  ```
- Agent 从共享目录**只读**设计产物，不向共享目录写入。

## 共享 MySQL

- MySQL 运行在 **Air** 上，三台 mini 通过 Tailscale 网络访问。
- 连接信息（host / port / user / 库名）统一写在仓库的 `application.yml` 或环境变量中，由 Air 在阶段 0 定稿，**Agent 不得擅自改动连接配置**。
- 数据库结构变更一律走 **Flyway 迁移文件**，不允许 Agent 手动改库。详见 `git-workflow.md` 与数据库 schema 契约文档。

## 铁律

1. 前端 Agent 只改 `frontend/`，后端 Agent 只改 `backend/`，测试/运维 Agent 只改 `qa/` 或 `tests/`。
2. 公共文件（父 POM、`common`、`config`、`security`、`application.yml`、Flyway 迁移）只有 Air 能改。
3. API 接口结构和数据库 schema 以 Air 定稿的 OpenSpec 为准，Agent 不得自行发明字段。
4. 只有 Air 能合并 PR。
