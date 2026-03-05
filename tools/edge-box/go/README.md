# FactMesh 边缘盒子（Go）

资源适中边缘设备（树莓派、工控机），paho.mqtt.golang。

## 构建

```bash
cd tools/edge-box/go
go build -o factmesh_edge_box .
```

## 运行

```bash
./factmesh_edge_box -b tcp://127.0.0.1:1883 -d sensor-001 -i 3
```

详见 [父目录 README](../README.md)。
