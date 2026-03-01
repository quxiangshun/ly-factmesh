package com.ly.factmesh.common.result;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 统一 API 响应体
 * 格式：{ "code": 0, "message": "ok", "data": { ... } }
 *
 * @author LY-FactMesh
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Result<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 业务码，0 表示成功 */
    private int code;
    /** 提示信息 */
    private String message;
    /** 业务数据 */
    private T data;

    public static <T> Result<T> ok() {
        return new Result<T>(0, "ok", null);
    }

    public static <T> Result<T> ok(T data) {
        return new Result<T>(0, "ok", data);
    }

    public static <T> Result<T> ok(String message, T data) {
        return new Result<T>(0, message, data);
    }

    public static <T> Result<T> fail(int code, String message) {
        return new Result<T>(code, message, null);
    }

    public static <T> Result<T> fail(String message) {
        return fail(-1, message);
    }
}
