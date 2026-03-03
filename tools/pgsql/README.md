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

## Flyway 迁移

**仅在主库执行 Flyway 迁移，从库通过 WAL 流复制自动同步表结构和数据。**

### 推荐配置

```yaml
flyway:
  enabled: true
  locations: classpath:db/migration
  baseline-on-migrate: true   # 对已有数据库首次迁移时自动创建基线
  clean-disabled: true        # 禁用 clean，防止误删数据
  validate-on-migrate: true   # 迁移前校验脚本版本
```

各业务模块的 DataSource 需**仅指向主库**执行 Flyway，不可连接从库。从库无需配置 Flyway。

### 主从迁移原则

- **禁止将 Flyway 指向从库**：PostgreSQL 从库默认只读，执行 DDL 会失败；即便可写，也会导致主从结构不一致、复制异常。
- **从库自动同步**：主库执行完迁移后，从库通过 WAL 流复制自动同步 DDL 与数据，无需额外操作。
- **复杂迁移**：大批量或高风险迁移时，可临时暂停从库复制（`pg_wal_replay_pause()`），迁移完成后再恢复。
- **迁移后校验**：通过 `pg_stat_wal_receiver` 或对比主从表结构，确认复制状态与结构一致。

## 停止

```bash
docker compose down
```

若要同时删除数据卷：`docker compose down -v`
