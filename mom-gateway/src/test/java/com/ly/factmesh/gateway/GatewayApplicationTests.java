package com.ly.factmesh.gateway;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.http.ResponseEntity;
import static org.junit.jupiter.api.Assertions.*;

/**
 * 网关应用集成测试
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
@TestPropertySource(properties = {
        "spring.cloud.nacos.discovery.enabled=false",
        "spring.cloud.nacos.config.enabled=false"
})
class GatewayApplicationTests {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void testGatewayHelloApi() {
        ResponseEntity<String> response = restTemplate.getForEntity("/api/hello", String.class);
        assertEquals(200, response.getStatusCode().value());
        assertTrue(response.getBody().contains("Hello, World!"));
        assertTrue(response.getBody().contains("Gateway Service"));
    }

    @Test
    void testGatewayHelloApiWithName() {
        ResponseEntity<String> response = restTemplate.getForEntity("/api/hello?name=Test", String.class);
        assertEquals(200, response.getStatusCode().value());
        assertTrue(response.getBody().contains("Hello, Test!"));
    }

    @Test
    void testGatewayUserApi() {
        ResponseEntity<String> response = restTemplate.getForEntity("/api/users/123", String.class);
        assertEquals(200, response.getStatusCode().value());
        assertTrue(response.getBody().contains("User ID: 123"));
    }

    @Test
    void testGatewayHealthApi() {
        ResponseEntity<String> response = restTemplate.getForEntity("/api/health", String.class);
        assertEquals(200, response.getStatusCode().value());
        assertTrue(response.getBody().contains("Gateway Service is healthy!"));
    }
}