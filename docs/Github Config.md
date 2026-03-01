# LY-FactMesh GitHub 分支规则配置手册
## 文档说明
- 适用分支：`main`（主分支）、`develop`（开发分支）
- 配置目标：保障核心分支代码安全，强制开源协作规范，兼顾代码质量与审核效率
- 生效状态：`Active`（直接生效，测试阶段可设为 `Evaluate`）

## 一、分支操作限制（核心保护）
| 规则项 | 配置选择 | 详细说明 & 适配理由 |
|--------|----------|----------------|
| **Restrict creations** <br>（限制分支创建） | ✅ 开启 <br>☑️ Only allow users with bypass permission to create matching refs | 1.  授权范围：仅项目负责人 + 核心维护者拥有 bypass 权限<br>2.  规则目的：禁止普通贡献者误创建 `main`/`develop` 分支<br>3.  协作适配：普通用户仅允许创建 `feature/*`/`bugfix/*` 分支 |
| **Restrict updates** <br>（限制分支更新） | ✅ 开启 <br>☑️ Only allow users with bypass permission to update matching refs | 1.  授权范围：仅核心维护者可绕过（用于紧急修复场景）<br>2.  规则目的：彻底禁止直接推送到核心分支，所有修改必须走 PR 流程<br>3.  核心价值：保障每一行代码都经过审核 |
| **Restrict deletions** <br>（限制分支删除） | ✅ 开启 <br>☑️ Only allow users with bypass permissions to delete matching refs | 1.  授权范围：仅项目负责人拥有删除权限（几乎不使用）<br>2.  规则目的：防止误删核心分支导致代码库丢失<br>3.  底线保障：开源项目代码不可恢复性风险防控 |
| **Block force pushes** <br>（禁止强制推送） | ✅ 开启 | 1.  规则目的：禁止任何用户通过强制推送覆盖分支历史<br>2.  配合规则：与「Require linear history」联动，保障提交记录可追溯 |

## 二、PR 合并前置规则（核心管控）
| 规则项 | 配置选择 | 详细说明 & 适配理由 |
|--------|----------|----------------|
| **Require a pull request before merging** <br>（合并前必须提交 PR） | ✅ 开启 | 基础规则：所有代码修改必须通过 PR 提交，禁止直接合并 |
| **Required approvals** <br>（必填审核数量） | 填写 `1` | 1.  配置依据：开源项目初期维护者较少，1人审核即可满足需求<br>2.  进阶调整：项目成熟后可增至 `2` 人审核 |
| **Dismiss stale pull request approvals when new commits are pushed** <br>（新提交后清空旧审核） | ✅ 开启 | 1.  规则目的：避免审核人员未查看新提交代码就批准合并<br>2.  协作价值：保证审核的有效性，防止遗留问题 |
| **Require review from specific teams** <br>（要求指定团队审核，预览功能） | ❌ 关闭 | 1.  关闭原因：个人开源仓库无「团队」概念<br>2.  进阶启用：项目转为组织仓库后，可按模块配置审核团队（如 `iot-team` 审核 `mom-iot` 代码） |
| **Require review from Code Owners** <br>（要求代码所有者审核） | ✅ 开启 | 1.  前置条件：仓库根目录需配置 `CODEOWNERS` 文件<br>2.  规则目的：确保核心模块代码修改必须经过模块负责人审核 |
| **Require approval of the most recent reviewable push** <br>（最新提交需他人审核） | ✅ 开启 | 1.  规则目的：禁止提交者自我审核，保证审核的客观性<br>2.  协作价值：避免「自己写自己审」的形式化流程 |
| **Require conversation resolution before merging** <br>（合并前解决所有讨论） | ✅ 开启 | 1.  规则目的：PR 中所有代码讨论必须达成一致后才能合并<br>2.  质量价值：避免带着争议代码进入核心分支 |
| **Allowed merge methods** <br>（允许的合并方式） | ✅ 勾选 `Squash and merge`（压缩合并）<br>✅ 勾选 `Rebase and merge`（变基合并）<br>❌ 取消 `Create a merge commit`（普通合并） | 1.  压缩合并：将 PR 所有提交压缩为 1 个，保持分支历史简洁<br>2.  变基合并：保持线性历史，无多余 merge commit<br>3.  禁用普通合并：避免分支历史混乱，便于版本追溯 |

