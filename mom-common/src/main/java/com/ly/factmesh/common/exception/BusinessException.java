package com.ly.factmesh.common.exception;

/**
 * 业务异常，用于可预期的业务规则校验失败（如重复、状态不允许等）
 *
 * @author LY-FactMesh
 */
public class BusinessException extends RuntimeException {

    private final int code;

    public BusinessException(String message) {
        super(message);
        this.code = -1;
    }

    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
