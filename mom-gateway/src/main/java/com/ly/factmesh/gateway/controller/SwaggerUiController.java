package com.ly.factmesh.gateway.controller;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * 提供 Swagger UI 静态页面（因 Springdoc 3 与 Spring Boot 4 兼容问题，改用 CDN 版 Swagger UI）
 */
@RestController
public class SwaggerUiController {

    @GetMapping(value = "/swagger-ui.html", produces = MediaType.TEXT_HTML_VALUE)
    public Mono<ResponseEntity<String>> swaggerUi() {
        try {
            Resource resource = new ClassPathResource("static/swagger-ui.html");
            String html = new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            return Mono.just(ResponseEntity.ok().body(html));
        } catch (IOException e) {
            return Mono.just(ResponseEntity.<String>notFound().build());
        }
    }
}
