package com.ly.factmesh.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum TenantStatusEnum {

    DISABLED(0, "\u7981\u7528"),
    ENABLED(1, "\u542f\u7528");

    private final int code;
    private final String desc;

    public static TenantStatusEnum of(int code) {
        for (TenantStatusEnum e : values()) {
            if (e.code == code) return e;
        }
        return null;
    }
}
