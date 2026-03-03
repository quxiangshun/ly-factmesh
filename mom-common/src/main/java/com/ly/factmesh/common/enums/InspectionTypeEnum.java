package com.ly.factmesh.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 质检类型枚举
 *
 * @author LY-FactMesh
 */
@Getter
@AllArgsConstructor
public enum InspectionTypeEnum {

    INCOMING(0, "来料检验"),
    PROCESS(1, "过程检验"),
    FINAL(2, "成品检验"),
    OTHER(9, "其他");

    private final int code;
    private final String desc;

    public static InspectionTypeEnum of(int code) {
        for (InspectionTypeEnum e : values()) {
            if (e.code == code) return e;
        }
        return null;
    }
}
