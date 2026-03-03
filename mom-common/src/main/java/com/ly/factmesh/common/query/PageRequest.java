package com.ly.factmesh.common.query;

import lombok.Data;

/**
 * 通用分页请求参数。page 从 1 开始，getOffset() 用于数据库 OFFSET 计算
 *
 * @author LY-FactMesh
 */
@Data
public class PageRequest {

    private int page = 1;
    private int size = 10;

    /** 计算 SQL OFFSET： (page-1) * size */
    public long getOffset() {
        return (long) (page - 1) * size;
    }
}
