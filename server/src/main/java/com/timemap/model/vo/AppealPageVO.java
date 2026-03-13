package com.timemap.model.vo;

import lombok.Data;
import java.util.List;

@Data
public class AppealPageVO {
    private List<AppealVO> list;
    private Long total;
    private Boolean hasMore;
}
