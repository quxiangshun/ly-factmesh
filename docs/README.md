# LY-FactMesh 文档中心

本目录集中存放项目设计、开发计划与各技术点说明，便于团队与社区直接阅读。

## 核心文档

| 文档 | 说明 |
|------|------|
| [项目设计文档](PROJECT_DESIGN.md) | 整个项目的设计文档：背景与目标、系统架构、模块设计、数据与 API 规范、安全与非功能需求、技术选型 |
| [系统开发计划](SYSTEM_DEVELOPMENT_PLAN.md) | 系统开发计划：阶段划分（P0–P4）、里程碑与交付物、任务清单、依赖与风险 |

## 架构与开发

| 文档 | 说明 |
|------|------|
| [DDD 架构总览](DDD_ARCHITECTURE_OVERVIEW.md) | 领域驱动设计分层、各模块领域对象与 DDD 分层结构 |
| [Knife4j 集成](knife4j-integration.md) | OpenAPI3 与 Knife4j 网关文档聚合配置与使用说明 |
| [贡献指南](CONTRIBUTING.md) | 分支规范、提交规范、PR 流程 |

## 部署与运维

| 文档 | 说明 |
|------|------|
| [Docker 部署](DOCKER_README.md) | Docker 与 Docker Compose 部署说明 |
| [Nacos 安装配置](nacos/Nacos%20安装配置指南.md) | 注册中心与配置中心安装与配置 |

## 协作与提交

| 文档 | 说明 |
|------|------|
| [GitHub 提交与推送问题处理](GITHUB_PUSH_GUIDE.md) | 推送 443 失败、代理、SSH、认证等排查与修复 |
| [Git 中文提交说明乱码修复](GIT_COMMIT_ENCODING.md) | 配置 UTF-8，避免 commit 信息中文乱码 |

## 其他

| 文档 | 说明 |
|------|------|
| [Github 配置](Github%20Config.md) | GitHub 相关配置说明 |
| [项目搭建踩坑 / 版本依赖](项目搭建踩坑/版本依赖.md) | 搭建与依赖版本记录 |

---

阅读建议：新人可先看 **项目设计文档** 与 **系统开发计划**，再按需查阅 DDD 架构与 Knife4j 集成；部署时参考 Docker 与 Nacos 文档。
