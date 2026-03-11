-- v10: 软删除优化 - 支持内容恢复

-- 1. 为 t_photo 添加 district 字段（如果还没有）
-- MySQL 不支持 ADD COLUMN IF NOT EXISTS，需要先检查
ALTER TABLE t_photo ADD COLUMN district VARCHAR(100) DEFAULT '' COMMENT '行政区划（区/县）' AFTER location_name;

-- 2. 创建 COS 延迟删除记录表
CREATE TABLE IF NOT EXISTS `t_cos_delete_record` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `file_url` VARCHAR(512) NOT NULL COMMENT '文件URL',
  `content_type` VARCHAR(20) NOT NULL COMMENT '内容类型: photo/comment',
  `content_id` BIGINT NOT NULL COMMENT '关联内容ID',
  `delete_reason` VARCHAR(50) NOT NULL COMMENT '删除原因: report_resolved/user_delete',
  `scheduled_delete_time` DATETIME NOT NULL COMMENT '计划删除时间（30天后）',
  `is_deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否已物理删除',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  INDEX `idx_scheduled_time` (`scheduled_delete_time`, `is_deleted`),
  INDEX `idx_content` (`content_type`, `content_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='COS文件延迟删除记录';

-- 3. 添加关键索引优化查询性能
-- t_photo 的社区查询索引
CREATE INDEX idx_district_date ON t_photo(district, photo_date, deleted);

-- t_comment 的用户评论查询索引
CREATE INDEX idx_user_time ON t_comment(user_id, create_time, deleted);

-- t_report 的管理员处理记录索引
CREATE INDEX idx_handler_time ON t_report(handled_by, create_time);

-- t_user 的手机号查询索引（如果还没有）
CREATE INDEX idx_user_phone ON t_user(phone);

-- 4. 为 t_user 添加违规和处罚相关字段（如果还没有）
ALTER TABLE t_user ADD COLUMN violation_count INT DEFAULT 0 COMMENT '违规次数' AFTER profile_completed;
ALTER TABLE t_user ADD COLUMN is_banned TINYINT(1) DEFAULT 0 COMMENT '是否封号' AFTER violation_count;
ALTER TABLE t_user ADD COLUMN mute_until DATETIME DEFAULT NULL COMMENT '禁言截止时间' AFTER is_banned;
ALTER TABLE t_user ADD COLUMN ban_upload_until DATETIME DEFAULT NULL COMMENT '禁止上传截止时间' AFTER mute_until;
