# FactMesh 边缘盒子（C）

极端低资源场景（单片机、无 OS 嵌入式），paho.mqtt.c。

## 构建

```bash
cd tools/edge-box/c
cmake -B build
cmake --build build
```

## 运行

```bash
./build/factmesh_edge_box -b tcp://127.0.0.1:1883 -d sensor-001 -i 3
```

详见 [父目录 README](../README.md)。
