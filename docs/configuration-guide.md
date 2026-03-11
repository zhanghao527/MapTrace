# 配置指南 - 简化版

## 快速开始（开发环境）

### 方式 1：直接写在配置文件（最简单）

1. **编辑开发环境配置**

```bash
# 编辑 server/src/main/resources/application-dev.yml
nano server/src/main/resources/application-dev.yml
```

2. **填入你的配置**

```yaml
spring:
  datasource:
    password: root  # 你的 MySQL 密码

cos:
  secret-id: your-cos-secret-id      # 腾讯云 COS
  secret-key: your-cos-secret-key
  bucket: your-bucket-name

wechat:
  app-id: your-wechat-app-id         # 微信小程序
  app-secret: your-wechat-app-secret

tencent-map:
  key: your-tencent-map-key          # 腾讯地图
```

3. **启动应用**

```bash
cd server
mvn spring-boot:run
# 默认使用 dev 配置，不需要设置环境变量
```

**优点**：
- ✅ 超级简单，不用折腾环境变量
- ✅ 适合个人开发、学习项目

**注意**：
- ⚠️ 不要把 `application-dev.yml` 提交到 Git（如果包含真实密钥）
- ⚠️ 只适合开发环境，生产环境不要这样做

---

### 方式 2：使用环境变量（推荐生产环境）

1. **创建 .env 文件**

```bash
cd server
nano .env
```

2. **写入配置**

```bash
export DB_PASSWORD=root
export JWT_SECRET=$(openssl rand -base64 32)
export COS_SECRET_ID=your-cos-secret-id
export COS_SECRET_KEY=your-cos-secret-key
export COS_BUCKET=your-bucket-name
export WX_APP_ID=your-wechat-app-id
export WX_APP_SECRET=your-wechat-app-secret
export TENCENT_MAP_KEY=your-tencent-map-key
export REDIS_HOST=localhost
```

3. **启动应用**

```bash
cd server
source .env  # 加载环境变量
java -jar target/timemap-server.jar --spring.profiles.active=prod
```

**优点**：
- ✅ 安全，敏感信息不进 Git
- ✅ 灵活，不同环境用不同配置
- ✅ 符合行业标准

---

## 配置文件说明

项目现在有 3 个配置文件：

### 1. application.yml（主配置）
- 通用配置（端口、文件上传大小等）
- 默认使用 `dev` 环境

### 2. application-dev.yml（开发环境）
- 所有配置直接写死
- 适合本地开发
- **可以提交到 Git**（如果不包含真实密钥）

### 3. application-prod.yml（生产环境）
- 所有敏感配置使用环境变量
- 适合生产部署
- **可以安全提交到 Git**

---

## 切换环境

### 开发环境（默认）

```bash
# 方式 1：直接启动（默认 dev）
mvn spring-boot:run

# 方式 2：明确指定
mvn spring-boot:run -Dspring.profiles.active=dev
```

### 生产环境

```bash
# 方式 1：命令行参数
java -jar target/timemap-server.jar --spring.profiles.active=prod

# 方式 2：环境变量
export SPRING_PROFILE=prod
java -jar target/timemap-server.jar
```

---

## 推荐配置方案

### 个人开发 / 学习项目

**推荐**：直接写在 `application-dev.yml`

```yaml
# application-dev.yml
spring:
  datasource:
    password: root  # 直接写

jwt:
  secret: dev-secret-key  # 开发环境用简单密钥

cos:
  secret-id: your-id  # 直接写
  secret-key: your-key
```

**启动**：
```bash
mvn spring-boot:run  # 就这么简单
```

---

### 团队项目 / 生产环境

**推荐**：使用环境变量 + application-prod.yml

**步骤 1**：配置文件只写占位符
```yaml
# application-prod.yml
spring:
  datasource:
    password: ${DB_PASSWORD}  # 从环境变量读取

jwt:
  secret: ${JWT_SECRET}  # 从环境变量读取
```

**步骤 2**：创建 .env 文件（不提交到 Git）
```bash
# .env
export DB_PASSWORD=real_password
export JWT_SECRET=$(openssl rand -base64 32)
```

**步骤 3**：启动
```bash
source .env
java -jar app.jar --spring.profiles.active=prod
```

---

## 安全建议

### ✅ 可以提交到 Git 的

- `application.yml`（主配置）
- `application-dev.yml`（如果只包含测试数据）
- `application-prod.yml`（只包含占位符）

### ❌ 不要提交到 Git 的

- `.env`（包含真实密钥）
- `application-dev.yml`（如果包含真实密钥）

**添加到 .gitignore**：
```bash
echo ".env" >> .gitignore
echo "application-local.yml" >> .gitignore
```

---

## 常见问题

### Q1: 我是个人开发者，用哪种方式？

**A**: 直接写在 `application-dev.yml`，最简单。

```bash
# 1. 编辑配置文件
nano server/src/main/resources/application-dev.yml

# 2. 填入你的密钥

# 3. 启动
mvn spring-boot:run
```

---

### Q2: 我要部署到服务器，用哪种方式？

**A**: 使用环境变量 + `application-prod.yml`。

```bash
# 1. 在服务器上创建 .env
nano /opt/timemap/.env

# 2. 写入真实配置
export DB_PASSWORD=real_password
export JWT_SECRET=real_secret

# 3. 启动
source /opt/timemap/.env
java -jar timemap-server.jar --spring.profiles.active=prod
```

---

### Q3: 为什么有人用环境变量？

**A**: 主要是为了安全和灵活性：

1. **安全**：密码不会被提交到 Git
2. **灵活**：同一份代码，不同环境用不同配置
3. **标准**：符合 12-Factor App 原则（行业标准）

但对于个人项目，直接写在配置文件也完全可以。

---

### Q4: 我可以混合使用吗？

**A**: 可以！这是最实用的方案：

```yaml
# application-dev.yml
spring:
  datasource:
    password: root  # 开发环境直接写

# application-prod.yml  
spring:
  datasource:
    password: ${DB_PASSWORD}  # 生产环境用环境变量
```

---

## 总结

| 场景 | 推荐方案 | 复杂度 |
|------|---------|--------|
| 个人学习 | 直接写在 application-dev.yml | ⭐ 简单 |
| 个人项目 | 直接写在 application-dev.yml | ⭐ 简单 |
| 团队开发 | 环境变量 + application-prod.yml | ⭐⭐ 中等 |
| 生产部署 | 环境变量 + application-prod.yml | ⭐⭐ 中等 |
| 企业级 | 配置中心（Apollo/Nacos） | ⭐⭐⭐ 复杂 |

**我的建议**：
- 刚开始学习？用 `application-dev.yml` 直接写
- 要上线了？改用环境变量 + `application-prod.yml`
- 不用一开始就搞得很复杂
