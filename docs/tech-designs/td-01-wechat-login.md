# TD-01：微信登录 - 技术方案

对应需求：[PRD-01 微信登录](../prds/prd-01-wechat-login.md)

## 1. 后端实现

### 1.1 新增/修改文件清单

```
server/src/main/java/com/timemap/
├── controller/AuthController.java        # 登录接口
├── service/AuthService.java              # 登录业务逻辑接口
├── service/impl/AuthServiceImpl.java     # 登录业务逻辑实现
├── model/dto/LoginRequest.java           # 登录请求参数
├── model/dto/LoginResponse.java          # 登录响应参数
├── model/dto/WxSessionResponse.java      # 微信 jscode2session 响应
├── util/JwtUtil.java                     # JWT 工具类
├── util/WxApiUtil.java                   # 微信接口调用工具
├── config/JwtInterceptor.java            # JWT 鉴权拦截器
├── config/WebConfig.java                 # 注册拦截器（已有，修改）
├── config/WxConfig.java                  # 微信配置属性类
└── common/Result.java                    # 统一响应（已有，不改）
```

### 1.2 核心流程

```
AuthController.login(LoginRequest)
    │
    ▼
AuthServiceImpl.login(code)
    │
    ├── 1. WxApiUtil.code2Session(code) 调用微信接口获取 openid
    │
    ├── 2. UserMapper.selectByOpenid(openid) 查询用户
    │       ├── 存在 → 使用已有用户
    │       └── 不存在 → 插入新用户
    │
    └── 3. JwtUtil.generateToken(userId, openid) 签发 Token
    │
    ▼
返回 LoginResponse(token, userId, isNew)
```

### 1.3 JWT 实现细节

- 依赖：jjwt 0.12.6
- 算法：HS256
- Payload：{ "userId": Long, "openid": String, "iat": timestamp, "exp": timestamp }
- 有效期：7 天（604800000 毫秒）
- Secret：从 application.yml 读取 ${JWT_SECRET}

### 1.4 拦截器逻辑

```
请求进入
    │
    ├── 路径匹配 /api/auth/** → 放行
    │
    └── 其他 /api/** 路径
            │
            ├── 读取 Header: Authorization
            │       ├── 为空 → 返回 401
            │       └── 解析 Bearer Token
            │               ├── 解析失败/过期 → 返回 401
            │               └── 解析成功 → 将 userId 存入 request attribute → 放行
```

### 1.5 微信接口调用

- 使用 Spring Boot 自带的 RestTemplate
- 请求：GET https://api.weixin.qq.com/sns/jscode2session?appid={}&secret={}&js_code={}&grant_type=authorization_code
- 超时设置：连接 5 秒，读取 10 秒
- 错误处理：微信返回 errcode != 0 时抛出业务异常

## 2. 前端实现

### 2.1 修改文件清单

```
miniprogram/
├── app.js                    # onLaunch 中添加自动登录逻辑
└── utils/request.js          # 添加 401 自动重新登录
```

### 2.2 登录流程

```javascript
// app.js onLaunch 伪代码
1. token = wx.getStorageSync('token')
2. if (token) → globalData.token = token → 结束
3. if (!token) → wx.login() 获取 code
4. request('/auth/login', 'POST', { code })
5. 成功 → wx.setStorageSync('token', token) → globalData.token = token
6. 失败 → 提示用户
```

### 2.3 请求封装改造

- 401 响应 → 清除本地 token → 重新调用 wx.login → 重新请求登录接口 → 重试原请求
- 防止并发多次重新登录：加一个 isRefreshing 标志

## 3. 依赖说明

后端无需新增 Maven 依赖，pom.xml 中已包含：
- spring-boot-starter-web（RestTemplate）
- jjwt-api / jjwt-impl / jjwt-jackson（JWT）
- mybatis-plus-spring-boot3-starter（数据库操作）
- lombok（简化代码）
