package com.timemap.model.vo;

import lombok.Data;
import java.util.List;

@Data
public class MyReportPageVO {
    private List<MyReportItemVO> list;
    private Long total;
    private Boolean hasMore;
}
