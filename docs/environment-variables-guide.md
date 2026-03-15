# 环境变量配置指南

## 什么是环境变量？

环境变量是操作系统提供的一种配置方式，用于存储程序运行时需要的配置信息。

**为什么要用环境变量？**
- ✅ **安全**：密码、密钥等敏感信息不写在代码里
- ✅ **灵活**：不同环境（开发/测试/生产）可以用不同配置
- ✅ **方便**：修改配置不需要重新编译代码

---

## 快速开始

### 方法 1：使用配置向导（推荐）

```bash
cd server
chmod +x setup-env.sh
./setup-env.sh
```

脚本会引导你输入所有配置，并自动生成 `.env` 文件。

### 方法 2：手动配置

```bash
cd server

# 1. 复制示例文件
cp .env.example .env

# 2. 编辑 .env 文件
nano .env  # 或者用其他编辑器

# 3. 生成 JWT 密钥
openssl rand -base64 32

# 4. 把生成的密钥填入 .env 文件的 JWT_SECRET
```

### 方法 3：临时设置（仅当前会话有效）

```bash
# 直接在命令行设置
export JWT_SECRET=$(openssl rand -base64 32)
export REDIS_HOST=localhost
export DB_PASSWORD=your_password

# 然后启动应用
java -jar target/maptrace-server.jar
```

---

## 必需的环境变量

### 1. JWT_SECRET（必需）

**作用**：用于签名 JWT Token（用户登录凭证）

**生成方法**：
```bash
openssl rand -base64 32
```

**示例**：
```bash
export JWT_SECRET=K8x2mP9vQ3nR7sT1wY4zB6cD8eF0gH2jL5mN8pQ1rS4t
```

**为什么重要？**
- 如果使用默认值，任何人都可以伪造用户 Token
- 必须是强随机密钥（至少 32 字节）
- 生产环境绝对不能用默认值

---

### 2. DB_PASSWORD（必需）

**作用**：MySQL 数据库密码

**示例**：
```bash
export DB_PASSWORD=your_mysql_root_password
```

---

### 3. COS 配置（必需，用于图片上传）

**作用**：腾讯云对象存储配置

**获取方法**：
1. 登录腾讯云控制台
2. 进入「访问管理」→「API 密钥管理」
3. 创建密钥，获取 SecretId 和 SecretKey

**示例**：
```bash
export COS_SECRET_ID=AKIDxxxxxxxxxxxxxxxxxxxxx
export COS_SECRET_KEY=xxxxxxxxxxxxxxxxxxxxxxxx
export COS_REGION=ap-shanghai
export COS_BUCKET=your-bucket-name
```

---

### 4. 微信小程序配置（必需）

**作用**：微信登录功能

**获取方法**：
1. 登录微信公众平台
2. 进入「开发」→「开发管理」→「开发设置」
3. 查看 AppID 和 AppSecret

**示例**：
```bash
export WX_APP_ID=wx1234567890abcdef
export WX_APP_SECRET=abcdef1234567890abcdef1234567890
```

---

### 5. 腾讯地图配置（必需）

**作用**：地图功能

**获取方法**：
1. 登录腾讯位置服务控制台
2. 创建应用，获取 Key

**示例**：
```bash
export TENCENT_MAP_KEY=XXXBZ-XXXXX-XXXXX-XXXXX-XXXXX-XXXXX
```

---

## 可选的环境变量

### 6. Redis 配置（推荐）

**作用**：缓存和频率限制

**默认值**：localhost:6379，无密码

**示例**：
```bash
export REDIS_HOST=localhost
export REDIS_PORT=6379
export REDIS_PASSWORD=your_redis_password  # 如果没有密码，留空
```

---

### 7. MyBatis 日志配置（可选）

**作用**：控制是否打印 SQL 日志

**选项**：
- 开发环境：`org.apache.ibatis.logging.stdout.StdOutImpl`（打印 SQL）
- 生产环境：`org.apache.ibatis.logging.nologging.NoLoggingImpl`（不打印）

**示例**：
```bash
# 开发环境
export MYBATIS_LOG_IMPL=org.apache.ibatis.logging.stdout.StdOutImpl

# 生产环境（默认）
export MYBATIS_LOG_IMPL=org.apache.ibatis.logging.nologging.NoLoggingImpl
```

---

## 使用环境变量的三种方式

### 方式 1：从 .env 文件加载（推荐）

```bash
# 1. 创建 .env 文件
cd server
nano .env

# 2. 写入配置
export JWT_SECRET=your_secret_here
export REDIS_HOST=localhost
export DB_PASSWORD=your_password

# 3. 加载环境变量
source .env

# 4. 启动应用
mvn spring-boot:run
```

### 方式 2：在启动命令中设置

