package com.timemap.model.dto;

import lombok.Data;
import java.util.List;

@Data
public class CommunityPageResponse {
    private List<CommunityPhotoResponse> list;
    private long total;
    private boolean hasMore;
}
