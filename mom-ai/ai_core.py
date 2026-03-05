"""
AI 核心逻辑：模型、训练、推理（供 HTTP 与 gRPC 共享）
"""
import os

import torch
import torch.nn as nn

MODEL_PATH = os.environ.get("MOM_AI_MODEL_PATH", "factmesh_ai_model.pth")


class CustomAIModel(nn.Module):
    def __init__(self, input_dim: int = 10, output_dim: int = 2, hidden_dim: int = 32):
        super().__init__()
        self.fc1 = nn.Linear(input_dim, hidden_dim)
        self.fc2 = nn.Linear(hidden_dim, output_dim)
        self.relu = nn.ReLU()

    def forward(self, x: torch.Tensor) -> torch.Tensor:
        x = self.relu(self.fc1(x))
        x = self.fc2(x)
        return x


model = CustomAIModel()
loss_fn = nn.CrossEntropyLoss()
optimizer = torch.optim.Adam(model.parameters(), lr=0.001)


def train(train_x: list, train_y: list, epochs: int = 10, lr: float = 0.001) -> dict:
    optimizer.param_groups[0]["lr"] = lr
    x = torch.tensor(train_x, dtype=torch.float32)
    y = torch.tensor(train_y, dtype=torch.long)
    model.train()
    final_loss = 0.0
    for _ in range(epochs):
        optimizer.zero_grad()
        outputs = model(x)
        loss = loss_fn(outputs, y)
        loss.backward()
        optimizer.step()
        final_loss = loss.item()
    torch.save(model.state_dict(), MODEL_PATH)
    return {"final_loss": final_loss, "model_path": MODEL_PATH}


def predict(data: list) -> list:
    try:
        model.load_state_dict(torch.load(MODEL_PATH))
    except FileNotFoundError:
        pass
    model.eval()
    with torch.no_grad():
        x = torch.tensor(data, dtype=torch.float32)
        outputs = model(x)
        pred = torch.argmax(outputs, dim=1).tolist()
    return pred


def get_capabilities_dict() -> dict:
    return {
        "module": "mom-ai",
        "version": "1.0-SNAPSHOT",
        "lang": "python",
        "capabilities": [
            {"code": "custom_train", "name": "自定义训练", "status": "implemented",
             "description": "POST /api/ai/train 或 gRPC Train"},
            {"code": "predict", "name": "推理预测", "status": "implemented",
             "description": "POST /api/ai/predict 或 gRPC Predict"},
            {"code": "device_fault_prediction", "name": "设备故障预测", "status": "delegated",
             "description": "由 mom-iot 提供统计预测"},
        ],
    }
