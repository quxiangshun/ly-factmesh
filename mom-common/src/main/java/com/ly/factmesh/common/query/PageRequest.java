package com.ly.factmesh.common.query;

import lombok.Data;

/**
 * 通用分页请求参数
 *
 * @author LY-FactMesh
 */
@Data
public class PageRequest {

    private int page = 1;
    private int size = 10;

    public long getOffset() {
        return (long) (page - 1) * size;
    }
}
