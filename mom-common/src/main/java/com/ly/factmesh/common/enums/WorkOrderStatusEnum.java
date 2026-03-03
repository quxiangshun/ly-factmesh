package com.ly.factmesh.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 工单状态枚举
 *
 * @author LY-FactMesh
 */
@Getter
@AllArgsConstructor
public enum WorkOrderStatusEnum {

    DRAFT(0, "草稿"),
    RELEASED(1, "已下发"),
    IN_PROGRESS(2, "进行中"),
    COMPLETED(3, "已完成"),
    CLOSED(4, "已关闭"),
    PAUSED(5, "暂停");

    /** 状态码，存库用 */
    private final int code;
    /** 状态描述 */
    private final String desc;

    /** 按状态码获取枚举 */
    public static WorkOrderStatusEnum of(int code) {
        for (WorkOrderStatusEnum e : values()) {
            if (e.code == code) return e;
        }
        return null;
    }
}
