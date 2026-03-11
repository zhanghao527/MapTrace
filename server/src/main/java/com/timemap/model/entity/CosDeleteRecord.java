package com.timemap.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * COS 文件延迟删除记录表
 * 用于支持内容申诉后的恢复
 */
@Data
@TableName("t_cos_delete_record")
public class CosDeleteRecord {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 文件 URL */
    private String fileUrl;

    /** 关联的内容类型: photo/comment */
    private String contentType;

    /** 关联的内容 ID */
    private Long contentId;

    /** 删除原因: report_resolved/user_delete */
    private String deleteReason;

    /** 计划删除时间（30天后） */
    private LocalDateTime scheduledDeleteTime;

    /** 是否已物理删除 */
    private Integer isDeleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
