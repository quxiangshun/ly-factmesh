package com.ly.factmesh.admin.presentation.controller;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.InfluxDBClientFactory;
import com.influxdb.client.InfluxDBClientOptions;
import com.influxdb.client.domain.HealthCheck;
import com.ly.factmesh.admin.infrastructure.database.entity.InfluxDbConnectionEntity;
import com.ly.factmesh.admin.infrastructure.database.mapper.InfluxDbConnectionMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * InfluxDB 连接管理与查询执行（运维管理）
 *
 * @author 屈想顺
 */
@RestController
@RequestMapping("/api/influxdb")
@RequiredArgsConstructor
@Tag(name = "InfluxDB管理", description = "InfluxDB 连接配置与 Flux 查询")
public class InfluxDbController {

    private final InfluxDbConnectionMapper influxDbConnectionMapper;

    @GetMapping("/connections")
    @Operation(summary = "连接列表", description = "获取所有已保存的 InfluxDB 连接，不分页")
    public ResponseEntity<List<Map<String, Object>>> listConnections() {
        List<InfluxDbConnectionEntity> list = influxDbConnectionMapper.selectList(null);
        List<Map<String, Object>> result = list.stream().map(this::toMap).collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }

    @PostMapping("/connections")
    @Operation(summary = "新增连接")
    public ResponseEntity<Map<String, Object>> createConnection(@RequestBody Map<String, Object> body) {
        InfluxDbConnectionEntity e = fromMap(body);
        if (e.getName() == null || e.getName().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "名称不能为空"));
        }
        if (e.getUrl() == null || e.getUrl().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "URL 不能为空"));
        }
        influxDbConnectionMapper.insert(e);
        return ResponseEntity.ok(toMap(e));
    }

    @PutMapping("/connections/{id}")
    @Operation(summary = "更新连接")
    public ResponseEntity<Map<String, Object>> updateConnection(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        InfluxDbConnectionEntity existing = influxDbConnectionMapper.selectById(id);
        if (existing == null) {
            return ResponseEntity.notFound().build();
        }
        InfluxDbConnectionEntity e = fromMap(body);
        if (e.getName() != null && !e.getName().isBlank()) existing.setName(e.getName());
        if (e.getUrl() != null && !e.getUrl().isBlank()) existing.setUrl(e.getUrl());
        if (e.getToken() != null) existing.setToken(e.getToken());
        if (e.getOrg() != null) existing.setOrg(e.getOrg());
        influxDbConnectionMapper.updateById(existing);
        return ResponseEntity.ok(toMap(existing));
    }

    @DeleteMapping("/connections/{id}")
    @Operation(summary = "删除连接")
    public ResponseEntity<Void> deleteConnection(@PathVariable Long id) {
        if (influxDbConnectionMapper.selectById(id) == null) {
            return ResponseEntity.notFound().build();
        }
        influxDbConnectionMapper.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/query")
    @Operation(summary = "执行 Flux 查询", description = "传入 connectionId 或 url/token/org，以及 query")
    public ResponseEntity<Map<String, Object>> query(@RequestBody Map<String, Object> body) {
        String url;
        String token;
        String org;
        Long connectionId = body.get("connectionId") != null ? Long.valueOf(body.get("connectionId").toString()) : null;
        if (connectionId != null) {
            InfluxDbConnectionEntity conn = influxDbConnectionMapper.selectById(connectionId);
            if (conn == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "连接不存在"));
            }
            url = conn.getUrl();
            token = conn.getToken();
            org = conn.getOrg();
        } else {
            url = getStr(body, "url");
            token = getStr(body, "token");
            org = getStr(body, "org");
            if (url == null || url.isBlank()) {
                return ResponseEntity.badRequest().body(Map.of("error", "请填写 URL 或选择已保存连接"));
            }
        }
        String query = getStr(body, "query");
        if (query == null || query.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "查询不能为空"));
        }
        String orgParam = org != null && !org.isBlank() ? org : "";
        try {
            List<Map<String, Object>> rows = executeFlux(url, token, orgParam, query);
            return ResponseEntity.ok(Map.of("rows", rows));
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage() != null ? ex.getMessage() : "执行失败"));
        }
    }

    @PostMapping("/ping")
    @Operation(summary = "测试连接")
    public ResponseEntity<Map<String, Object>> ping(@RequestBody Map<String, Object> body) {
        String url;
        String token;
        Long connectionId = body.get("connectionId") != null ? Long.valueOf(body.get("connectionId").toString()) : null;
        if (connectionId != null) {
            InfluxDbConnectionEntity conn = influxDbConnectionMapper.selectById(connectionId);
            if (conn == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "连接不存在"));
            }
            url = conn.getUrl();
            token = conn.getToken();
        } else {
            url = getStr(body, "url");
            token = getStr(body, "token");
            if (url == null || url.isBlank()) {
                return ResponseEntity.badRequest().body(Map.of("error", "请填写 URL 或选择已保存连接"));
            }
        }
        try {
            InfluxDBClient client = createClient(url, token);
            try {
                HealthCheck check = client.health();
                String status = check != null && check.getStatus() != null ? check.getStatus().name() : "unknown";
                String message = check != null && check.getMessage() != null ? check.getMessage() : "OK";
                return ResponseEntity.ok(Map.of("status", status, "message", message));
            } finally {
                client.close();
            }
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage() != null ? ex.getMessage() : "连接失败"));
        }
    }

    private List<Map<String, Object>> executeFlux(String url, String token, String org, String flux) {
        InfluxDBClient client = createClient(url, token);
        try {
            List<Map<String, Object>> rows = new ArrayList<>();
            client.getQueryApi().query(flux, org).forEach(table ->
                table.getRecords().forEach(record -> {
                    Map<String, Object> row = new LinkedHashMap<>();
                    record.getValues().forEach((k, v) -> row.put(k, v));
                    rows.add(row);
                })
            );
            return rows;
        } finally {
            client.close();
        }
    }

    private InfluxDBClient createClient(String url, String token) {
        var builder = InfluxDBClientOptions.builder().url(url);
        if (token != null && !token.isBlank()) {
            builder.authenticateToken(token.toCharArray());
        }
        return InfluxDBClientFactory.create(builder.build());
    }

    private Map<String, Object> toMap(InfluxDbConnectionEntity e) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", e.getId());
        m.put("name", e.getName());
        m.put("url", e.getUrl());
        m.put("token", e.getToken() != null ? "****" : null);
        m.put("org", e.getOrg());
        m.put("createTime", e.getCreateTime());
        m.put("updateTime", e.getUpdateTime());
        return m;
    }

    private InfluxDbConnectionEntity fromMap(Map<String, Object> m) {
        InfluxDbConnectionEntity e = new InfluxDbConnectionEntity();
        if (m.get("name") != null) e.setName(m.get("name").toString());
        if (m.get("url") != null) e.setUrl(m.get("url").toString());
        if (m.get("token") != null) e.setToken(m.get("token").toString());
        if (m.get("org") != null) e.setOrg(m.get("org").toString());
        return e;
    }

    private static String getStr(Map<String, Object> m, String key) {
        Object v = m != null ? m.get(key) : null;
        return v != null ? v.toString().trim() : null;
    }
}
