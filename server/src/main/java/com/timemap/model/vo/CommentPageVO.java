package com.timemap.model.vo;

import lombok.Data;
import java.util.List;

@Data
public class CommentPageVO {
    private List<CommentVO> list;
    private Long total;
    private Boolean hasMore;
}
