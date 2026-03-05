package com.ly.factmesh.edgebox;

import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

/**
 * FactMesh 边缘盒子（Java）- 边缘端数据采集与上报
 *
 * 运行：java -jar factmesh-edge-box.jar [-b broker] [-d device_id] [-i interval_sec]
 */
public class FactMeshEdgeBox {

    private static final String TOPIC_PREFIX = "factmesh/input/device/";
    private static final String DEFAULT_BROKER = "tcp://127.0.0.1:1883";
    private static final String DEFAULT_DEVICE_ID = "edge-box-01";
    private static final int DEFAULT_INTERVAL = 5;
    private static final int QOS = 1;

    private static volatile boolean running = true;

    public static void main(String[] args) {
        String broker = DEFAULT_BROKER;
        String deviceId = DEFAULT_DEVICE_ID;
        int interval = DEFAULT_INTERVAL;

        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "-b" -> { if (i + 1 < args.length) broker = args[++i]; }
                case "-d" -> { if (i + 1 < args.length) deviceId = args[++i]; }
                case "-i" -> {
                    if (i + 1 < args.length) {
                        interval = Math.max(1, Integer.parseInt(args[++i]));
                    }
                }
            }
        }

        System.out.printf("FactMesh Edge Box (Java) - broker=%s device=%s interval=%ds%n", broker, deviceId, interval);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> running = false));

        run(broker, deviceId, interval);
    }

    private static void run(String broker, String deviceId, int intervalSec) {
        String clientId = "factmesh-edge-java-" + deviceId;
        String topic = TOPIC_PREFIX + deviceId;

        try {
            MqttClient client = new MqttClient(broker, clientId, new MemoryPersistence());
            MqttConnectOptions opts = new MqttConnectOptions();
            opts.setCleanSession(false);
            opts.setKeepAliveInterval(30);

            client.connect(opts);
            System.out.printf("Connected to %s, publishing to %s every %ds%n", broker, topic, intervalSec);

            while (running) {
                String payload = buildPayload(deviceId);
                MqttMessage msg = new MqttMessage(payload.getBytes(StandardCharsets.UTF_8));
                msg.setQos(QOS);

                client.publish(topic, msg);
                System.out.printf("[%d] Published %d bytes to %s%n", System.currentTimeMillis() / 1000, payload.length(), topic);

                for (int i = 0; i < intervalSec && running; i++) {
                    TimeUnit.SECONDS.sleep(1);
                }
            }

            client.disconnect(1000);
            client.close();
        } catch (MqttException e) {
            System.err.println("MQTT error: " + e.getMessage());
            System.exit(1);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private static String buildPayload(String deviceId) {
        long now = System.currentTimeMillis() / 1000;
        double temp = 20.0 + (now % 150) / 10.0;
        double hum = 40.0 + (now % 400) / 10.0;
        return String.format("{\"deviceId\":\"%s\",\"timestamp\":%d,\"temperature\":%.1f,\"humidity\":%.1f}",
                deviceId, now, temp, hum);
    }
}
