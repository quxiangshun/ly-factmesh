# EMQX MQTT Broker

供 FactMesh 设备遥测、领域事件、跨节点同步等使用。

## 启动

```bash
docker compose -f tools/mqtt/docker-compose.yml up -d
```

## 端口

| 端口  | 协议         | 说明                          |
|-------|--------------|-------------------------------|
| 1883  | MQTT TCP     | 默认连接                      |
| 8883  | MQTT TLS     | 加密连接                      |
| 8083  | WebSocket    | 浏览器端 MQTT                 |
| 18083 | HTTP         | 控制台，默认账号 admin/public |

## 认证

开发环境默认允许匿名连接。生产环境建议：
1. 设置 `EMQX_ALLOW_ANONYMOUS=false`
2. 在 Dashboard(18083) → 访问控制 → 认证 中启用内置数据库认证
3. 添加用户 `factmesh_user` / `factmesh_pwd123`，与 `infra.mqtt` 配置对应

## 应用配置示例

在业务模块（如 mom-iot）的 `application.yml` 中配置：

```yaml
infra:
  mqtt:
    broker-url: tcp://127.0.0.1:1883
    client-id: factmesh-core-01
    username: factmesh_user
    password: factmesh_pwd123
    connect-timeout: 5
    keep-alive: 30
    auto-reconnect: true
    max-reconnect-interval: 10
    enable-tls: false  # 生产环境改为 true，并配置 tls-cert-path
    # 订阅主题：key=主题（支持通配符 +/#），value=QoS
    subscribe-topics:
      factmesh/input/device/+: 1   # 设备数据（QoS 1，至少一次）
      factmesh/input/order/+: 1    # 订单数据（QoS 1）
      factmesh/sync/edge/+: 2      # 跨节点同步（QoS 2，恰好一次）
```

## 主题约定

| 主题模式                 | 说明                     |
|--------------------------|--------------------------|
| factmesh/input/device/+  | 设备/业务系统上报事实数据 |
| factmesh/input/order/+   | 订单状态变更等           |
| factmesh/output/+        | FactMesh 处理后向下游分发 |
| factmesh/sync/edge/+      | 边缘节点跨节点同步       |
| factmesh/dead/letter     | 死信队列（规则引擎配置） |
