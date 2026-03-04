package com.ly.factmesh.admin.infrastructure.database.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Redis 连接配置实体
 *
 * @author 屈想顺
 */
@Data
@TableName("sys_redis_connection")
public class RedisConnectionEntity {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("name")
    private String name;

    @TableField("host")
    private String host;

    @TableField("port")
    private Integer port;

    @TableField("password")
    private String password;

    @TableField("database")
    private Integer database;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
