# 地图时迹 - 快速部署指南

## 前置条件

- Java 17+
- MySQL 8.0+
- Redis 7.x
- Maven 3.9+
- Node.js 18+ (前端)

---

## 1. 数据库初始化

```bash
# 创建数据库并导入初始化脚本
mysql -u root -p < server/src/main/resources/db/init.sql

# 执行所有迁移脚本
mysql -u root -p timemap < docs/sql/v04-comments-messages.sql
mysql -u root -p timemap < docs/sql/v05-notification-report.sql
mysql -u root -p timemap < docs/sql/v06-report-moderation-phase1.sql
mysql -u root -p timemap < docs/sql/v07-enterprise-report-governance.sql
mysql -u root -p timemap < docs/sql/v08-web-admin-console.sql
mysql -u root -p timemap < docs/sql/v09-enterprise-login-profile.sql
mysql -u root -p timemap < docs/sql/v10-soft-delete-optimization.sql
```

---

## 2. Redis 安装与配置

```bash
# macOS
brew install redis
brew services start redis

# Ubuntu/Debian
sudo apt install redis-server
sudo systemctl start redis

# 验证 Redis 运行
redis-cli ping
# 应返回 PONG
```

---

## 3. 后端配置

### 3.1 创建环境变量文件

```bash
# server/.env
export DB_PASSWORD=your_mysql_password
export JWT_SECRET=$(openssl rand -base64 32)
export COS_SECRET_ID=your_tencent_cos_secret_id
export COS_SECRET_KEY=your_tencent_cos_secret_key
export COS_REGION=ap-shanghai
export COS_BUCKET=your-bucket-name
export WX_APP_ID=your_wechat_app_id
export WX_APP_SECRET=your_wechat_app_secret
export TENCENT_MAP_KEY=your_tencent_map_key
export REDIS_HOST=localhost
export REDIS_PORT=6379
export REDIS_PASSWORD=
export MYBATIS_LOG_IMPL=org.apache.ibatis.logging.nologging.NoLoggingImpl
```

### 3.2 加载环境变量并启动

```bash
cd server
source .env
mvn clean package -DskipTests
java -jar target/timemap-server-0.0.1-SNAPSHOT.jar
```

### 3.3 验证后端启动

```bash
# 健康检查
curl http://localhost:8080/api/actuator/health

# Prometheus 指标
curl http://localhost:8080/api/actuator/prometheus
```

---

## 4. 前端部署

### 4.1 Web 管理后台

```bash
cd admin-web
npm install
npm run build

# 部署到 Nginx
sudo cp -r dist/* /var/www/admin.yourdomain.com/
```

### 4.2 微信小程序

```bash
cd miniprogram

# 修改 utils/config.js 中的 API 地址
# const API_BASE_URL = 'https://api.yourdomain.com/api'

# 使用微信开发者工具打开项目
# 上传代码到微信后台
```

---

## 5. Nginx 配置

```nginx
# /etc/nginx/sites-available/timemap

# 后端 API
server {
    listen 80;
    server_name api.yourdomain.com;

    location / {
        proxy_pass http://localhost:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}

# Web 管理后台
server {
    listen 80;
    server_name admin.yourdomain.com;
    root /var/www/admin.yourdomain.com;
    index index.html;

    location / {
        try_files $uri $uri/ /index.html;
    }
}
```

```bash
# 启用配置
sudo ln -s /etc/nginx/sites-available/timemap /etc/nginx/sites-enabled/
sudo nginx -t
sudo systemctl reload nginx
```

---

## 6. HTTPS 配置（推荐）

```bash
# 使用 Let's Encrypt 免费证书
sudo apt install certbot python3-certbot-nginx

# 为域名申请证书
sudo certbot --nginx -d api.yourdomain.com
sudo certbot --nginx -d admin.yourdomain.com
```

---

## 7. 修复安全配置

### 7.1 修改 CORS 配置

```java
// server/src/main/java/com/timemap/config/WebConfig.java
@Override
public void addCorsMappings(CorsRegistry registry) {
    registry.addMapping("/api/**")
            .allowedOriginPatterns(
                "https://admin.yourdomain.com",
                "https://yourdomain.com"
            )  // 替换为实际域名
            .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
            .allowedHeaders("*")
            .exposedHeaders("Authorization")
            .maxAge(3600);
}
```

### 7.2 确保 JWT 密钥安全

```bash
# 生成强随机密钥
openssl rand -base64 32

# 设置环境变量（不要使用默认值）
export JWT_SECRET=<生成的密钥>
```

---

## 8. 监控配置

### 8.1 Prometheus 配置

```yaml
# prometheus.yml
scrape_configs:
  - job_name: 'timemap'
    metrics_path: '/api/actuator/prometheus'
    static_configs:
      - targets: ['localhost:8080']
```

### 8.2 Grafana 仪表盘

1. 导入 Spring Boot 仪表盘模板（ID: 4701）
2. 添加自定义面板监控业务指标：
   - 照片上传量
   - 举报处理量
   - 用户活跃度
   - API 响应时间

---

