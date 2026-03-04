package com.ly.factmesh.iot.infrastructure.protocol.exception;

/**
 * 工业协议连接与读写异常
 *
 * @author LY-FactMesh
 */
public class IndustrialConnException extends RuntimeException {

    public IndustrialConnException(String message) {
        super(message);
    }

    public IndustrialConnException(String message, Throwable cause) {
        super(message, cause);
    }

    public IndustrialConnException(Throwable cause) {
        super(cause);
    }
}
