package com.mulehang.blog.model;

import lombok.Data;

import java.util.Collections;
import java.util.List;

/**
 * 统一分页返回模型。
 *
 * @param <T> 列表元素类型
 */
@Data
public class PageResult<T> {

    private List<T> list;
    private long total;
    private long pageNo;
    private long pageSize;

    public static <T> PageResult<T> empty(long pageNo, long pageSize) {
        PageResult<T> r = new PageResult<>();
        r.list = Collections.emptyList();
        r.total = 0;
        r.pageNo = pageNo;
        r.pageSize = pageSize;
        return r;
    }
}
