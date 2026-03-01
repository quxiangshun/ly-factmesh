# Add, commit (if changes), push to GitHub
$ErrorActionPreference = "Stop"
Set-Location $PSScriptRoot\..

git config i18n.commitEncoding utf-8 2>$null
$status = git status --porcelain
if ($status) {
    git add -A
    git commit -m "feat: P1 M1-6 跨域流程、提交编码文档与 rebase 脚本"
}
git push origin feature/continue-p1 --force-with-lease
