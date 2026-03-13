package com.timemap.common;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

/**
 * 统一分页请求基类，对齐模版规范
 */
@Data
public class PageRequest {

    @Min(value = 1, message = "页码最小为1")
    private long pageNum = 1;

    @Min(value = 1, message = "每页数量最小为1")
    @Max(value = 50, message = "每页数量不能超过50")
    private long pageSize = 10;

    private String sortField;

    private String sortOrder = "descend";
}
