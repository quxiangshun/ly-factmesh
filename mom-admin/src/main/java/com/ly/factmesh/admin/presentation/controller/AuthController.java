package com.ly.factmesh.admin.presentation.controller;

import com.ly.factmesh.admin.application.dto.LoginRequest;
import com.ly.factmesh.admin.application.dto.LoginResponse;
import com.ly.factmesh.admin.application.service.AuthService;
import com.ly.factmesh.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 认证控制器
 */
@RestController
@RequestMapping("/api/auth")
@Tag(name = "认证", description = "登录、登出")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    @Operation(summary = "登录", description = "用户名密码登录，返回 JWT")
    public ResponseEntity<Result<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return ResponseEntity.ok(Result.ok(response));
    }

    @GetMapping("/me")
    @Operation(summary = "获取当前用户", description = "根据 JWT 获取当前登录用户信息")
    public ResponseEntity<Result<com.ly.factmesh.admin.application.dto.UserDTO>> getCurrentUser(
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        com.ly.factmesh.admin.application.dto.UserDTO user = authService.getCurrentUser(authorization);
        return ResponseEntity.ok(Result.ok(user));
    }
}
