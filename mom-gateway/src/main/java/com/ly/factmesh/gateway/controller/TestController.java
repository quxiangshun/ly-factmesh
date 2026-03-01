package com.ly.factmesh.gateway.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 测试控制器
 */
@RestController
public class TestController {
    
    /**
     * 测试接口，演示网关路由和工具类的使用
     * @param name 请求参数
     * @return 响应消息
     */
    @GetMapping("/api/hello")
    public String hello(@RequestParam(required = false) String name) {
        if (name == null || name.trim().isEmpty()) {
            name = "World";
        }
        return "Hello, " + name + "! This is Gateway Service.";
    }
    
    /**
     * 测试接口，演示路径参数
     * @param id 路径参数
     * @return 响应消息
     */
    @GetMapping("/api/users/{id}")
    public String getUser(@PathVariable Long id) {
        return "User ID: " + id + " (from Gateway Service)";
    }
    
    /**
     * 测试健康检查接口
     * @return 健康状态
     */
    @GetMapping("/api/health")
    public String health() {
        return "Gateway Service is healthy!";
    }
}