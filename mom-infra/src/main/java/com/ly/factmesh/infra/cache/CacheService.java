package com.ly.factmesh.infra.cache;

import java.util.concurrent.TimeUnit;

/**
 * 缓存服务接口
 * 业务域通过此接口操作缓存，无需关心底层实现（Redis/Local）
 *
 * @author LY-FactMesh
 */
public interface CacheService {

    /**
     * 设置缓存
     *
     * @param key   键
     * @param value 值
     */
    void set(String key, Object value);

    /**
     * 设置缓存（带过期时间）
     *
     * @param key      键
     * @param value    值
     * @param timeout  过期时间
     * @param timeUnit 时间单位
     */
    void set(String key, Object value, long timeout, TimeUnit timeUnit);

    /**
     * 获取缓存
     *
     * @param key   键
     * @param clazz 值类型
     * @return 值，不存在返回 null
     */
    <T> T get(String key, Class<T> clazz);

    /**
     * 删除缓存
     *
     * @param key 键
     */
    void delete(String key);

    /**
     * 判断键是否存在
     *
     * @param key 键
     * @return 是否存在
     */
    boolean hasKey(String key);

    /**
     * 设置过期时间
     *
     * @param key      键
     * @param timeout  过期时间
     * @param timeUnit 时间单位
     */
    void expire(String key, long timeout, TimeUnit timeUnit);
}
