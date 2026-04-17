# WorldThings News - Vue3 项目

一个基于 Vue3 + Tailwind CSS 的国际新闻网站。

## 目录结构

```
vue-project/
├── index.html              # 入口 HTML 文件
├── package.json            # 项目依赖配置
├── vite.config.js          # Vite 配置文件
├── tailwind.config.js      # Tailwind CSS 配置
├── postcss.config.js       # PostCSS 配置
├── README.md               # 项目说明文档
├── src/
│   ├── main.js            # 应用入口文件
│   ├── App.vue            # 根组件
│   ├── style.css          # 全局样式
│   ├── components/        # 组件目录
│   │   ├── Navbar.vue     # 导航栏组件（主题切换）
│   │   ├── Hero.vue       # Hero 区域（标题+搜索）
│   │   ├── Carousel.vue   # 轮播图组件
│   │   ├── Categories.vue # 分类筛选组件
│   │   ├── NewsGrid.vue   # 新闻网格组件
│   │   └── Footer.vue     # 页脚组件
│   └── data/
│       └── news.js        # 新闻数据和分类数据
```

## 组件说明

| 组件 | 功能 |
|------|------|
| **Navbar** | 顶部导航栏，包含 Logo 和主题切换按钮 |
| **Hero** | Hero 区域，包含网站标题、副标题和搜索框 |
| **Carousel** | 轮播图组件，2秒自动切换，支持手动控制 |
| **Categories** | 分类筛选按钮组，使用 v-for 渲染 |
| **NewsGrid** | 新闻卡片网格，支持搜索和分类筛选，使用 v-for 渲染 |
| **Footer** | 页脚组件，使用 v-for 渲染链接列表 |

## 技术栈

- **Vue 3** - 渐进式 JavaScript 框架
- **Vite** - 下一代前端构建工具
- **Tailwind CSS** - 原子化 CSS 框架
- **Vue Composition API** - `<script setup>` 语法

## 安装和运行

```bash
# 进入项目目录
cd vue-project

# 安装依赖
npm install

# 启动开发服务器
npm run dev

# 构建生产版本
npm run build

# 预览生产版本
npm run preview
```

## 特性

- 响应式设计，支持移动端和桌面端
- 深色/浅色主题切换
- 新闻分类筛选
- 模糊搜索功能
- 轮播图自动播放
- 毛玻璃效果（Glassmorphism）
- 渐变动画背景

## 颜色方案

- 主色：`#0EA5E9`（天蓝色）
- 辅色：`#06B6D4`（青色）
- 强调色：`#10B981`（翠绿色）
