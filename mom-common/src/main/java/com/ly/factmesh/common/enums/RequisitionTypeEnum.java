package com.ly.factmesh.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum RequisitionTypeEnum {

    REQUISITION(1, "\u9886\u6599"),
    RETURN(2, "\u9000\u6599");

    private final int code;
    private final String desc;

    public static RequisitionTypeEnum of(int code) {
        for (RequisitionTypeEnum e : values()) {
            if (e.code == code) return e;
        }
        return null;
    }
}
