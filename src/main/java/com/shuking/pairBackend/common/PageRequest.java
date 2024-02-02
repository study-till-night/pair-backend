package com.shuking.pairBackend.common;

import lombok.Data;

import java.io.Serializable;

@Data
public class PageRequest implements Serializable {
    protected Integer pageSize = 10;
    protected Integer pageNum = 1;
}
