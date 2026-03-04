package com.ly.factmesh.admin.infrastructure.database.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * InfluxDB 连接配置实体
 *
 * @author 屈想顺
 */
@Data
@TableName("sys_influxdb_connection")
public class InfluxDbConnectionEntity {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("name")
    private String name;

    @TableField("url")
    private String url;

    @TableField("token")
    private String token;

    @TableField("org")
    private String org;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
