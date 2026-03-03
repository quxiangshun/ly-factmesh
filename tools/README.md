# LY-FactMesh 工具与基础设施

## 一键启动基础环境

本地开发时，使用以下脚本一次性启动 PostgreSQL、Nacos（含 MySQL）、InfluxDB、EMQX（MQTT）、Seata：

**Windows：**
```bash
tools\start-env.bat
```

**Linux / Mac：**
```bash
chmod +x tools/start-env.sh
./tools/start-env.sh
```

或手动执行：
```bash
docker compose -f tools/docker-compose-base.yml up -d
```

### 启动的服务

| 服务 | 端口 | 说明 |
|------|------|------|
| PostgreSQL | 5432 | 业务数据库（admin/iot/mes/wms/qms/ops，由 Flyway 创建表） |
| MySQL | 3306 | Nacos 配置持久化 |
| Nacos | 8848 / 9848 | 服务注册与配置中心（HTTP / gRPC） |
| InfluxDB | 8086 | IoT 遥测时序数据 |
| EMQX | 1883 / 18083 | MQTT Broker，18083 为控制台 |
| Seata | 8091 | 分布式事务 TC |

业务模块端口（本地 bootRun / Docker 部署）：gateway 9090、admin 9091、iot 9092、mes 9093、wms 9094、qms 9095、ops 9096。

首次启动后，MySQL/Nacos 约需 30 秒完成初始化；业务模块启动时，Flyway 会自动执行迁移脚本创建表结构。

**若 EMQX/Seata 镜像拉取失败**，可先仅启动核心 4 项：
```bash
docker compose -f tools/docker-compose-base.yml up -d postgres mysql-nacos nacos influxdb
```

## 其他编排

| 文件 | 说明 |
|------|------|
| `docker-compose-base.yml` | 基础环境（PostgreSQL + Nacos + InfluxDB） |
| `nacos/docker-compose.yml` | 仅 Nacos + MySQL |
| `docker-compose-influxdb.yml` | 仅 InfluxDB |
| `pgsql/` | PostgreSQL 主从集群（读写分离） |
| `mqtt/` | EMQX MQTT Broker |
| `seata/` | Seata 分布式事务服务端 |
| `sql/` | 数据库初始化脚本 |
