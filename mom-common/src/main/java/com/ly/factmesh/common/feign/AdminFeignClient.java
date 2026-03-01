package com.ly.factmesh.common.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * 调用admin服务的Feign客户端
 *
 * @author 屈想顺
 */
@FeignClient(name = "mom-admin", contextId = "adminFeignClient")
public interface AdminFeignClient {

    /**
     * 检查用户是否存在
     * @param userId 用户ID
     * @return 是否存在
     */
    @GetMapping("/api/users/{userId}/exists")
    Boolean checkUserExists(@PathVariable("userId") Long userId);

    /**
     * 获取用户信息
     * @param userId 用户ID
     * @return 用户信息
     */
    @GetMapping("/api/users/{userId}")
    UserInfoDTO getUserInfo(@PathVariable("userId") Long userId);

    /**
     * 用户信息DTO
     */
    class UserInfoDTO {
        private Long id;
        private String username;
        private String nickname;
        private String email;
        private Integer status;

        // Getters and Setters
        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getNickname() {
            return nickname;
        }

        public void setNickname(String nickname) {
            this.nickname = nickname;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public Integer getStatus() {
            return status;
        }

        public void setStatus(Integer status) {
            this.status = status;
        }
    }
}