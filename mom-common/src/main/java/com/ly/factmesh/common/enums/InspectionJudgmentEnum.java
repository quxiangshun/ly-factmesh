package com.ly.factmesh.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum InspectionJudgmentEnum {

    PASS(0, "Pass"),
    FAIL(1, "Fail");

    private final int code;
    private final String desc;

    public static InspectionJudgmentEnum of(int code) {
        for (InspectionJudgmentEnum e : values()) {
            if (e.code == code) return e;
        }
        return null;
    }
}
