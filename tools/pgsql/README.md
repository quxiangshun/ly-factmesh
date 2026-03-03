# PostgreSQL 主从集群

用于 LY-FactMesh 读写分离的 PostgreSQL 主从集群，主库负责写操作，从库负责读操作。

## 启动

```bash
cd tools/pgsql
docker compose up -d
```

- **主库（Master）**：`localhost:5432`，用于写操作
- **从库（Slave）**：`localhost:5433`，用于读操作

## 配置读写分离

在各业务模块（mom-admin、mom-iot、mom-mes、mom-wms、mom-qms）的 `application.yml` 中启用读写分离：

```yaml
infra:
  datasource:
    read-write-split:
      enabled: true
      master-url: jdbc:postgresql://localhost:5432/ly_factmesh_admin   # 按模块替换数据库名
      slave-url: jdbc:postgresql://localhost:5433/ly_factmesh_admin   # 同上
      username: postgres
      password: postgres
      driver-class-name: org.postgresql.Driver
```

各模块对应数据库：

| 模块     | 数据库名           |
|----------|--------------------|
| mom-admin | ly_factmesh_admin  |
| mom-iot   | ly_factmesh_iot    |
| mom-mes   | ly_factmesh_mes    |
| mom-wms   | ly_factmesh_wms    |
| mom-qms   | ly_factmesh_qms    |

## 使用 @ReadOnly

在 Service 或 Mapper 的**读方法**上标注 `@ReadOnly`，执行时自动路由到从库：

```java
@ReadOnly
public List<Device> listDevices(Long tenantId) {
    return deviceMapper.selectByTenantId(tenantId);
}
```

- 未标注 `@ReadOnly` 的方法：路由到主库（写）
- 标注 `@ReadOnly` 的方法：路由到从库（读）

## 停止

```bash
docker compose down
```

若要同时删除数据卷：`docker compose down -v`
