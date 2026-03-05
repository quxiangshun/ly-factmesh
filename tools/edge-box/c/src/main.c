/**
 * FactMesh 边缘盒子（C）- 边缘端数据采集与上报
 *
 * 适用场景：传感器数据采集、硬件控制、无 OS 嵌入式设备、极致资源占用场景
 * 与 FactMesh Java 核心通过 MQTT 通信，发布到 factmesh/input/device/{deviceId}
 *
 * 依赖：paho.mqtt.c (libpaho-mqtt3c)
 * 编译：cd c && cmake -B build && cmake --build build
 * 运行：./build/factmesh_edge_box [-b broker] [-d device_id] [-i interval_sec]
 */
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <time.h>

#ifdef _WIN32
#include <winsock2.h>
#include <windows.h>
#define sleep(x) Sleep((x) * 1000)
#else
#include <unistd.h>
#include <signal.h>
#endif

#include "MQTTClient.h"
#include "config.h"

#define CLIENT_ID_PREFIX   "factmesh-edge-"
#define KEEP_ALIVE         30
#define TIMEOUT_MS         5000

static volatile int g_running = 1;
static MQTTClient g_client;

#ifndef _WIN32
static void handle_signal(int sig) {
    (void)sig;
    g_running = 0;
}
#endif

/**
 * 生成模拟传感器数据（实际部署时可替换为真实硬件采集）
 */
static int build_payload(char* buf, size_t buf_len, const char* device_id) {
    time_t now = time(NULL);
    double temp = 20.0 + (double)(now % 150) / 10.0;
    double hum = 40.0 + (double)(now % 400) / 10.0;
    return (int)snprintf(buf, buf_len,
        "{\"deviceId\":\"%s\",\"timestamp\":%ld,\"temperature\":%.1f,\"humidity\":%.1f}",
        device_id, (long)now, temp, hum);
}

static int run(const char* broker, const char* device_id, int interval_sec) {
    MQTTClient_connectOptions conn_opts = MQTTClient_connectOptions_initializer;
    MQTTClient_message pubmsg = MQTTClient_message_initializer;
    MQTTClient_deliveryToken token;

    char topic[128];
    char payload[MAX_PAYLOAD_LEN];
    char client_id[64];
    int rc;

    snprintf(topic, sizeof(topic), "%s%s", TOPIC_PREFIX_INPUT, device_id);
    snprintf(client_id, sizeof(client_id), "%s%s", CLIENT_ID_PREFIX, device_id);

    conn_opts.keepAliveInterval = KEEP_ALIVE;
    conn_opts.cleansession = 0;

    rc = MQTTClient_create(&g_client, broker, client_id, MQTTCLIENT_PERSISTENCE_NONE, NULL);
    if (rc != MQTTCLIENT_SUCCESS) {
        fprintf(stderr, "MQTT create failed: %d\n", rc);
        return -1;
    }

    rc = MQTTClient_connect(g_client, &conn_opts);
    if (rc != MQTTCLIENT_SUCCESS) {
        fprintf(stderr, "MQTT connect failed: %d\n", rc);
        MQTTClient_destroy(&g_client);
        return -1;
    }

    printf("Connected to %s, publishing to %s every %d s\n", broker, topic, interval_sec);

    while (g_running) {
        int len = build_payload(payload, sizeof(payload), device_id);
        if (len <= 0 || (size_t)len >= sizeof(payload)) continue;

        pubmsg.payload = payload;
        pubmsg.payloadlen = (size_t)len;
        pubmsg.qos = DEFAULT_QOS;
        pubmsg.retained = 0;

        rc = MQTTClient_publishMessage(g_client, topic, &pubmsg, &token);
        if (rc != MQTTCLIENT_SUCCESS) {
            fprintf(stderr, "Publish failed: %d\n", rc);
        } else {
            MQTTClient_waitForCompletion(g_client, token, TIMEOUT_MS);
            printf("[%ld] Published %d bytes to %s\n", (long)time(NULL), len, topic);
        }

        for (int i = 0; i < interval_sec && g_running; i++) {
            sleep(1);
        }
    }

    MQTTClient_disconnect(g_client, 1000);
    MQTTClient_destroy(&g_client);
    return 0;
}

static void usage(const char* prog) {
    printf("Usage: %s [options]\n", prog);
    printf("  -b <broker>     MQTT broker (default: %s)\n", DEFAULT_MQTT_BROKER);
    printf("  -d <device_id> Device ID (default: %s)\n", DEFAULT_DEVICE_ID);
    printf("  -i <seconds>   Publish interval (default: %d)\n", DEFAULT_PUBLISH_INTERVAL);
    printf("  -h              Show this help\n");
}

int main(int argc, char* argv[]) {
    const char* broker = DEFAULT_MQTT_BROKER;
    const char* device_id = DEFAULT_DEVICE_ID;
    int interval = DEFAULT_PUBLISH_INTERVAL;

#ifdef _WIN32
    WSADATA wsa;
    if (WSAStartup(MAKEWORD(2, 2), &wsa) != 0) {
        fprintf(stderr, "WSAStartup failed\n");
        return -1;
    }
#endif

#ifndef _WIN32
    signal(SIGINT, handle_signal);
    signal(SIGTERM, handle_signal);

    int opt;
    while ((opt = getopt(argc, argv, "b:d:i:h")) != -1) {
        switch (opt) {
            case 'b': broker = optarg; break;
            case 'd': device_id = optarg; break;
            case 'i': interval = atoi(optarg); if (interval < 1) interval = 1; break;
            case 'h': usage(argv[0]); return 0;
            default: usage(argv[0]); return 1;
        }
    }
#else
    for (int i = 1; i < argc; i++) {
        if (strcmp(argv[i], "-h") == 0) { usage(argv[0]); return 0; }
        if (strcmp(argv[i], "-b") == 0 && i + 1 < argc) { broker = argv[++i]; continue; }
        if (strcmp(argv[i], "-d") == 0 && i + 1 < argc) { device_id = argv[++i]; continue; }
        if (strcmp(argv[i], "-i") == 0 && i + 1 < argc) { interval = atoi(argv[++i]); if (interval < 1) interval = 1; continue; }
    }
#endif

    printf("FactMesh Edge Box (C) - broker=%s device=%s interval=%ds\n", broker, device_id, interval);
    {
        int ret = run(broker, device_id, interval);
#ifdef _WIN32
        WSACleanup();
#endif
        return ret;
    }
}
