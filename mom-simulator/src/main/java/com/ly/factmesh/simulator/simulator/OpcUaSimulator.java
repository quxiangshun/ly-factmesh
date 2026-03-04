package com.ly.factmesh.simulator.simulator;

import com.ly.factmesh.simulator.config.SimulatorRuntimeConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * OPC UA 模拟生产端（内存模拟）
 * <p>支持多设备，点位格式：ns=2;s=Device_{n}_Temperature、ns=2;s=Device_{n}_Humidity</p>
 * <p>更新间隔、设备数量由界面配置，未配置默认 5000ms、1 台</p>
 *
 * @author LY-FactMesh
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "simulator.opcua", name = "enabled", havingValue = "true")
public class OpcUaSimulator {

    private final SimulatorRuntimeConfig runtimeConfig;
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private final ConcurrentHashMap<String, Double> values = new ConcurrentHashMap<>();
    private final AtomicBoolean running = new AtomicBoolean(true);
    private volatile Future<?> taskFuture;

    @PostConstruct
    public void start() {
        initDeviceValues(runtimeConfig.getDeviceCount());
        taskFuture = scheduler.submit(this::runLoop);
        log.info("OPC UA 模拟生产端启动（内存模式），更新间隔 {} ms，设备数 {}。", runtimeConfig.getIntervalMs(), runtimeConfig.getDeviceCount());
    }

    private void runLoop() {
        while (running.get()) {
            try {
                updateValues();
                Thread.sleep(runtimeConfig.getIntervalMs());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                log.debug("OPC UA 更新异常", e);
            }
        }
    }

    private void initDeviceValues(int count) {
        values.clear();
        for (int i = 1; i <= count; i++) {
            values.put(pointKey(i, "Temperature"), 25.0);
            values.put(pointKey(i, "Humidity"), 50.0);
        }
    }

    private void updateValues() {
        int count = runtimeConfig.getDeviceCount();
        for (int i = 1; i <= count; i++) {
            values.put(pointKey(i, "Temperature"), 20 + Math.random() * 10);
            values.put(pointKey(i, "Humidity"), 40 + Math.random() * 20);
        }
        if (values.size() / 2 != count) {
            initDeviceValues(count);
        }
    }

    private static String pointKey(int deviceIdx, String name) {
        return "ns=2;s=Device_" + deviceIdx + "_" + name;
    }

    @PreDestroy
    public void stop() {
        running.set(false);
        if (taskFuture != null) {
            taskFuture.cancel(true);
        }
        scheduler.shutdown();
    }

    public Map<String, Double> getAllValues() {
        return new HashMap<>(values);
    }

    public boolean isRunning() {
        return running.get();
    }
}
