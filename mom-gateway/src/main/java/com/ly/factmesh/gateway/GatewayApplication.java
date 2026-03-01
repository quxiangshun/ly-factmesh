package com.ly.factmesh.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.CommandLineRunner;
import org.springframework.cloud.gateway.discovery.DiscoveryClientRouteDefinitionLocator;
import org.springframework.cloud.gateway.discovery.DiscoveryLocatorProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.discovery.ReactiveDiscoveryClient;

/**
 * @author 屈想顺
 */
@SpringBootApplication
public class GatewayApplication {

    @Bean
    public DiscoveryClientRouteDefinitionLocator discoveryClientRouteDefinitionLocator(
            ReactiveDiscoveryClient discoveryClient,
            DiscoveryLocatorProperties properties) {
        return new DiscoveryClientRouteDefinitionLocator(discoveryClient, properties);
    }
    
    @Value("${server.port}")
    private String serverPort;
    
    public static void main(String[] args) {
        SpringApplication.run(GatewayApplication.class, args);
    }
    
    @Bean
    public CommandLineRunner commandLineRunner() {
        return args -> {
            System.out.println("\n=== Server Configuration ===");
            System.out.println("Server Port: " + serverPort);
            System.out.println("Server URL: http://localhost:" + serverPort);
            System.out.println("API Endpoint: http://localhost:" + serverPort + "/api/hello");
            System.out.println("Gateway Routes: http://localhost:" + serverPort + "/admin/** -> http://localhost:8081");
            System.out.println("============================\n");
        };
    }
}