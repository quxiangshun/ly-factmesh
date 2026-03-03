package com.ly.factmesh.common.enums;

public enum DeviceStatusEnum {
    OFFLINE(0, "离线"),
    ONLINE(1, "在线"),
    RUNNING(2, "运行中"),
    FAULT(3, "故障");

    private final int code;
    private final String desc;

    DeviceStatusEnum(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public int getCode() { return code; }
    public String getDesc() { return desc; }

    public static DeviceStatusEnum of(int code) {
        for (DeviceStatusEnum e : values()) {
            if (e.code == code) return e;
        }
        return null;
    }
}