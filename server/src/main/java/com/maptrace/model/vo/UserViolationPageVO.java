package com.maptrace.model.vo;

import lombok.Data;
import java.util.List;

@Data
public class UserViolationPageVO {
    private List<UserViolationVO> list;
    private Long total;
    private Boolean hasMore;
}
