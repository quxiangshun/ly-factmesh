package com.ly.factmesh.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum InventoryTransactionTypeEnum {

    INBOUND(1, "\u5165\u5e93"),
    OUTBOUND(2, "\u51fa\u5e93"),
    ADJUST(3, "\u8c03\u62e8");

    private final int code;
    private final String desc;

    public static InventoryTransactionTypeEnum of(int code) {
        for (InventoryTransactionTypeEnum e : values()) {
            if (e.code == code) return e;
        }
        return null;
    }
}
