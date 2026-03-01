# GIT_EDITOR when reword: overwrite message file with correct Chinese (UTF-8)
param([string]$MsgFile = $args[0])
$newMsg = @"
feat: P0+P1 - 基础平台、JDK25、统一响应体、IoT设备API、MES工单API

- P0: Nacos Discovery、OpenAPI 文档聚合、web 前端骨架、设计文档与开发计划、JDK25
- P1: mom-common Result/PageResult、mom-iot DeviceController、mom-mes 工单领域与API、docs P1 完成情况

Co-authored-by: Cursor <cursoragent@cursor.com>
"@
[System.IO.File]::WriteAllText($MsgFile, $newMsg.TrimEnd(), [System.Text.UTF8Encoding]::new($false))
