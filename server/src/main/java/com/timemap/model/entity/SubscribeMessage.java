package com.timemap.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("t_subscribe_message")
public class SubscribeMessage {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String templateId;
    private Integer remainingCount;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
