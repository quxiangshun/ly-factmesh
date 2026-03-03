# 执行 SQL 脚本的 PowerShell 脚本
Write-Host "=== 开始执行 SQL 脚本 ==="

# 设置容器名称和用户名
$container = "ly-factmesh-postgres"
$user = "postgres"

# 1. 先创建 ly_factmesh_ops 数据库（运维库），因为之前可能缺少
docker exec -i $container psql -U $user -c "CREATE DATABASE ly_factmesh_ops;" 2>$null
Write-Host "数据库 ly_factmesh_ops 创建完成"

# 2. 执行每个 SQL 脚本，跳过 CREATE DATABASE 语句
# ly_factmesh_admin 脚本
Write-Host "执行 ly_factmesh_admin.sql..."
Get-Content "d:\Users\73559\IdeaProjects\LY-FactMesh\ly-factmesh\tools\sql\ly_factmesh_admin.sql" |
Where-Object { $_ -notlike "*CREATE DATABASE*" } | 
docker exec -i $container psql -U $user 2>$null
Write-Host "ly_factmesh_admin.sql 执行完成"

# ly_factmesh_ops 脚本（运维库：全局日志、审计、系统事件）
Write-Host "执行 ly_factmesh_ops.sql..."
Get-Content "d:\Users\73559\IdeaProjects\LY-FactMesh\ly-factmesh\tools\sql\ly_factmesh_ops.sql" |
Where-Object { $_ -notlike "*CREATE DATABASE*" } | 
docker exec -i $container psql -U $user 2>$null
Write-Host "ly_factmesh_ops.sql 执行完成"

# ly_factmesh_iot 脚本
Write-Host "执行 ly_factmesh_iot.sql..."
Get-Content "d:\Users\73559\IdeaProjects\LY-FactMesh\ly-factmesh\tools\sql\ly_factmesh_iot.sql" |
Where-Object { $_ -notlike "*CREATE DATABASE*" } | 
docker exec -i $container psql -U $user 2>$null
Write-Host "ly_factmesh_iot.sql 执行完成"

# ly_factmesh_mes 脚本
Write-Host "执行 ly_factmesh_mes.sql..."
Get-Content "d:\Users\73559\IdeaProjects\LY-FactMesh\ly-factmesh\tools\sql\ly_factmesh_mes.sql" |
Where-Object { $_ -notlike "*CREATE DATABASE*" } | 
docker exec -i $container psql -U $user 2>$null
Write-Host "ly_factmesh_mes.sql 执行完成"

# ly_factmesh_qms 脚本
Write-Host "执行 ly_factmesh_qms.sql..."
Get-Content "d:\Users\73559\IdeaProjects\LY-FactMesh\ly-factmesh\tools\sql\ly_factmesh_qms.sql" |
Where-Object { $_ -notlike "*CREATE DATABASE*" } | 
docker exec -i $container psql -U $user 2>$null
Write-Host "ly_factmesh_qms.sql 执行完成"

# ly_factmesh_wms 脚本
Write-Host "执行 ly_factmesh_wms.sql..."
Get-Content "d:\Users\73559\IdeaProjects\LY-FactMesh\ly-factmesh\tools\sql\ly_factmesh_wms.sql" |
Where-Object { $_ -notlike "*CREATE DATABASE*" } | 
docker exec -i $container psql -U $user 2>$null
Write-Host "ly_factmesh_wms.sql 执行完成"

Write-Host "=== 所有脚本执行完成 ==="