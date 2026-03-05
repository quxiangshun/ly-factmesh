# FactMesh 边缘盒子（Java）

与 FactMesh 核心同语言，Paho MQTT Java，fat jar 部署。

## 构建

```bash
# 从项目根目录
./gradlew :tools:edge-box:java:jar
```

产物：`tools/edge-box/java/build/libs/factmesh-edge-box-java-1.0-SNAPSHOT.jar`

## 运行

```bash
cd tools/edge-box/java
java -jar build/libs/factmesh-edge-box-java-1.0-SNAPSHOT.jar -b tcp://127.0.0.1:1883 -d sensor-001 -i 3
```

详见 [父目录 README](../README.md)。
