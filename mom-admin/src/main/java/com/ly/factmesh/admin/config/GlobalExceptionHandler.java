package com.ly.factmesh.admin.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;
import java.util.stream.Collectors;

/**
 * 全局异常处理：认证失败返回 401，校验失败返回 400
 *
 * @author LY-FactMesh
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException e) {
        String msg = e.getBindingResult().getFieldErrors().stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .collect(Collectors.joining("; "));
        return ResponseEntity.badRequest().body(Map.of("code", 400, "message", msg));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgument(IllegalArgumentException e) {
        String msg = e.getMessage() != null ? e.getMessage() : "请求参数错误";
        if (isAuthError(msg)) {
            log.debug("Auth error: {}", msg);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("code", 401, "message", msg));
        }
        return ResponseEntity.badRequest().body(Map.of("code", 400, "message", msg));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> handleRuntime(RuntimeException e) {
        String msg = e.getMessage() != null ? e.getMessage() : "操作失败";
        if (isAuthError(msg)) {
            log.debug("Auth error: {}", msg);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("code", 401, "message", msg));
        }
        return ResponseEntity.badRequest().body(Map.of("code", 400, "message", msg));
    }

    private boolean isAuthError(String msg) {
        return msg.contains("用户名或密码") || msg.contains("账号已禁用") || msg.contains("用户不存在") || msg.contains("刷新令牌");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleOther(Exception e) {
        log.error("Unhandled exception", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("code", 500, "message", e.getMessage() != null ? e.getMessage() : "服务器内部错误"));
    }
}
