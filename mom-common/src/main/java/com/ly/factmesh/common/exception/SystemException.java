package com.ly.factmesh.common.exception;

/**
 * 系统异常，用于非预期的技术性错误（如数据库异常、外部调用失败等）
 *
 * @author LY-FactMesh
 */
public class SystemException extends RuntimeException {

    /** 错误码，默认 500 */
    private final int code;

    public SystemException(String message) {
        super(message);
        this.code = 500;
    }

    public SystemException(int code, String message) {
        super(message);
        this.code = code;
    }

    public SystemException(String message, Throwable cause) {
        super(message, cause);
        this.code = 500;
    }

    public int getCode() {
        return code;
    }
}
