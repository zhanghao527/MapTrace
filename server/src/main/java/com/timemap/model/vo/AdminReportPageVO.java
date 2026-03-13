package com.timemap.model.vo;

import lombok.Data;
import java.util.List;

@Data
public class AdminReportPageVO {
    private List<AdminReportListItemVO> list;
    private Long total;
    private Boolean hasMore;
}
