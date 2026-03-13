-- v11: 微信订阅消息 — 数据库迁移

-- 用户订阅消息授权记录表
CREATE TABLE IF NOT EXISTS `t_subscribe_message` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `template_id` VARCHAR(100) NOT NULL COMMENT '模板ID',
  `remaining_count` INT NOT NULL DEFAULT 0 COMMENT '剩余可发送次数',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `uk_user_template` (`user_id`, `template_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户订阅消息授权记录';
