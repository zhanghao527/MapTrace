#!/bin/bash
# 地图时迹 - 环境变量配置脚本

echo "=========================================="
echo "地图时迹 - 环境变量配置向导"
echo "=========================================="
echo ""

# 1. 生成 JWT 密钥
echo "1. 生成 JWT 密钥..."
JWT_SECRET=$(openssl rand -base64 32)
export JWT_SECRET
echo "   ✓ JWT_SECRET 已生成"

# 2. 数据库配置
echo ""
echo "2. 配置数据库..."
read -p "   请输入 MySQL root 密码: " -s DB_PASSWORD
echo ""
export DB_PASSWORD
echo "   ✓ DB_PASSWORD 已设置"

# 3. Redis 配置
echo ""
echo "3. 配置 Redis..."
read -p "   Redis 地址 (默认 localhost): " REDIS_HOST
REDIS_HOST=${REDIS_HOST:-localhost}
export REDIS_HOST
echo "   ✓ REDIS_HOST = $REDIS_HOST"

read -p "   Redis 端口 (默认 6379): " REDIS_PORT
REDIS_PORT=${REDIS_PORT:-6379}
export REDIS_PORT
echo "   ✓ REDIS_PORT = $REDIS_PORT"

read -p "   Redis 密码 (没有密码直接回车): " -s REDIS_PASSWORD
echo ""
export REDIS_PASSWORD
echo "   ✓ REDIS_PASSWORD 已设置"

# 4. 腾讯云 COS 配置
echo ""
echo "4. 配置腾讯云 COS..."
read -p "   COS SecretId: " COS_SECRET_ID
export COS_SECRET_ID
echo "   ✓ COS_SECRET_ID 已设置"

read -p "   COS SecretKey: " -s COS_SECRET_KEY
echo ""
export COS_SECRET_KEY
echo "   ✓ COS_SECRET_KEY 已设置"

read -p "   COS 地域 (默认 ap-shanghai): " COS_REGION
COS_REGION=${COS_REGION:-ap-shanghai}
export COS_REGION
echo "   ✓ COS_REGION = $COS_REGION"

read -p "   COS 存储桶名称: " COS_BUCKET
export COS_BUCKET
echo "   ✓ COS_BUCKET = $COS_BUCKET"

# 5. 微信小程序配置
echo ""
echo "5. 配置微信小程序..."
read -p "   微信小程序 AppID: " WX_APP_ID
export WX_APP_ID
echo "   ✓ WX_APP_ID 已设置"

read -p "   微信小程序 AppSecret: " -s WX_APP_SECRET
echo ""
export WX_APP_SECRET
echo "   ✓ WX_APP_SECRET 已设置"

# 6. 腾讯地图配置
echo ""
echo "6. 配置腾讯地图..."
read -p "   腾讯地图 API Key: " TENCENT_MAP_KEY
export TENCENT_MAP_KEY
echo "   ✓ TENCENT_MAP_KEY 已设置"

# 7. MyBatis 日志配置
echo ""
echo "7. 配置 MyBatis 日志..."
echo "   开发环境: org.apache.ibatis.logging.stdout.StdOutImpl"
echo "   生产环境: org.apache.ibatis.logging.nologging.NoLoggingImpl"
read -p "   选择环境 (dev/prod, 默认 prod): " ENV_TYPE
ENV_TYPE=${ENV_TYPE:-prod}

if [ "$ENV_TYPE" = "dev" ]; then
    export MYBATIS_LOG_IMPL=org.apache.ibatis.logging.stdout.StdOutImpl
    echo "   ✓ 已启用 SQL 日志（开发模式）"
else
    export MYBATIS_LOG_IMPL=org.apache.ibatis.logging.nologging.NoLoggingImpl
    echo "   ✓ 已关闭 SQL 日志（生产模式）"
fi

# 8. 保存到 .env 文件
echo ""
echo "=========================================="
echo "配置完成！"
echo "=========================================="
echo ""
echo "环境变量已设置到当前 Shell 会话。"
echo ""
read -p "是否保存到 .env 文件？(y/n): " SAVE_ENV

if [ "$SAVE_ENV" = "y" ] || [ "$SAVE_ENV" = "Y" ]; then
    cat > .env << EOF
# 地图时迹 - 环境变量配置
# 生成时间: $(date)

export DB_PASSWORD=$DB_PASSWORD
export JWT_SECRET=$JWT_SECRET
export REDIS_HOST=$REDIS_HOST
export REDIS_PORT=$REDIS_PORT
export REDIS_PASSWORD=$REDIS_PASSWORD
export COS_SECRET_ID=$COS_SECRET_ID
export COS_SECRET_KEY=$COS_SECRET_KEY
export COS_REGION=$COS_REGION
export COS_BUCKET=$COS_BUCKET
export WX_APP_ID=$WX_APP_ID
export WX_APP_SECRET=$WX_APP_SECRET
export TENCENT_MAP_KEY=$TENCENT_MAP_KEY
export MYBATIS_LOG_IMPL=$MYBATIS_LOG_IMPL
EOF
    echo "✓ 已保存到 .env 文件"
    echo ""
    echo "下次启动时，执行以下命令加载环境变量："
    echo "  source .env"
fi

echo ""
echo "现在可以启动应用了："
echo "  mvn spring-boot:run"
echo "或者："
echo "  java -jar target/timemap-server-0.0.1-SNAPSHOT.jar"
echo ""
