# 设置当前仓库 Git 使用 UTF-8，避免中文提交说明乱码
# 在项目根目录执行: .\scripts\setup-git-encoding.ps1

Set-Location $PSScriptRoot\..

git config core.quotepath false
git config i18n.commitEncoding utf-8
git config i18n.logOutputEncoding utf-8

Write-Host "Git encoding configured for this repo (UTF-8). Chinese commit messages will display correctly."
