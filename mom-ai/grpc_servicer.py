"""
gRPC 服务端实现，供 mom-admin 通过 gRPC 调用
"""
import os
from concurrent.futures import ThreadPoolExecutor

import grpc

from proto import ai_service_pb2, ai_service_pb2_grpc

from ai_core import get_capabilities_dict, predict, train


class AiServicer(ai_service_pb2_grpc.AiServiceServicer):
    def GetCapabilities(self, request, context):
        d = get_capabilities_dict()
        caps = [
            ai_service_pb2.Capability(
                code=c["code"], name=c["name"], status=c["status"], description=c["description"]
            )
            for c in d["capabilities"]
        ]
        return ai_service_pb2.GetCapabilitiesResponse(
            module=d["module"], version=d["version"], lang=d["lang"], capabilities=caps
        )

    def Train(self, request, context):
        train_x = [list(fx.values) for fx in request.train_x]
        train_y = list(request.train_y)
        result = train(train_x, train_y, epochs=request.epochs, lr=request.lr)
        return ai_service_pb2.TrainResponse(
            status="训练完成",
            epochs=request.epochs,
            lr=request.lr,
            final_loss=result["final_loss"],
            model_path=result["model_path"],
        )

    def Predict(self, request, context):
        data = [list(d.values) for d in request.data]
        preds = predict(data)
        input_data = [ai_service_pb2.FloatArray(values=row) for row in data]
        return ai_service_pb2.PredictResponse(input_data=input_data, predictions=preds)


def serve(port: int = 9098):
    server = grpc.server(ThreadPoolExecutor(max_workers=4))
    ai_service_pb2_grpc.add_AiServiceServicer_to_server(AiServicer(), server)
    # Windows 上 0.0.0.0 可能绑定失败，可用 127.0.0.1；Docker 需 0.0.0.0
    host = os.environ.get("MOM_AI_GRPC_HOST", "127.0.0.1")
    server.add_insecure_port(f"{host}:{port}")
    return server
