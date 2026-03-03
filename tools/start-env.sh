#!/usr/bin/env bash
set -e

# 切换到项目根目录（脚本位于 tools/ 下）
cd "$(dirname "$0")/.."

echo ""
echo "========================================"
echo "  LY-FactMesh 基础环境一键启动"
echo "========================================"
echo ""
echo "将启动以下服务："
echo "  - PostgreSQL  5432  （各业务库）"
echo "  - MySQL       3306  （Nacos 持久化）"
echo "  - Nacos       8848  （服务注册与配置中心）"
echo "  - InfluxDB    8086  （IoT 遥测时序库）"
echo "  - EMQX        1883  （MQTT Broker）"
echo "  - Seata       8091  （分布式事务）"
echo ""

docker compose -f tools/docker-compose-base.yml up -d

echo ""
echo "[完成] 基础环境已启动"
echo ""
echo "服务地址："
echo "  PostgreSQL  jdbc:postgresql://localhost:5432/"
echo "  Nacos       http://localhost:8848/nacos"
echo "  InfluxDB    http://localhost:8086"
echo "  EMQX        tcp://localhost:1883   Dashboard http://localhost:18083"
echo "  Seata       127.0.0.1:8091"
echo ""
echo "提示：首次启动 MySQL/Nacos 需等待约 30 秒初始化完成"
echo "      业务模块启动后，Flyway 将自动执行迁移创建表结构"
echo ""
