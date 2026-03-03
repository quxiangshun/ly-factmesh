package com.ly.factmesh.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum InspectionTaskStatusEnum {

    DRAFT(0, "\u8349\u7a3f"),
    IN_PROGRESS(1, "\u8fdb\u884c\u4e2d"),
    COMPLETED(2, "\u5df2\u5b8c\u6210"),
    CLOSED(3, "\u5df2\u5173\u95ed");

    private final int code;
    private final String desc;

    public static InspectionTaskStatusEnum of(int code) {
        for (InspectionTaskStatusEnum e : values()) {
            if (e.code == code) return e;
        }
        return null;
    }
}
