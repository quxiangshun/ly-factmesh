package com.ly.factmesh.common.utils;

import java.util.concurrent.atomic.AtomicLong;

/**
 * 雪花ID生成器
 * 结构：1位符号位 + 41位时间戳 + 10位工作机器ID + 12位序列号
 * 优点：全局唯一、含时间戳、可排序、无主键冲突
 * 适用：分布式部署、高并发写入、跨库关联
 *
 * @author 屈想顺
 */
public class SnowflakeIdGenerator {
    
    /** 开始时间戳 (2026-01-01) */
    private static final long START_TIMESTAMP = 1777536000000L;
    
    /** 机器ID位数 */
    private static final long MACHINE_ID_BITS = 5L;
    
    /** 数据中心ID位数 */
    private static final long DATA_CENTER_ID_BITS = 5L;
    
    /** 序列号位数 */
    private static final long SEQUENCE_BITS = 12L;
    
    /** 机器ID最大值 */
    private static final long MAX_MACHINE_ID = ~(-1L << MACHINE_ID_BITS);
    
    /** 数据中心ID最大值 */
    private static final long MAX_DATA_CENTER_ID = ~(-1L << DATA_CENTER_ID_BITS);
    
    /** 序列号最大值 */
    private static final long MAX_SEQUENCE = ~(-1L << SEQUENCE_BITS);
    
    /** 机器ID左移位数 */
    private static final long MACHINE_ID_SHIFT = SEQUENCE_BITS;
    
    /** 数据中心ID左移位数 */
    private static final long DATA_CENTER_ID_SHIFT = SEQUENCE_BITS + MACHINE_ID_BITS;
    
    /** 时间戳左移位数 */
    private static final long TIMESTAMP_LEFT_SHIFT = SEQUENCE_BITS + MACHINE_ID_BITS + DATA_CENTER_ID_BITS;
    
    /** 数据中心ID */
    private final long dataCenterId;
    
    /** 机器ID */
    private final long machineId;
    
    /** 序列号 */
    private final AtomicLong sequence = new AtomicLong(0L);
    
    /** 上一次生成ID的时间戳 */
    private volatile long lastTimestamp = -1L;
    
    /** 单例实例 */
    private static volatile SnowflakeIdGenerator instance;
    
    /**
     * 私有构造函数
     */
    private SnowflakeIdGenerator(long dataCenterId, long machineId) {
        if (dataCenterId > MAX_DATA_CENTER_ID || dataCenterId < 0) {
            throw new IllegalArgumentException(String.format("数据中心ID必须在0到%d之间", MAX_DATA_CENTER_ID));
        }
        if (machineId > MAX_MACHINE_ID || machineId < 0) {
            throw new IllegalArgumentException(String.format("机器ID必须在0到%d之间", MAX_MACHINE_ID));
        }
        this.dataCenterId = dataCenterId;
        this.machineId = machineId;
    }
    
    /**
     * 获取单例实例
     * 默认使用数据中心ID=1，机器ID=1
     */
    public static SnowflakeIdGenerator getInstance() {
        if (instance == null) {
            synchronized (SnowflakeIdGenerator.class) {
                if (instance == null) {
                    instance = new SnowflakeIdGenerator(1, 1);
                }
            }
        }
        return instance;
    }
    
    /**
     * 获取单例实例
     * 
     * @param dataCenterId 数据中心ID (0-31)
     * @param machineId 机器ID (0-31)
     */
    public static SnowflakeIdGenerator getInstance(long dataCenterId, long machineId) {
        if (instance == null) {
            synchronized (SnowflakeIdGenerator.class) {
                if (instance == null) {
                    instance = new SnowflakeIdGenerator(dataCenterId, machineId);
                }
            }
        }
        return instance;
    }
    
    /**
     * 静态便捷方法：生成下一个ID
     *
     * @return 雪花ID
     */
    public static long generateId() {
        return getInstance().nextId();
    }

    /**
     * 生成下一个ID
     * 
     * @return 雪花ID
     */
    public synchronized long nextId() {
        long currentTimestamp = System.currentTimeMillis();
        
        // 如果当前时间小于上一次生成ID的时间，说明系统时钟回退，抛出异常
        if (currentTimestamp < lastTimestamp) {
            throw new RuntimeException(String.format("系统时钟回退，拒绝生成ID，差值：%d毫秒", lastTimestamp - currentTimestamp));
        }
        
        // 如果是同一时间生成的，则递增序列号
        if (currentTimestamp == lastTimestamp) {
            sequence.compareAndSet(MAX_SEQUENCE, 0);
            if (sequence.incrementAndGet() >= MAX_SEQUENCE) {
                // 序列号达到最大值，等待下一毫秒
                currentTimestamp = tilNextMillis(lastTimestamp);
                sequence.set(0L);
            }
        } else {
            // 不同时间戳，序列号重置为0
            sequence.set(0L);
        }
        
        // 保存当前时间戳
        lastTimestamp = currentTimestamp;
        
        // 生成ID
        return (
            ((currentTimestamp - START_TIMESTAMP) << TIMESTAMP_LEFT_SHIFT) |
            (dataCenterId << DATA_CENTER_ID_SHIFT) |
            (machineId << MACHINE_ID_SHIFT) |
            sequence.get()
        );
    }
    
    /**
     * 等待直到下一毫秒
     * 
     * @param lastTimestamp 上一次生成ID的时间戳
     * @return 新的时间戳
     */
    private long tilNextMillis(long lastTimestamp) {
        long timestamp = System.currentTimeMillis();
        while (timestamp <= lastTimestamp) {
            timestamp = System.currentTimeMillis();
        }
        return timestamp;
    }
    
    /**
     * 生成字符串形式的雪花ID
     * 
     * @return 雪花ID字符串
     */
    public String nextIdString() {
        return String.valueOf(nextId());
    }
}
