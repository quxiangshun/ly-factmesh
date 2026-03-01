package com.ly.factmesh.iot.domain.event;

import java.time.LocalDateTime;

/**
 * 领域事件抽象类
 * 定义所有领域事件的基础结构
 *
 * @author 屈想顺
 */
public abstract class DomainEvent {
    
    /** 事件ID */
    private final String eventId;
    
    /** 事件发生时间 */
    private final LocalDateTime occurredOn;
    
    /** 事件类型 */
    private final String eventType;
    
    /**
     * 构造函数
     * @param eventId 事件ID
     * @param eventType 事件类型
     */
    protected DomainEvent(String eventId, String eventType) {
        this.eventId = eventId;
        this.eventType = eventType;
        this.occurredOn = LocalDateTime.now();
    }
    
    /**
     * 获取事件ID
     * @return 事件ID
     */
    public String getEventId() {
        return eventId;
    }
    
    /**
     * 获取事件发生时间
     * @return 事件发生时间
     */
    public LocalDateTime getOccurredOn() {
        return occurredOn;
    }
    
    /**
     * 获取事件类型
     * @return 事件类型
     */
    public String getEventType() {
        return eventType;
    }
}