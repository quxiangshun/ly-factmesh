# Git 中文提交说明乱码修复

## 问题

在 Windows PowerShell 或 CMD 下执行 `git commit -m "中文描述"` 时，提交信息在历史中显示为乱码（如 `鍩虹骞冲彴`），原因是 Git 未统一使用 UTF-8 编码。

## 解决（推荐在项目根目录执行一次）

在**本仓库**内执行（仅影响当前项目）：

```powershell
# 项目根目录下执行
git config core.quotepath false
git config i18n.commitEncoding utf-8
git config i18n.logOutputEncoding utf-8
```

或直接运行脚本：

```powershell
.\scripts\setup-git-encoding.ps1
```

若希望**全局**生效（所有仓库），使用：

```powershell
git config --global core.quotepath false
git config --global i18n.commitEncoding utf-8
git config --global i18n.logOutputEncoding utf-8
```

## 说明

| 配置项 | 作用 |
|--------|------|
| `core.quotepath false` | 路径中的非 ASCII 字符正常显示，不转义 |
| `i18n.commitEncoding utf-8` | 提交信息按 UTF-8 存储 |
| `i18n.logOutputEncoding utf-8` | `git log` 等输出使用 UTF-8 |

配置后，使用中文写 commit message 即可正常显示。

## 已修复历史中的乱码提交

历史中曾有一条乱码提交（如 `鍩虹骞冲彴銆丣DK25...`）已通过 rebase 改为正确中文：

- **修复前**：`feat: P0+P1 - 鍩虹骞冲彴銆丣DK25銆佺粺涓€鍝嶅簲浣撱€両oT璁惧API銆丮ES宸ュ崟API`
- **修复后**：`feat: P0+P1 - 基础平台、JDK25、统一响应体、IoT设备API、MES工单API`

本地分支 `feature/continue-p1` 已包含修复后的历史。因网络原因未自动推送到 GitHub，**请在网络可用时在本仓库根目录执行**：

```powershell
git push origin feature/continue-p1 --force-with-lease
```

推送后 GitHub 上的该分支将显示正确中文提交说明（会重写远程历史，若他人基于该分支开发需先同步）。
