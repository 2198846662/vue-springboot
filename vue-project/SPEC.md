# 旅游景点平台 - 设计规范

## 1. 概念与愿景

一个专注于**发现与灵感**的旅游景点探索平台。不是冰冷的预订工具，而是一本活着的旅行杂志——用编辑的眼光挑选每一处风景，用精致排版呈现每一段故事。用户打开这个网站，就像翻开一本精心制作的目的地画册，忍不住想要收藏、规划、出发。

**用户应该记住的：** "这里的美景，让我种草每一处想去的地方。"

## 2. 设计语言

### 2.1 美学方向：杂志编辑风 (Editorial Magazine)

参考《National Geographic Traveler》与《Condé Nast Traveler》的视觉语言：
- 高对比度的图文关系
- 大量留白创造呼吸感
- 精致而大胆的排版层次
- 不规则布局打破机械感

### 2.2 色彩系统

```css
/* 主色 - 深森林绿 */
--primary: #1B4332;
--primary-hover: #2D6A4F;
--primary-light: #40916C;

/* 强调色 - 暖橘/赤陶色 */
--accent: #E76F51;
--accent-hover: #D35400;
--accent-light: #F4A261;

/* 辅助色 - 沙金色 */
--gold: #D4A373;

/* 背景色 - 温暖米白 */
--bg-primary: #FAFAF8;
--bg-secondary: #F5F3EF;
--bg-card: #FFFFFF;
--bg-elevated: #FFFFFF;

/* 文字色 - 深炭色 */
--text-primary: #1C1C1C;
--text-secondary: #4A4A4A;
--text-muted: #8C8C8C;

/* 边框 */
--border: #E5E2DD;
--border-subtle: #F0EDE8;

/* 阴影 */
--shadow-sm: 0 2px 8px rgba(0,0,0,0.06);
--shadow: 0 4px 16px rgba(0,0,0,0.08);
--shadow-lg: 0 12px 32px rgba(0,0,0,0.12);
```

### 2.3 字体系统

- **标题字体：** Playfair Display（衬线体，优雅有力）
- **正文字体：** Noto Sans SC（中文）/ Inter（英文）
- **辅助字体：** Source Code Pro（数字、标签）

```css
/* 字号层级 */
--text-xs: 0.75rem;    /* 12px - 标签 */
--text-sm: 0.875rem;   /* 14px - 辅助信息 */
--text-base: 1rem;     /* 16px - 正文 */
--text-lg: 1.125rem;   /* 18px - 卡片标题 */
--text-xl: 1.5rem;     /* 24px - 区块标题 */
--text-2xl: 2rem;      /* 32px - 页面标题 */
--text-3xl: 3rem;      /* 48px - Hero标题 */
```

### 2.4 空间系统

```css
--space-xs: 0.5rem;   /* 8px */
--space-sm: 1rem;     /* 16px */
--space-md: 1.5rem;   /* 24px */
--space-lg: 2rem;     /* 32px */
--space-xl: 3rem;     /* 48px */
--space-2xl: 4rem;    /* 64px */

--radius-sm: 4px;
--radius: 8px;
--radius-lg: 16px;
--radius-xl: 24px;
```

### 2.5 动效设计

- **过渡时长：** 快速交互 150ms，页面切换 300ms，装饰动效 600ms
- **缓动函数：** `cubic-bezier(0.4, 0, 0.2, 1)` 标准 / `cubic-bezier(0.34, 1.56, 0.64, 1)` 弹性
- **图片悬停：** scale(1.03) + 阴影加深
- **卡片悬停：** translateY(-4px) + 边框高亮
- **页面加载：** 渐入 + 微上移，stagger 100ms

### 2.6 视觉资产

- **图标库：** Lucide Icons（线条简洁，与编辑风契合）
- **图片策略：** Unsplash 高质量风景摄影，16:9 或 4:3 比例
- **装饰元素：** 细线分隔、圆点标记、手绘风箭头

## 3. 布局与结构

### 3.1 首页布局 - 地图+列表混合

```
┌─────────────────────────────────────────────────────────┐
│  Navbar (Logo + 导航 + 用户)                              │
├─────────────────────────────────────────────────────────┤
│  Hero Banner (全宽精选景点，大标题 + 搜索)                  │
├───────────────────────────────┬─────────────────────────┤
│                               │                          │
│   地图区域 (45%)               │   景点列表 (55%)          │
│   - Leaflet 交互地图           │   - 分类筛选栏            │
│   - 景点标记点                 │   - 瀑布流/列表切换        │
│   - 点击跳转详情               │   - 无限滚动加载           │
│                               │                          │
└───────────────────────────────┴─────────────────────────┘
│  Footer                                                 │
└─────────────────────────────────────────────────────────┘
```

### 3.2 响应式策略

- **桌面 (≥1280px)：** 地图+列表并排，完整布局
- **平板 (768-1279px)：** 地图可折叠，列表全宽
- **手机 (<768px)：** 地图置顶（可收起），列表为主

## 4. 功能与交互

### 4.1 核心功能

#### 景点列表 (GET /api/scenery)
- 分页加载（每页 12 条）
- 按分类筛选
- 搜索（标题/描述模糊匹配）
- 排序（最新/最热/评分）

#### 景点详情 (GET /api/scenery/{id})
- 沉浸式大图轮播
- 景点介绍（富文本）
- 基本信息（位置、开放时间、门票）
- 用户评价列表
- 收藏按钮

#### 收藏功能 (POST /api/scenery/{id}/favorite)
- 点击心形图标切换
- 收藏时动画反馈
- 登录用户专属

