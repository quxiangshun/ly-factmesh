package com.ly.factmesh.admin.presentation.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * SQL 执行控制器（仅支持 SELECT，供运维 PG 管理页面使用）
 * 仅使用页面传入的连接参数，不使用项目数据源
 *
 * @author 屈想顺
 */
@RestController
@RequestMapping("/api/sql")
@Tag(name = "SQL执行", description = "执行只读 SQL，供运维 PG 管理使用")
public class SqlController {

    @PostMapping("/execute")
    @Operation(summary = "执行 SQL", description = "仅支持 SELECT；必须传入 host/username/password，database 可选（默认 postgres）")
    public ResponseEntity<Map<String, Object>> execute(@RequestBody Map<String, Object> body) {
        String sql = body != null && body.get("sql") != null ? body.get("sql").toString() : null;
        if (sql == null || sql.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "SQL 不能为空"));
        }
        sql = sql.trim();
        if (!isSelectOnly(sql)) {
            return ResponseEntity.badRequest().body(Map.of("error", "仅支持 SELECT 查询"));
        }
        String host = getStr(body, "host");
        String username = getStr(body, "username");
        String password = body != null && body.get("password") != null ? body.get("password").toString() : null;
        if (host == null || host.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "请填写主机"));
        }
        if (username == null || username.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "请填写用户名"));
        }
        if (password == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "请填写密码"));
        }
        String port = getStr(body, "port");
        String database = getStr(body, "database");
        String h = host;
        String pt = port == null || port.isBlank() ? "5432" : port;
        String db = database == null || database.isBlank() ? "postgres" : database;
        String url = "jdbc:postgresql://" + h + ":" + pt + "/" + db + "?useSSL=false";
        try {
            List<Map<String, Object>> rows;
            try (java.sql.Connection conn = java.sql.DriverManager.getConnection(url, username, password);
                 java.sql.Statement st = conn.createStatement();
                 java.sql.ResultSet rs = st.executeQuery(sql)) {
                rows = resultSetToList(rs);
            }
            List<String> columns = rows.isEmpty() ? List.of() : new ArrayList<>(rows.get(0).keySet());
            return ResponseEntity.ok(Map.of("columns", columns, "rows", rows));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", e.getMessage() != null ? e.getMessage() : "执行失败"
            ));
        }
    }

    private static String getStr(Map<String, Object> m, String key) {
        Object v = m != null ? m.get(key) : null;
        return v != null ? v.toString().trim() : null;
    }

    private static List<Map<String, Object>> resultSetToList(java.sql.ResultSet rs) throws java.sql.SQLException {
        java.sql.ResultSetMetaData meta = rs.getMetaData();
        int colCount = meta.getColumnCount();
        List<String> colNames = new ArrayList<>();
        for (int i = 1; i <= colCount; i++) colNames.add(meta.getColumnLabel(i));
        List<Map<String, Object>> rows = new ArrayList<>();
        while (rs.next()) {
            Map<String, Object> row = new LinkedHashMap<>();
            for (String col : colNames) row.put(col, rs.getObject(col));
            rows.add(row);
        }
        return rows;
    }

    private static boolean isSelectOnly(String sql) {
        String upper = sql.toUpperCase().stripLeading();
        if (upper.startsWith("/*")) {
            int end = upper.indexOf("*/");
            if (end > 0) upper = upper.substring(end + 2).stripLeading();
        }
        if (!upper.startsWith("SELECT") && !upper.startsWith("WITH")) return false;
        Set<String> forbidden = Set.of("INSERT", "UPDATE", "DELETE", "DROP", "CREATE", "ALTER", "TRUNCATE", "GRANT", "REVOKE");
        for (String word : forbidden) {
            if (upper.contains(word)) return false;
        }
        return true;
    }
}
