package com.ly.factmesh.admin.presentation.controller;

import com.ly.factmesh.admin.infrastructure.database.entity.RedisConnectionEntity;
import com.ly.factmesh.admin.infrastructure.database.mapper.RedisConnectionMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Redis 连接管理与命令执行（运维管理）
 *
 * @author 屈想顺
 */
@RestController
@RequestMapping("/api/redis")
@RequiredArgsConstructor
@Tag(name = "Redis管理", description = "Redis 连接配置与命令执行")
public class RedisController {

    private final RedisConnectionMapper redisConnectionMapper;

    @GetMapping("/connections")
    @Operation(summary = "连接列表", description = "获取所有已保存的 Redis 连接，不分页")
    public ResponseEntity<List<Map<String, Object>>> listConnections() {
        List<RedisConnectionEntity> list = redisConnectionMapper.selectList(null);
        List<Map<String, Object>> result = list.stream().map(this::toMap).collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }

    @PostMapping("/connections")
    @Operation(summary = "新增连接")
    public ResponseEntity<Map<String, Object>> createConnection(@RequestBody Map<String, Object> body) {
        RedisConnectionEntity e = fromMap(body);
        if (e.getName() == null || e.getName().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "名称不能为空"));
        }
        if (e.getHost() == null || e.getHost().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "主机不能为空"));
        }
        e.setPort(e.getPort() != null ? e.getPort() : 6379);
        e.setDatabase(e.getDatabase() != null ? e.getDatabase() : 0);
        redisConnectionMapper.insert(e);
        return ResponseEntity.ok(toMap(e));
    }

    @PutMapping("/connections/{id}")
    @Operation(summary = "更新连接")
    public ResponseEntity<Map<String, Object>> updateConnection(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        RedisConnectionEntity existing = redisConnectionMapper.selectById(id);
        if (existing == null) {
            return ResponseEntity.notFound().build();
        }
        RedisConnectionEntity e = fromMap(body);
        e.setId(id);
        if (e.getName() != null && !e.getName().isBlank()) existing.setName(e.getName());
        if (e.getHost() != null && !e.getHost().isBlank()) existing.setHost(e.getHost());
        if (e.getPort() != null) existing.setPort(e.getPort());
        if (e.getPassword() != null) existing.setPassword(e.getPassword());
        if (e.getDatabase() != null) existing.setDatabase(e.getDatabase());
        redisConnectionMapper.updateById(existing);
        return ResponseEntity.ok(toMap(existing));
    }

    @DeleteMapping("/connections/{id}")
    @Operation(summary = "删除连接")
    public ResponseEntity<Void> deleteConnection(@PathVariable Long id) {
        if (redisConnectionMapper.selectById(id) == null) {
            return ResponseEntity.notFound().build();
        }
        redisConnectionMapper.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/execute")
    @Operation(summary = "执行命令", description = "传入 connectionId 或 host/port/password/database，以及 cmd 如 GET key")
    public ResponseEntity<Map<String, Object>> execute(@RequestBody Map<String, Object> body) {
        String host;
        int port;
        String password;
        int database;
        Long connectionId = body.get("connectionId") != null ? Long.valueOf(body.get("connectionId").toString()) : null;
        if (connectionId != null) {
            RedisConnectionEntity conn = redisConnectionMapper.selectById(connectionId);
            if (conn == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "连接不存在"));
            }
            host = conn.getHost();
            port = conn.getPort() != null ? conn.getPort() : 6379;
            password = conn.getPassword();
            database = conn.getDatabase() != null ? conn.getDatabase() : 0;
        } else {
            host = getStr(body, "host");
            String portStr = getStr(body, "port");
            port = portStr != null && !portStr.isBlank() ? Integer.parseInt(portStr) : 6379;
            password = getStr(body, "password");
            String dbStr = getStr(body, "database");
            database = dbStr != null && !dbStr.isBlank() ? Integer.parseInt(dbStr) : 0;
            if (host == null || host.isBlank()) {
                return ResponseEntity.badRequest().body(Map.of("error", "请填写主机或选择已保存连接"));
            }
        }
        String cmd = getStr(body, "cmd");
        if (cmd == null || cmd.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "命令不能为空"));
        }
        String[] parts = cmd.trim().split("\\s+");
        if (parts.length == 0) {
            return ResponseEntity.badRequest().body(Map.of("error", "命令不能为空"));
        }
        String command = parts[0].toUpperCase();
        String[] args = parts.length > 1 ? Arrays.copyOfRange(parts, 1, parts.length) : new String[0];
        try {
            Object result = executeRedis(host, port, password, database, command, args);
            return ResponseEntity.ok(Map.of("result", result != null ? result : "OK"));
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage() != null ? ex.getMessage() : "执行失败"));
        }
    }

    private Object executeRedis(String host, int port, String password, int database, String command, String[] args) {
        try (redis.clients.jedis.Jedis jedis = new redis.clients.jedis.Jedis(host, port)) {
            if (password != null && !password.isBlank()) {
                jedis.auth(password);
            }
            if (database > 0) {
                jedis.select(database);
            }
            redis.clients.jedis.Protocol.Command cmd;
            try {
                cmd = redis.clients.jedis.Protocol.Command.valueOf(command);
            } catch (IllegalArgumentException ex) {
                throw new IllegalArgumentException("不支持的 Redis 命令: " + command);
            }
            Object raw = jedis.sendBlockingCommand(cmd, args);
            return formatRedisResponse(raw);
        }
    }

    private static Object formatRedisResponse(Object raw) {
        if (raw == null) return null;
        if (raw instanceof byte[]) return new String((byte[]) raw);
        if (raw instanceof List) {
            List<?> list = (List<?>) raw;
            return list.stream().map(item -> item instanceof byte[] ? new String((byte[]) item) : item).toList();
        }
        if (raw instanceof Set) {
            return ((Set<?>) raw).stream().map(item -> item instanceof byte[] ? new String((byte[]) item) : item).toList();
        }
        return raw;
    }

    private Map<String, Object> toMap(RedisConnectionEntity e) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", e.getId());
        m.put("name", e.getName());
        m.put("host", e.getHost());
        m.put("port", e.getPort());
        m.put("password", e.getPassword() != null ? "****" : null);
        m.put("database", e.getDatabase());
        m.put("createTime", e.getCreateTime());
        m.put("updateTime", e.getUpdateTime());
        return m;
    }

    private RedisConnectionEntity fromMap(Map<String, Object> m) {
        RedisConnectionEntity e = new RedisConnectionEntity();
        if (m.get("name") != null) e.setName(m.get("name").toString());
        if (m.get("host") != null) e.setHost(m.get("host").toString());
        if (m.get("port") != null) e.setPort(Integer.valueOf(m.get("port").toString()));
        if (m.get("password") != null) e.setPassword(m.get("password").toString());
        if (m.get("database") != null) e.setDatabase(Integer.valueOf(m.get("database").toString()));
        return e;
    }

    private static String getStr(Map<String, Object> m, String key) {
        Object v = m != null ? m.get(key) : null;
        return v != null ? v.toString().trim() : null;
    }
}
