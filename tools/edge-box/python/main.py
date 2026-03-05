#!/usr/bin/env python3
"""
FactMesh 边缘盒子（Python）- 边缘端数据采集与上报

运行：python main.py [-b broker] [-d device_id] [-i interval_sec]
依赖：pip install -r requirements.txt
"""

import argparse
import json
import signal
import sys
import time

import paho.mqtt.client as mqtt

TOPIC_PREFIX = "factmesh/input/device/"
DEFAULT_BROKER = "127.0.0.1"
DEFAULT_PORT = 1883
DEFAULT_DEVICE_ID = "edge-box-01"
DEFAULT_INTERVAL = 5
QOS = 1

running = True


def on_connect(client, userdata, flags, rc):
    if rc == 0:
        print("Connected to broker")
    else:
        print(f"Connect failed: {rc}")


def build_payload(device_id: str) -> str:
    now = int(time.time())
    temp = 20.0 + (now % 150) / 10.0
    hum = 40.0 + (now % 400) / 10.0
    return json.dumps({
        "deviceId": device_id,
        "timestamp": now,
        "temperature": round(temp, 1),
        "humidity": round(hum, 1),
    })


def run(broker: str, port: int, device_id: str, interval: int) -> None:
    topic = TOPIC_PREFIX + device_id
    client_id = f"factmesh-edge-python-{device_id}"

    try:
        client = mqtt.Client(
            callback_api_version=mqtt.CallbackAPIVersion.VERSION1,
            client_id=client_id,
        )
    except TypeError:
        client = mqtt.Client(client_id=client_id)
    client.on_connect = on_connect

    try:
        client.connect(broker, port, keepalive=30)
    except Exception as e:
        print(f"Connect failed: {e}", file=sys.stderr)
        sys.exit(1)

    client.loop_start()
    print(f"Publishing to {topic} every {interval}s")

    try:
        while running:
            payload = build_payload(device_id)
            result = client.publish(topic, payload, qos=QOS)
            if result.rc == mqtt.MQTT_ERR_SUCCESS:
                print(f"[{int(time.time())}] Published {len(payload)} bytes to {topic}")
            else:
                print(f"Publish failed: {result.rc}", file=sys.stderr)
            for _ in range(interval):
                if not running:
                    break
                time.sleep(1)
    finally:
        client.loop_stop()
        client.disconnect()


def main() -> None:
    global running

    def stop(signum=None, frame=None):
        global running
        running = False

    signal.signal(signal.SIGINT, stop)
    signal.signal(signal.SIGTERM, stop)

    parser = argparse.ArgumentParser(description="FactMesh Edge Box (Python)")
    parser.add_argument("-b", "--broker", default=DEFAULT_BROKER, help="MQTT broker (host or tcp://host:port)")
    parser.add_argument("-p", "--port", type=int, default=DEFAULT_PORT, help="MQTT broker port")
    parser.add_argument("-d", "--device-id", default=DEFAULT_DEVICE_ID, help="Device ID")
    parser.add_argument("-i", "--interval", type=int, default=DEFAULT_INTERVAL, help="Publish interval (seconds)")
    args = parser.parse_args()

    if args.interval < 1:
        args.interval = 1

    broker = args.broker
    port = args.port
    if broker.startswith("tcp://"):
        parts = broker.replace("tcp://", "").split(":")
        broker = parts[0] if parts else DEFAULT_BROKER
        port = int(parts[1]) if len(parts) > 1 else DEFAULT_PORT

    print(f"FactMesh Edge Box (Python) - broker={broker}:{port} device={args.device_id} interval={args.interval}s")
    run(broker, port, args.device_id, args.interval)


if __name__ == "__main__":
    main()
