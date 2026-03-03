package com.ly.factmesh.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum RequisitionStatusEnum {

    DRAFT(0, "\u8349\u7a3f"),
    SUBMITTED(1, "\u5df2\u63d0\u4ea4"),
    DONE(2, "\u5df2\u5b8c\u6210"),
    CANCELLED(3, "\u5df2\u53d6\u6d88");

    private final int code;
    private final String desc;

    public static RequisitionStatusEnum of(int code) {
        for (RequisitionStatusEnum e : values()) {
            if (e.code == code) return e;
        }
        return null;
    }
}
