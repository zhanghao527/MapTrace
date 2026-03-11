# V10 数据库迁移指南

## 方法 1：使用安全脚本（推荐，可重复执行）

```bash
mysql -u root -p < docs/sql/v10-soft-delete-optimization-safe.sql
```

这个脚本会自动检查字段和索引是否存在，可以安全地重复执行。

---

## 方法 2：手动分步执行

如果自动脚本有问题，可以手动执行以下步骤：

### 步骤 1：登录 MySQL

```bash
mysql -u root -p
```

### 步骤 2：切换到 timemap 数据库

```sql
USE timemap;
```

### 步骤 3：检查并添加 district 字段

```sql
-- 先检查字段是否存在
SHOW COLUMNS FROM t_photo LIKE 'district';

-- 如果不存在，执行添加
ALTER TABLE t_photo ADD COLUMN district VARCHAR(100) DEFAULT '' COMMENT '行政区划（区/县）' AFTER location_name;
```

### 步骤 4：创建 COS 延迟删除记录表

```sql
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
```

### 步骤 5：添加性能优化索引

```sql
-- 检查索引是否存在
SHOW INDEX FROM t_photo WHERE Key_name = 'idx_district_date';

-- 如果不存在，创建索引
CREATE INDEX idx_district_date ON t_photo(district, photo_date, deleted);
CREATE INDEX idx_user_time ON t_comment(user_id, create_time, deleted);
CREATE INDEX idx_handler_time ON t_report(handled_by, create_time);
CREATE INDEX idx_user_phone ON t_user(phone);
```

**注意**：如果索引已存在，会报错 `Duplicate key name`，这是正常的，可以忽略。

### 步骤 6：添加用户处罚相关字段

```sql
-- 检查字段是否存在
SHOW COLUMNS FROM t_user LIKE 'violation_count';

-- 如果不存在，添加字段
ALTER TABLE t_user ADD COLUMN violation_count INT DEFAULT 0 COMMENT '违规次数' AFTER profile_completed;
ALTER TABLE t_user ADD COLUMN is_banned TINYINT(1) DEFAULT 0 COMMENT '是否封号' AFTER violation_count;
ALTER TABLE t_user ADD COLUMN mute_until DATETIME DEFAULT NULL COMMENT '禁言截止时间' AFTER is_banned;
ALTER TABLE t_user ADD COLUMN ban_upload_until DATETIME DEFAULT NULL COMMENT '禁止上传截止时间' AFTER mute_until;
```

### 步骤 7：验证迁移结果

```sql
-- 验证新表
SHOW TABLES LIKE 't_cos_delete_record';

-- 验证索引
SHOW INDEX FROM t_photo WHERE Key_name = 'idx_district_date';
SHOW INDEX FROM t_comment WHERE Key_name = 'idx_user_time';
SHOW INDEX FROM t_report WHERE Key_name = 'idx_handler_time';
SHOW INDEX FROM t_user WHERE Key_name = 'idx_user_phone';

-- 验证新字段
DESCRIBE t_user;
DESCRIBE t_photo;
```

---

## 常见错误处理

### 错误 1：字段已存在

```
ERROR 1060 (42S21): Duplicate column name 'district'
```

**解决方案**：字段已存在，跳过这一步即可。

### 错误 2：索引已存在

```
ERROR 1061 (42000): Duplicate key name 'idx_district_date'
```

**解决方案**：索引已存在，跳过这一步即可。

### 错误 3：表已存在

```
Table 't_cos_delete_record' already exists
```

**解决方案**：表已存在，跳过这一步即可（因为使用了 `IF NOT EXISTS`）。

### 错误 4：密码错误

```
ERROR 1045 (28000): Access denied for user 'root'@'localhost'
```

**解决方案**：
1. 确认 MySQL root 密码
2. 或者使用其他有权限的用户
3. 或者重置 MySQL root 密码

---

## 回滚方案

如果需要回滚迁移：

```sql
-- 删除新表
DROP TABLE IF EXISTS t_cos_delete_record;

-- 删除索引
DROP INDEX idx_district_date ON t_photo;
DROP INDEX idx_user_time ON t_comment;
DROP INDEX idx_handler_time ON t_report;
DROP INDEX idx_user_phone ON t_user;

-- 删除新字段
ALTER TABLE t_photo DROP COLUMN district;
ALTER TABLE t_user DROP COLUMN violation_count;
ALTER TABLE t_user DROP COLUMN is_banned;
ALTER TABLE t_user DROP COLUMN mute_until;
ALTER TABLE t_user DROP COLUMN ban_upload_until;
```

---

## 执行时间估算

- 小型数据库（< 10万条记录）：约 1-2 分钟
- 中型数据库（10万-100万条记录）：约 5-10 分钟
- 大型数据库（> 100万条记录）：约 10-30 分钟

**注意**：创建索引时会锁表，建议在业务低峰期执行。
