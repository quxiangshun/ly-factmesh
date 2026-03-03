package com.ly.factmesh.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ProductionLineStatusEnum {

    IDLE(0, "\u7a7a\u95f2"),
    RUNNING(1, "\u8fd0\u884c\u4e2d"),
    MAINTENANCE(2, "\u68c0\u4fee\u4e2d");

    private final int code;
    private final String desc;

    public static ProductionLineStatusEnum of(int code) {
        for (ProductionLineStatusEnum e : values()) {
            if (e.code == code) return e;
        }
        return null;
    }
}
