package com.ly.factmesh.common.result;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

/**
 * 统一分页响应体
 * 与 Result 配合使用：Result&lt;PageResult&lt;T&gt;&gt;
 *
 * @author LY-FactMesh
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageResult<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 当前页数据 */
    private List<T> records;
    /** 总记录数 */
    private long total;
    /** 当前页码，从 1 开始 */
    private long current;
    /** 每页大小 */
    private long size;

    public static <T> PageResult<T> of(List<T> records, long total, long current, long size) {
        return new PageResult<T>(
                records != null ? records : Collections.<T>emptyList(),
                total,
                current,
                size
        );
    }

    public static <T> PageResult<T> empty(long current, long size) {
        return of(Collections.emptyList(), 0L, current, size);
    }
}