## 9. 定时任务配置

定时任务已自动启用（`@EnableScheduling`），无需额外配置。

### 当前定时任务
- **COS 文件清理**：每天凌晨 3:00 执行
- 清理超过 30 天的已标记删除文件

### 手动触发（调试用）
```bash
# 需要添加管理员接口
curl -X POST http://localhost:8080/api/admin/tasks/cleanup-cos \
  -H "Authorization: Bearer <admin_token>"
```

---

## 10. 验证部署

### 10.1 后端健康检查

```bash
# 基础健康检查
curl https://api.yourdomain.com/api/actuator/health

# 数据库连接
curl https://api.yourdomain.com/api/actuator/health/db

# Redis 连接
curl https://api.yourdomain.com/api/actuator/health/redis
```

### 10.2 功能测试

```bash
# 微信登录（需要真实 code）
curl -X POST https://api.yourdomain.com/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"code":"<wechat_code>"}'

# 管理员登录
curl -X POST https://api.yourdomain.com/api/admin/auth/login \
  -H "Content-Type: application/json" \
  -H "X-Client-Type: web" \
  -d '{"username":"admin","password":"Admin@2026"}'
```

---

## 11. 性能优化检查

- [ ] Redis 已启动并可连接
- [ ] 数据库索引已创建（v10 迁移脚本）
- [ ] SQL 日志已关闭（生产环境）
- [ ] CORS 已限制为实际域名
- [ ] JWT 密钥已更换为强随机值
- [ ] 定时任务正常运行

---

## 12. 故障排查

### 后端无法启动

```bash
# 检查端口占用
lsof -i :8080

# 查看日志
tail -f logs/spring.log

# 检查环境变量
env | grep -E 'DB_|JWT_|COS_|WX_|REDIS_'
```

### Redis 连接失败

```bash
# 检查 Redis 状态
redis-cli ping

# 检查防火墙
sudo ufw status
sudo ufw allow 6379/tcp

# 查看 Redis 日志
tail -f /var/log/redis/redis-server.log
```

### 数据库连接失败

```bash
# 测试连接
mysql -h localhost -u root -p timemap

# 检查用户权限
GRANT ALL PRIVILEGES ON timemap.* TO 'root'@'localhost';
FLUSH PRIVILEGES;
```

---

## 13. 回滚方案

### 数据库回滚

```bash
# 备份当前数据库
mysqldump -u root -p timemap > backup_$(date +%Y%m%d_%H%M%S).sql

# 回滚到备份
mysql -u root -p timemap < backup_20260311_120000.sql
```

### 应用回滚

```bash
# 停止当前版本
pkill -f timemap-server

# 启动旧版本
java -jar timemap-server-0.0.1-SNAPSHOT.old.jar
```

---

## 14. 生产环境建议

### 资源配置
- **CPU**: 4 核+
- **内存**: 8GB+
- **磁盘**: 100GB+ SSD
- **带宽**: 10Mbps+

### JVM 参数
```bash
java -Xms2g -Xmx4g \
  -XX:+UseG1GC \
  -XX:MaxGCPauseMillis=200 \
  -XX:+HeapDumpOnOutOfMemoryError \
  -XX:HeapDumpPath=/var/log/timemap/heapdump.hprof \
  -jar timemap-server.jar
```

### 数据库优化
```sql
-- 调整 MySQL 配置
SET GLOBAL max_connections = 500;
SET GLOBAL innodb_buffer_pool_size = 2147483648; -- 2GB
SET GLOBAL query_cache_size = 67108864; -- 64MB
```

### Redis 优化
```bash
# redis.conf
maxmemory 1gb
maxmemory-policy allkeys-lru
save 900 1
save 300 10
save 60 10000
```

---

## 15. 联系与支持

- 技术文档：`docs/`
- 问题反馈：GitHub Issues
- 紧急联系：[管理员邮箱]

---

## 附录：环境变量完整列表

| 变量名 | 必需 | 默认值 | 说明 |
|--------|------|--------|------|
| `DB_PASSWORD` | 是 | - | MySQL 密码 |
| `JWT_SECRET` | 是 | - | JWT 签名密钥（32+ 字节） |
| `COS_SECRET_ID` | 是 | - | 腾讯云 COS SecretId |
| `COS_SECRET_KEY` | 是 | - | 腾讯云 COS SecretKey |
| `COS_REGION` | 否 | ap-shanghai | COS 地域 |
| `COS_BUCKET` | 是 | - | COS 存储桶名称 |
| `WX_APP_ID` | 是 | - | 微信小程序 AppID |
| `WX_APP_SECRET` | 是 | - | 微信小程序 AppSecret |
| `TENCENT_MAP_KEY` | 是 | - | 腾讯地图 API Key |
| `REDIS_HOST` | 否 | localhost | Redis 主机地址 |
| `REDIS_PORT` | 否 | 6379 | Redis 端口 |
| `REDIS_PASSWORD` | 否 | - | Redis 密码 |
| `MYBATIS_LOG_IMPL` | 否 | NoLoggingImpl | MyBatis 日志实现 |
