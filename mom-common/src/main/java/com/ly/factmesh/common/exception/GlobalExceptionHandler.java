package com.ly.factmesh.common.exception;

import com.ly.factmesh.common.result.Result;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

/**
 * 全局异常处理器
 * <p>
 * 统一返回 {@link com.ly.factmesh.common.result.Result} 格式。
 * 业务异常 400；校验异常（@Valid、@Validated）400；认证类错误 401；系统异常 500。
 * </p>
 *
 * @author LY-FactMesh
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /** 业务异常，直接返回错误码与消息 */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Result<Object>> handleBusiness(BusinessException e) {
        return ResponseEntity.badRequest()
                .body(Result.fail(e.getCode(), e.getMessage()));
    }

    /** 系统异常，记录日志并返回 500 */
    @ExceptionHandler(SystemException.class)
    public ResponseEntity<Result<Object>> handleSystem(SystemException e) {
        log.error("System exception", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Result.fail(e.getCode(), e.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Result<Object>> handleValidation(MethodArgumentNotValidException e) {
        String msg = e.getBindingResult().getFieldErrors().stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .collect(Collectors.joining("; "));
        return ResponseEntity.badRequest().body(Result.fail(400, msg));
    }

    /** 方法参数校验失败（@Validated） */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Result<Object>> handleConstraint(ConstraintViolationException e) {
        String msg = e.getConstraintViolations().stream()
                .map(cv -> cv.getPropertyPath() + ": " + cv.getMessage())
                .collect(Collectors.joining("; "));
        return ResponseEntity.badRequest().body(Result.fail(400, msg));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Result<Object>> handleIllegal(IllegalArgumentException e) {
        String msg = e.getMessage() != null ? e.getMessage() : "参数错误";
        if (isAuthError(msg)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Result.fail(401, msg));
        }
        return ResponseEntity.badRequest().body(Result.fail(400, msg));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Result<Object>> handleRuntime(RuntimeException e) {
        String msg = e.getMessage() != null ? e.getMessage() : "操作失败";
        if (isAuthError(msg)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Result.fail(401, msg));
        }
        return ResponseEntity.badRequest().body(Result.fail(400, msg));
    }

    /** 认证相关错误（如登录失败、账号禁用）返回 401 */
    protected boolean isAuthError(String msg) {
        return msg != null && (msg.contains("用户名或密码") || msg.contains("账号已禁用")
                || msg.contains("用户不存在") || msg.contains("刷新令牌"));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Result<Object>> handleOther(Exception e) {
        log.error("Unhandled exception", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Result.fail(500, e.getMessage() != null ? e.getMessage() : "服务器内部错误"));
    }
}
