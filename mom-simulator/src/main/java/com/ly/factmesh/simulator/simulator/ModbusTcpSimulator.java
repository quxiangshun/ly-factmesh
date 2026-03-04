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
 * Modbus TCP 模拟生产端（内存模拟）
 * <p>支持多设备，点位格式：{slaveId}_3_200（压力）、{slaveId}_3_201（电压），slaveId=1..N</p>
 * <p>更新间隔、设备数量由界面配置，未配置默认 5000ms、1 台</p>
 *
 * @author LY-FactMesh
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "simulator.modbus", name = "enabled", havingValue = "true")
public class ModbusTcpSimulator {

    private final SimulatorRuntimeConfig runtimeConfig;
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private final ConcurrentHashMap<String, Integer> values = new ConcurrentHashMap<>();
    private final AtomicBoolean running = new AtomicBoolean(true);
    private volatile Future<?> taskFuture;

    @PostConstruct
    public void start() {
        initDeviceValues(runtimeConfig.getDeviceCount());
        taskFuture = scheduler.submit(this::runLoop);
        log.info("Modbus TCP 模拟生产端启动（内存模式），更新间隔 {} ms，设备数 {}。", runtimeConfig.getIntervalMs(), runtimeConfig.getDeviceCount());
    }

    private void runLoop() {
        while (running.get()) {
            try {
                updateRegisters();
                Thread.sleep(runtimeConfig.getIntervalMs());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                log.debug("Modbus 更新异常", e);
            }
        }
    }

    private void initDeviceValues(int count) {
        values.clear();
        for (int i = 1; i <= count; i++) {
            values.put(pointKey(i, 200), 50);
            values.put(pointKey(i, 201), 220);
        }
    }

    private void updateRegisters() {
        int count = runtimeConfig.getDeviceCount();
        for (int i = 1; i <= count; i++) {
            values.put(pointKey(i, 200), 30 + (int) (Math.random() * 70));
            values.put(pointKey(i, 201), 210 + (int) (Math.random() * 20));
        }
        if (values.size() / 2 != count) {
            initDeviceValues(count);
        }
    }

    private static String pointKey(int slaveId, int addr) {
        return slaveId + "_3_" + addr;
    }

    @PreDestroy
    public void stop() {
        running.set(false);
        if (taskFuture != null) {
            taskFuture.cancel(true);
        }
        scheduler.shutdown();
    }

    public Map<String, Integer> getAllValues() {
        return new HashMap<>(values);
    }

    public boolean isRunning() {
        return running.get();
    }
}