#### 评论功能 (GET/POST /api/scenery/{id}/comments)
- 评论列表展示
- 添加评论（需登录）
- 评论时间相对显示

### 4.2 交互细节

| 元素 | 默认状态 | Hover 状态 | Active 状态 |
|------|---------|-----------|------------|
| 景点卡片 | 白底 + 细边框 | 上浮 + 阴影加深 + 图片放大 | 边框变为accent色 |
| 收藏按钮 | 空心心形 | 填充半透明 | 填充红色 + 弹出动画 |
| 分类标签 | 描边样式 | 背景填充 | 选中态（accent色） |
| 地图标记 | 绿色圆点 | 放大 + 显示名称 | 打开详情卡片 |

### 4.3 状态处理

- **加载中：** 骨架屏（card 形状 + 渐变动画）
- **空状态：** 插画 + "暂无景点" 文案
- **错误状态：** 重试按钮 + 友好提示
- **网络错误：** 离线提示 + 本地缓存展示

## 5. 组件清单

### 5.1 Navbar
- Logo（地球仪图标 + "TravelVista" 文字）
- 导航链接：发现 / 目的地 / 我的收藏
- 搜索入口
- 登录/用户头像下拉菜单
- **状态：** 默认 / 滚动后背景加深 / 移动端汉堡菜单

### 5.2 Hero Banner
- 全宽背景图（精选景点大图）
- 渐变遮罩
- 主标题 + 副标题
- 搜索框（地点 / 关键词）
- **动效：** 背景微动 + 文字依次淡入

### 5.3 MapView
- Leaflet 地图容器
- 自定义绿色标记点
- 悬停显示景点名称 tooltip
- 点击标记展开侧边预览
- **地图瓦片：** OpenStreetMap（免费）

### 5.4 SceneryCard
- 封面图（16:9，圆角）
- 分类标签（左上角）
- 景点名称（标题）
- 地点 + 简短描述
- 评分 + 评价数
- 收藏按钮（右下角）
- **悬停：** 图片放大 + 卡片上浮

### 5.5 CategoryFilter
- 水平滚动的标签列表
- 全部 / 山川 / 海岛 / 古城 / ...
- 选中态：填充背景 + 白色文字

### 5.6 CommentSection
- 评论列表（头像 + 用户名 + 时间 + 内容）
- 加载更多
- 添加评论表单（需登录）
- 空状态处理

### 5.7 FavoriteButton
- 心形图标
- **状态：** 未收藏（空心）/ 已收藏（实心红）/ 悬停（放大）
- **动画：** 点击时弹出 + 掉落效果

### 5.8 SearchBox
- 搜索图标 + 输入框
- Placeholder："搜索景点、地点..."
- 清除按钮（有内容时显示）
- **聚焦：** 边框高亮 + 展开

### 5.9 SkeletonLoader
- 卡片形状骨架
- 图片区域渐变动画
- 文字区域条状渐变

## 6. 技术实现

### 6.1 技术栈
- **框架：** Vue 3 (Composition API)
- **构建：** Vite
- **样式：** Tailwind CSS + CSS Variables
- **地图：** Leaflet.js
- **图标：** Lucide Vue
- **HTTP：** Fetch API（封装 service 层）

### 6.2 项目结构
```
src/
├── components/
│   ├── Navbar.vue
│   ├── Hero.vue
│   ├── MapView.vue
│   ├── SceneryCard.vue
│   ├── SceneryList.vue
│   ├── CategoryFilter.vue
│   ├── CommentSection.vue
│   ├── FavoriteButton.vue
│   ├── SearchBox.vue
│   ├── SkeletonLoader.vue
│   └── Footer.vue
├── views/
│   ├── Home.vue
│   └── SceneryDetail.vue
├── services/
│   └── api.js
├── data/
│   └── scenery.js (模拟数据)
├── styles/
│   └── variables.css
├── App.vue
└── main.js
```

### 6.3 API 服务层

```javascript
// services/api.js
const BASE_URL = '/api'

export const sceneryService = {
  // 景点列表
  getSceneryList(params) {
    return fetch(`${BASE_URL}/scenery?${new URLSearchParams(params)}`)
  },

  // 景点详情
  getSceneryDetail(id) {
    return fetch(`${BASE_URL}/scenery/${id}`)
  },

  // 分类列表
  getCategories() {
    return fetch(`${BASE_URL}/categories`)
  },

  // 收藏/取消收藏
  toggleFavorite(id) {
    return fetch(`${BASE_URL}/scenery/${id}/favorite`, { method: 'POST' })
  },

  // 获取评论
  getComments(id) {
    return fetch(`${BASE_URL}/scenery/${id}/comments`)
  },

  // 添加评论
  addComment(id, content) {
    return fetch(`${BASE_URL}/scenery/${id}/comments`, {
      method: 'POST',
      body: JSON.stringify({ content })
    })
  }
}
```

### 6.4 数据模型

```javascript
// 景点数据
{
  id: 1,
  title: "黄山风景区",
  description: "中国著名的山岳风景区，以奇松、怪石、云海、温泉著称。",
  location: "安徽省黄山市",
  category: "mountain",
  categoryName: "山川",
  images: ["url1", "url2", "url3"],
  rating: 4.8,
  reviewCount: 1256,
  openingHours: "06:30-17:30",
  ticket: "旺季 230元/淡季 150元",
  latitude: 29.7145,
  longitude: 118.158,
  createdAt: "2024-01-15T08:00:00Z"
}

// 评论数据
{
  id: 1,
  userId: "user123",
  userName: "旅行者小王",
  userAvatar: "W",
  content: "日出云海真的太美了！建议住在山上等日出。",
  createdAt: "2024-03-10T14:30:00Z"
}
```
