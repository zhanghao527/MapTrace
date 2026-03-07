# PRD-03：社区时间轴

## 1. 背景与目标

目前用户只能通过地图浏览照片，缺少一个按时间维度集中浏览当前地区照片的入口。社区功能提供时间轴视图，让用户以"刷动态"的方式浏览附近的时光记忆，增强社区感和内容发现能力。

## 2. 入口

首页底部悬浮栏左侧"社区"按钮（icon-community.svg），点击进入社区页面。

## 3. 核心功能

### 3.1 时间轴照片流

- 以当前地图中心点为基准，展示附近地区的所有照片
- 按拍摄日期（photoDate）倒序排列，最新的在最上面
- 滑动分页（无限滚动），每次加载 20 条，滑到底部自动加载下一页
- 同一天的照片归组在同一个日期标题下

### 3.2 照片卡片

每张照片展示为一个卡片，包含：
- 照片图片（宽度撑满，高度自适应，最大高度限制）
- 拍摄地点名称
- 拍摄日期
- 上传者昵称 + 头像
- 点击照片进入详情页（复用现有 detail 页面）

### 3.3 日期分组标题

- 格式："2026年3月5日 · 星期四"
- 今天显示"今天"，昨天显示"昨天"
- 固定吸顶效果（滚动时当前日期标题吸在顶部）

### 3.4 空状态

- 附近没有照片时显示："这片区域还没有时光记忆，去上传第一张吧"

### 3.5 下拉刷新

- 支持下拉刷新，重新从第一页加载

## 4. 数据来源

- 复用现有的附近照片查询逻辑，新增分页参数
- 接口：`GET /api/photo/community`
- 参数：
  - `latitude`：中心纬度
  - `longitude`：中心经度
  - `radius`：搜索半径（默认 10km）
  - `page`：页码（从 1 开始）
  - `size`：每页条数（默认 20）
- 返回：
  - `list`：照片列表（含上传者信息）
  - `total`：总数
  - `hasMore`：是否有下一页

## 5. 页面结构

```
pages/community/community
├── community.js
├── community.json
├── community.wxml
└── community.wxss
```

## 6. 交互细节

| 场景 | 行为 |
|------|------|
| 进入页面 | 使用地图页传入的经纬度加载第一页 |
| 滑到底部 | 自动加载下一页，底部显示"加载中..." |
| 没有更多 | 底部显示"没有更多了" |
| 下拉刷新 | 重置页码，重新加载 |
| 点击照片 | 跳转详情页（传 id） |
| 点击用户头像/昵称 | 暂不处理（后续可跳转用户主页） |
| 返回 | 返回地图首页 |

## 7. 接口设计

### 后端新增

- `PhotoController`：新增 `GET /api/photo/community` 接口
- `PhotoService`：新增 `findCommunity(lat, lng, radius, page, size)` 方法
- `PhotoMapper`：新增分页查询 SQL（按 photo_date DESC, id DESC 排序）
- `WebConfig`：放行 `/api/photo/community`（公开接口，无需登录）

### 返回数据结构

```json
{
  "code": 200,
  "data": {
    "list": [
      {
        "id": 1,
        "imageUrl": "https://...",
        "thumbnailUrl": "https://...",
        "photoDate": "2026-03-05",
        "locationName": "武昌区黄鹤楼",
        "description": "",
        "nickname": "用户A",
        "avatarUrl": "https://...",
        "latitude": 30.5443,
        "longitude": 114.3022
      }
    ],
    "total": 156,
    "hasMore": true
  }
}
```

## 8. 不做的事情（本期）

- 点赞、评论、收藏
- 用户主页
- 照片举报
- 按标签/分类筛选
- 搜索功能

## 9. 技术要点

- 前端使用 `scroll-view` + `lower-threshold` 触发加载更多
- 开启页面级下拉刷新（`enablePullDownRefresh`）
- 图片使用 `thumbnailUrl` 展示列表，点击后详情页用 `imageUrl`
- 日期分组在前端处理（后端返回扁平列表，前端按 photoDate 分组）
- 需要注册新页面到 `app.json`