## 三、自动化检查规则（质量保障）
| 规则项 | 配置选择 | 详细说明 & 适配理由 |
|--------|----------|----------------|
| **Require status checks to pass** <br>（要求状态检查通过） | ✅ 开启 | 1.  需添加检查项：`build`（编译检查）、`test`（单元测试检查）<br>2.  前置条件：仓库需配置 GitHub Actions 工作流（`.github/workflows/ci.yml`）<br>3.  规则目的：强制代码通过编译和单元测试，避免低级错误 |
| **Require branches to be up to date before merging** <br>（合并前分支需同步最新代码） | ✅ 开启 | 1.  前置条件：已开启「Require status checks to pass」<br>2.  规则目的：确保 PR 基于最新代码测试，避免合并后出现冲突或隐藏 bug |
| **Do not require status checks on creation** <br>（创建分支时不要求状态检查） | ✅ 开启 | 1.  规则目的：创建分支是基础操作，无需触发 CI 检查<br>2.  资源价值：减少不必要的 GitHub Actions 资源消耗 |

## 四、代码安全 & 质量规则（进阶管控）
| 规则项 | 配置选择 | 详细说明 & 适配理由 |
|--------|----------|----------------|
| **Require linear history** <br>（要求线性历史） | ✅ 开启 | 1.  规则目的：禁止 merge commit 混入核心分支<br>2.  配合规则：与「Allowed merge methods」联动，保证提交历史清晰可追溯 |
| **Require deployments to succeed** <br>（要求部署成功） | ❌ 关闭 | 1.  关闭原因：开源项目初期无固定部署环境<br>2.  进阶启用：实现测试环境自动部署后，可开启并选择 `test` 环境 |
| **Require signed commits** <br>（要求签名提交） | ✅ 开启 | 1.  规则目的：防止提交被篡改，保证代码提交者身份真实<br>2.  配置要求：核心维护者必须配置 GPG 签名，普通贡献者建议开启<br>3.  适配场景：企业级 MOM 系统涉及工业数据，需保障代码可信度 |
| **Require code scanning results** <br>（要求代码扫描结果） | ✅ 开启 <br>选择工具：`CodeQL` | 1.  前置条件：仓库需配置 CodeQL 扫描工作流（`.github/workflows/codeql-analysis.yml`）<br>2.  规则目的：自动扫描代码漏洞（如 SQL 注入、空指针异常）<br>3.  适配场景：MES/IoT 系统需保障代码安全性 |
| **Require code quality results** <br>（要求代码质量结果） | ✅ 开启 <br>Severity：`Error`（初期）→ `Error+Warning`（成熟后） | 1.  初期配置：仅阻断 `Error` 级问题（语法错误、高危漏洞）<br>2.  进阶配置：项目成熟后阻断 `Warning` 级问题（代码规范、性能问题）<br>3.  质量价值：逐步提升代码库整体质量 |
| **Automatically request Copilot code review** <br>（自动请求 Copilot 审核） | ✅ 开启 | 子选项配置：<br>✅ 开启 `Review new pushes`（审核每次新提交）<br>✅ 开启 `Review draft pull requests`（审核草稿 PR）<br>规则目的：AI 辅助审核，发现人工遗漏问题，不替代人工审核 |
| **Manage static analysis tools in Copilot code review** <br>（Copilot 集成静态分析） | ✅ 开启 <br>选择工具：`CodeQL` + `Standard queries` | 1.  规则目的：让 Copilot 审核时引用 CodeQL 扫描结果，给出精准修改建议<br>2.  协作价值：减少人工重复审核工作 |

## 五、配套配置文件（必选）
### 1. `CODEOWNERS` 文件（仓库根目录）