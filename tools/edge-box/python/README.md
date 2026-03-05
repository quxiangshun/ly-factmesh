# FactMesh 边缘盒子（Python）

原型验证、脚本化，paho-mqtt。

## 安装

```bash
cd tools/edge-box/python
pip install -r requirements.txt
```

## 运行

```bash
python main.py -b 127.0.0.1 -p 1883 -d sensor-001 -i 3
# 或
python main.py -b tcp://192.168.1.100:1883 -d edge-node-01
```

详见 [父目录 README](../README.md)。
