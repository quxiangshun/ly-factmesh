package com.ly.factmesh.simulator.config;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 模拟器运行时配置（界面可动态修改，未设置时使用默认值）
 *
 * @author LY-FactMesh
 */
@Component
public class SimulatorRuntimeConfig {

    /** 点位更新间隔（毫秒），默认 5000 */
    private final AtomicInteger intervalMs = new AtomicInteger(5000);

    /** 模拟设备数量，由 deviceIds 推导或直接设置，默认 1 */
    private final AtomicInteger deviceCount = new AtomicInteger(1);

    /** 选中的设备 ID 列表，为空时 fallback 到 deviceCount */
    private final List<String> deviceIds = new CopyOnWriteArrayList<>();

    public int getIntervalMs() {
        return intervalMs.get();
    }

    public void setIntervalMs(int ms) {
        intervalMs.set(Math.max(100, Math.min(300000, ms)));
    }

    public int getDeviceCount() {
        if (!deviceIds.isEmpty()) {
            return deviceIds.size();
        }
        return deviceCount.get();
    }

    public void setDeviceCount(int count) {
        deviceCount.set(Math.max(1, Math.min(1000, count)));
    }

    public List<String> getDeviceIds() {
        return new ArrayList<>(deviceIds);
    }

    public void setDeviceIds(List<String> ids) {
        deviceIds.clear();
        if (ids != null && !ids.isEmpty()) {
            deviceIds.addAll(ids);
        } else {
            deviceCount.set(1);
        }
    }
}
