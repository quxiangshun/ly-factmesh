"""
mom-ai: AI 预测分析模块（可自定义训练）
FastAPI + PyTorch 实现，支持模型定义、自定义训练、推理；同时提供 gRPC 服务供 mom-admin 调用
"""
import os
from contextlib import asynccontextmanager

from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel

from ai_core import get_capabilities_dict, predict, train


def _run_grpc_server():
    """在后台线程中启动 gRPC 服务（仅启动一次）"""
    import threading

    from grpc_servicer import serve

    port = int(os.environ.get("MOM_AI_GRPC_PORT", "9098"))
    server = serve(port)
    server.start()
    server.wait_for_termination()


@asynccontextmanager
async def lifespan(app: FastAPI):
    """生命周期管理：启动时启动 gRPC，关闭时清理"""
    import threading

    t = threading.Thread(target=_run_grpc_server, daemon=True)
    t.start()
    yield
    # daemon 线程会随主进程退出自动结束


app = FastAPI(
    title="ly-factmesh AI 模块",
    description="AI 预测分析、智能决策，支持自定义训练",
    version="1.0.0",
    lifespan=lifespan,
)

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)


# 请求体模型
class TrainRequest(BaseModel):
    epochs: int = 10
    lr: float = 0.001
    train_x: list = [[1.0] * 10, [2.0] * 10]
    train_y: list = [0, 1]


class PredictRequest(BaseModel):
    data: list = [[1.0] * 10]


# ========== 根路径（便于确认服务已启动）==========
@app.get("/")
async def root():
    """根路径，返回服务信息"""
    return {"module": "mom-ai", "docs": "/docs", "capabilities": "/api/ai/capabilities"}


# ========== 能力查询 ==========
@app.get("/api/ai/capabilities")
async def get_capabilities():
    """获取当前 AI 模块支持的能力及状态"""
    return get_capabilities_dict()


# ========== 自定义训练 ==========
@app.post("/api/ai/train")
async def train_model(req: TrainRequest):
    """
    自定义训练接口
    支持传训练数据、超参数，训练后可持久化供推理使用
    """
    result = train(req.train_x, req.train_y, epochs=req.epochs, lr=req.lr)
    return {"status": "训练完成", "epochs": req.epochs, "lr": req.lr, **result}


# ========== 推理预测 ==========
@app.post("/api/ai/predict")
async def predict_api(req: PredictRequest):
    """
    推理接口
    加载已训练模型对输入数据进行预测
    """
    preds = predict(req.data)
    return {"input_data": req.data, "predictions": preds}


@app.get("/actuator/health")
async def health():
    """健康检查，供 Docker/K8s 使用"""
    return {"status": "UP"}


if __name__ == "__main__":
    import uvicorn

    uvicorn.run(app, host="0.0.0.0", port=9097)
