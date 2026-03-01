# 贡献指南
## 分支规范
- `main`：主分支，仅接受已审核的PR，保持稳定；
- `develop`：开发分支，日常开发提交到此；
- `feature/xxx`：功能分支，从develop创建，完成后合并回develop；
- `bugfix/xxx`：bug修复分支，从develop创建，完成后合并回develop。

## 提交规范
提交信息格式：`类型: 描述`
类型包括：feat（新功能）、fix（修复）、docs（文档）、style（格式）、refactor（重构）、test（测试）、chore（构建）

示例：`feat: 新增mom-admin模块的租户管理功能`

**中文乱码**：若 commit 信息在 `git log` 中显示乱码，请执行一次 `.\scripts\setup-git-encoding.ps1` 或参考 [Git 中文提交说明乱码修复](GIT_COMMIT_ENCODING.md)。

## PR流程
1. Fork 本仓库；
2. 创建特性分支：`git checkout -b feature/xxx`；
3. 提交代码：`git commit -m 'feat: xxx'`；
4. 推送分支：`git push origin feature/xxx`；
5. 发起 Pull Request 到 develop 分支。