package com.maptrace.model.vo;

import lombok.Data;
import java.util.List;

@Data
public class CommunityPageVO {
    private List<CommunityPhotoVO> list;
    private long total;
    private boolean hasMore;
}
