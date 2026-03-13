package com.timemap.model.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;
import java.util.List;

@Data
public class UserProfileVO {
    @JsonSerialize(using = ToStringSerializer.class)
    private Long userId;
    private String nickname;
    private String avatarUrl;
    private Integer photoCount;
    private Integer areaCount;
    private Integer likeCount;
    private String latestPhotoDate;
    private List<UserAreaStatVO> topAreas;
    private String createTime;
}
