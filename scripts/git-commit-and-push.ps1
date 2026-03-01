# LY-FactMesh 提交并推送到 GitHub
# 在项目根目录执行: .\scripts\git-commit-and-push.ps1

Set-Location $PSScriptRoot\..

# 1. 不再跟踪各模块 bin 目录（若之前被跟踪过）
$bins = @("mom-admin/bin", "mom-common/bin", "mom-gateway/bin", "mom-infra/bin", "mom-iot/bin", "mom-mes/bin", "mom-wms/bin", "mom-qms/bin")
foreach ($b in $bins) {
    if (Test-Path $b) {
        git rm -r --cached $b 2>$null
    }
}

# 2. 暂存本次需要的变更（不含 bin）
git add .gitignore README.md build.gradle
git add docs/
git add web/
git add mom-admin/build.gradle mom-admin/Dockerfile mom-admin/src
git add mom-common/build.gradle mom-common/src
git add mom-gateway/build.gradle mom-gateway/Dockerfile mom-gateway/src
git add mom-infra/build.gradle
git add mom-iot/build.gradle mom-iot/Dockerfile mom-iot/src
git add mom-mes/build.gradle mom-mes/Dockerfile mom-mes/src
git add mom-wms/build.gradle mom-wms/Dockerfile mom-wms/src
git add mom-qms/build.gradle mom-qms/Dockerfile mom-qms/src

# 3. 提交（按需修改下方 commit message）
git commit -m "feat: P1 核心业务 - 统一响应体、IoT 设备 API、MES 工单 API

- mom-common: 新增 Result<T>、PageResult<T> 统一响应与分页
- mom-iot: DeviceController、DeviceRegisterRequest，设备注册/上下线/状态/列表/删除 API
- mom-mes: 工单领域 WorkOrder、WorkOrderRepository、工单创建/分页/下发/开始/完成 API
- docs: 开发计划 P1 完成情况（M1-1~M1-3）"

# 4. 推送到当前分支
$branch = git rev-parse --abbrev-ref HEAD
git push origin $branch

Write-Host "Done. Pushed to origin/$branch"
