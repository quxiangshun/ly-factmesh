# mom-ai

AI 预测分析模块，基于 Python + FastAPI + PyTorch 实现，支持**自定义训练**与推理。

## 环境要求

- Python 3.12+
- pip

## 安装与运行

### 启动步骤

```bash
cd mom-ai
# 推荐使用虚拟环境
python -m venv .venv
.venv\Scripts\activate   # Windows
# source .venv/bin/activate  # Linux/macOS

pip install -r requirements.txt
python main.py
```

启动成功后：
- **HTTP**：http://localhost:9097
- **gRPC**：localhost:9098（供 mom-admin 调用）

访问示例：
- http://localhost:9097/ — 返回服务信息，可确认是否为 mom-ai
- http://localhost:9097/api/ai/capabilities — 能力列表
- http://localhost:9097/docs — Swagger 文档
- http://localhost:9097/actuator/health — 健康检查

### 启动顺序（与 mom-admin 联调）

1. 先启动 **mom-ai**（本服务）
2. 再启动 **mom-admin**（通过 gRPC 连接 mom-ai）
3. 经网关访问：`GET http://localhost:9090/api/ai-grpc/capabilities`

## 接口说明

### HTTP (port 9096)
| 接口 | 方法 | 说明 |
|------|------|------|
| /api/ai/capabilities | GET | 获取 AI 能力列表及状态 |
| /api/ai/train | POST | 自定义训练（传 epochs、lr、train_x、train_y） |
| /api/ai/predict | POST | 推理预测 |
| /actuator/health | GET | 健康检查 |

### gRPC (port 9098)
mom-admin 通过 gRPC 调用 mom-ai，proto 见 `proto/ai_service.proto`。本地/开发时 ai.grpc.address=localhost:9098；Docker 中为 mom-ai:9098。

## 训练与推理示例

```bash
# 训练（可自定义 epochs、lr、训练数据）
curl -X POST http://localhost:9097/api/ai/train \
  -H "Content-Type: application/json" \
  -d '{"epochs": 20, "lr": 0.0005, "train_x": [[1.0]*10, [2.0]*10], "train_y": [0, 1]}'

# 推理
curl -X POST http://localhost:9097/api/ai/predict \
  -H "Content-Type: application/json" \
  -d '{"data": [[1.2, 3.4, 5.6, 7.8, 9.0, 1.2, 3.4, 5.6, 7.8, 9.0]]}'
```

## 模型持久化

训练后模型保存至 `factmesh_ai_model.pth`（可通过环境变量 `MOM_AI_MODEL_PATH` 指定路径）。

## 常见问题

### 1. gRPC 启动失败：`AttributeError: module 'grpc' has no attribute 'ThreadPoolExecutor'`

**原因**：gRPC Python 库中无 `grpc.ThreadPoolExecutor`，应使用标准库的 `concurrent.futures.ThreadPoolExecutor`。

**解决**：已在 `grpc_servicer.py` 中修正。若仍报错，请确保使用最新代码，或手动修改：

```python
from concurrent.futures import ThreadPoolExecutor
server = grpc.server(ThreadPoolExecutor(max_workers=4))
```

### 2. PyTorch 警告：`Failed to initialize NumPy: No module named 'numpy'`

**原因**：PyTorch 依赖 NumPy，若未安装会触发该警告。

**解决**：

```bash
pip install numpy
# 或重新安装依赖
pip install -r requirements.txt
```

### 3. gRPC 端口绑定失败：`Failed to add port to server` / `No address added`

**原因**：① 重复启动 gRPC（已修复）；② Windows 上 `0.0.0.0` 绑定可能失败；③ 端口 9098 被占用。

**解决**：
- 默认使用 `127.0.0.1:9098`（Windows 本地开发更稳定）
- Docker 中已设置 `MOM_AI_GRPC_HOST=0.0.0.0`
- 若端口被占用：`set MOM_AI_GRPC_PORT=9099` 后重启

### 4. 访问 /api/ai/capabilities 返回 404 或命中 mihomo

**原因**：端口 9097 常被 mihomo（Clash 内核）占用，返回 `{"hello":"mihomo"}`。

**解决**：mom-ai 默认已改为 **9096**。重启 mom-ai 后访问 http://localhost:9096/api/ai/capabilities。若 9096 仍冲突，可设置 `MOM_AI_HTTP_PORT=9095` 等。

### 5. FastAPI 弃用警告：`on_event is deprecated, use lifespan event handlers instead`

**说明**：仅弃用提示，不影响运行。后续版本会迁移到 `lifespan` 写法。

## 网关接入

网关将 `/api/ai/**` 转发至 mom-ai。本地开发时网关默认使用 `http://localhost:9097`；Docker 编排时通过环境变量 `MOM_AI_URI=http://mom-ai:9097` 配置。

## 扩展方向

- **替换模型**：将 `CustomAIModel` 替换为 Hugging Face LLM（BERT、LLaMA 等）
- **数据集成**：从 ly-factmesh 数据库或 API 读取训练数据
- **分布式训练**：使用 PyTorch Distributed 实现多卡/多机训练
- **部署优化**：导出 ONNX/TensorRT 提升推理速度
