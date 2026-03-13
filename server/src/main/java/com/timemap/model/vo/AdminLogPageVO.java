package com.timemap.model.vo;

import lombok.Data;
import java.util.List;

@Data
public class AdminLogPageVO {
    private List<AdminLogVO> list;
    private Long total;
    private Boolean hasMore;
}