```bash
JWT_SECRET=xxx REDIS_HOST=localhost java -jar target/maptrace-server.jar
```

### 方式 3：在 IDE 中配置

**IntelliJ IDEA**：
1. 打开 Run/Debug Configurations
2. 找到 Environment variables
3. 添加环境变量：`JWT_SECRET=xxx;REDIS_HOST=localhost`

**VS Code**：
1. 打开 `.vscode/launch.json`
2. 添加 `env` 配置：
```json
{
  "env": {
    "JWT_SECRET": "xxx",
    "REDIS_HOST": "localhost"
  }
}
```

---

## 验证环境变量

### 查看当前环境变量

```bash
# 查看所有环境变量
env

# 查看特定环境变量
echo $JWT_SECRET
echo $REDIS_HOST
```

### 测试应用是否正确读取

启动应用后，查看日志：

```bash
# 如果看到这些，说明配置正确
✓ Redis 连接成功
✓ 数据库连接成功
✓ JWT 配置加载成功
```

---

## 安全建议

### ✅ 应该做的

1. **使用强随机密钥**
   ```bash
   # 好的做法
   export JWT_SECRET=$(openssl rand -base64 32)
   ```

2. **不要提交 .env 文件到 Git**
   ```bash
   # 添加到 .gitignore
   echo ".env" >> .gitignore
   ```

3. **定期更换密钥**
   - JWT 密钥：每 3-6 个月更换一次
   - 数据库密码：每 6-12 个月更换一次

4. **不同环境使用不同配置**
   - 开发环境：`.env.dev`
   - 测试环境：`.env.test`
   - 生产环境：`.env.prod`

### ❌ 不应该做的

1. **不要使用默认值**
   ```bash
   # 危险！
   export JWT_SECRET=maptrace-dev-secret-key-change-in-production
   ```

2. **不要在代码中硬编码**
   ```java
   // 危险！
   String jwtSecret = "my-secret-key";
   ```

3. **不要在日志中打印敏感信息**
   ```java
   // 危险！
   log.info("JWT Secret: {}", jwtSecret);
   ```

---

## 常见问题

### Q1: 为什么启动时提示找不到环境变量？

**A**: 环境变量只在当前 Shell 会话有效，关闭终端后会失效。

**解决方案**：
```bash
# 每次启动前加载
source .env
java -jar target/maptrace-server.jar
```

或者写一个启动脚本：
```bash
#!/bin/bash
source .env
java -jar target/maptrace-server.jar
```

---

### Q2: 如何在 Docker 中使用环境变量？

**A**: 使用 `docker run -e` 或 `docker-compose.yml`

```bash
# 方式 1：命令行
docker run -e JWT_SECRET=xxx -e REDIS_HOST=redis maptrace-server

# 方式 2：docker-compose.yml
services:
  app:
    environment:
      - JWT_SECRET=xxx
      - REDIS_HOST=redis
```

---

### Q3: 如何在生产环境管理环境变量？

**A**: 使用专业的密钥管理服务

- **云服务**：AWS Secrets Manager、阿里云 KMS
- **容器编排**：Kubernetes Secrets
- **配置中心**：Apollo、Nacos

---

## 完整配置示例

```bash
# .env 文件示例

# 数据库
export DB_PASSWORD=MyStr0ngP@ssw0rd

# JWT
export JWT_SECRET=K8x2mP9vQ3nR7sT1wY4zB6cD8eF0gH2jL5mN8pQ1rS4t

# Redis
export REDIS_HOST=localhost
export REDIS_PORT=6379
export REDIS_PASSWORD=

# 腾讯云 COS
export COS_SECRET_ID=AKIDxxxxxxxxxxxxxxxxxxxxx
export COS_SECRET_KEY=xxxxxxxxxxxxxxxxxxxxxxxx
export COS_REGION=ap-shanghai
export COS_BUCKET=maptrace-prod-bucket

# 微信小程序
export WX_APP_ID=wx1234567890abcdef
export WX_APP_SECRET=abcdef1234567890abcdef1234567890

# 腾讯地图
export TENCENT_MAP_KEY=XXXBZ-XXXXX-XXXXX-XXXXX-XXXXX-XXXXX

# MyBatis 日志
export MYBATIS_LOG_IMPL=org.apache.ibatis.logging.nologging.NoLoggingImpl
```

---

## 总结

环境变量是配置应用的标准方式，主要用于：
1. 存储敏感信息（密码、密钥）
2. 区分不同环境的配置
3. 提高应用的安全性和灵活性

**记住**：
- ✅ 使用强随机密钥
- ✅ 不要提交 .env 到 Git
- ✅ 定期更换密钥
- ❌ 不要使用默认值
- ❌ 不要硬编码敏感信息
