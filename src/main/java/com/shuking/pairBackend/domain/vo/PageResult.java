package com.shuking.pairBackend.domain.vo;

import lombok.Data;

import java.util.List;

/**
 * 分页返回对象
 *
 * @param <T> 查询数据类型
 */
@Data
public class PageResult<T> {

    private long pageSize;

    private long pageNum;

    private long total;

    private List<T> records;
}
