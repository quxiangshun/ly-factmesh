package com.ly.factmesh.admin.application.service;

import com.ly.factmesh.admin.application.dto.LoginRequest;
import com.ly.factmesh.admin.application.dto.LoginResponse;
import com.ly.factmesh.admin.application.dto.UserDTO;
import com.ly.factmesh.admin.config.JwtUtil;
import com.ly.factmesh.admin.domain.entity.User;
import com.ly.factmesh.admin.domain.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * 认证服务
 */
@Service
public class AuthService {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public AuthService(UserRepository userRepository, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
    }

    /**
     * 登录：校验用户名密码，返回 JWT
     */
    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("用户名或密码错误"));

        if (user.getStatus() != null && user.getStatus() == 0) {
            throw new RuntimeException("账号已禁用");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("用户名或密码错误");
        }

        String token = jwtUtil.generateToken(user.getId(), user.getUsername());
        return new LoginResponse(token, user.getId(), user.getUsername(), user.getNickname());
    }

    /**
     * 根据 Token 获取当前用户信息
     */
    public UserDTO getCurrentUser(String bearerToken) {
        if (bearerToken == null || !bearerToken.startsWith("Bearer ")) {
            throw new RuntimeException("未提供有效令牌");
        }
        String token = bearerToken.substring(7);
        Long userId = jwtUtil.getUserId(token);
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("用户不存在"));
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setRealName(user.getNickname());
        dto.setEmail(user.getEmail());
        dto.setPhone(user.getPhone());
        dto.setStatus(user.getStatus());
        dto.setCreatedAt(user.getCreatedAt());
        dto.setUpdatedAt(user.getUpdatedAt());
        return dto;
    }
}
