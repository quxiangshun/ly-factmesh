package com.ly.factmesh.gateway.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest
@AutoConfigureWebTestClient
@TestPropertySource(properties = {
        "spring.cloud.nacos.discovery.enabled=false",
        "spring.cloud.nacos.config.enabled=false"
})
class TestControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @Test
    void testHello() {
        webTestClient.get().uri("/api/hello")
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class).isEqualTo("Hello, World! This is Gateway Service.");
    }

    @Test
    void testHelloWithName() {
        webTestClient.get().uri(uriBuilder -> uriBuilder.path("/api/hello").queryParam("name", "Test").build())
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class).isEqualTo("Hello, Test! This is Gateway Service.");
    }
}