/**
 * FactMesh 边缘盒子 - 配置头文件
 * 适配极端低资源边缘设备，可通过编译宏或运行时参数覆盖
 */
#ifndef FACTMESH_EDGE_BOX_CONFIG_H
#define FACTMESH_EDGE_BOX_CONFIG_H

/* 默认 MQTT Broker（可通过 -b 覆盖） */
#define DEFAULT_MQTT_BROKER    "tcp://127.0.0.1:1883"

/* 默认设备 ID（可通过 -d 覆盖） */
#define DEFAULT_DEVICE_ID      "edge-box-01"

/* 默认发布周期（秒） */
#define DEFAULT_PUBLISH_INTERVAL  5

/* 主题前缀，与 FactMesh Java 端约定一致 */
#define TOPIC_PREFIX_INPUT     "factmesh/input/device/"

/* 最大 payload 长度 */
#define MAX_PAYLOAD_LEN        256

/* QoS：0=至多一次，1=至少一次，2=恰好一次 */
#define DEFAULT_QOS           1

#endif /* FACTMESH_EDGE_BOX_CONFIG_H */
