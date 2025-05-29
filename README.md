# MemoApp - 安卓备忘录应用

## 项目概述

MemoApp是一个简洁高效的安卓备忘录应用，提供用户登录、备忘录管理等功能。应用采用MVVM架构模式设计，使用Room数据库存储备忘录信息，SharedPreferences管理用户登录状态，通过ConstraintLayout和RecyclerView构建现代化UI界面。

## 功能特点

### 用户认证
- 用户名/密码登录系统
- 记住登录状态功能
- 安全登出

### 备忘录管理
- 查看所有备忘录列表
- 创建新备忘录
- 查看/编辑备忘录详情
- 自动保存修改时间

### 用户界面
- 使用ConstraintLayout构建响应式布局
- 使用RecyclerView高效显示备忘录列表
- Material Design设计风格
- 流畅的页面过渡和用户交互

## 技术架构

### MVVM架构
- **Model**: Room数据库实体和数据访问对象
- **View**: XML布局和Activity/Fragment
- **ViewModel**: 连接UI和数据层的中间件

### 主要技术栈
- Kotlin语言
- Jetpack组件库
  - Room: 数据持久化
  - ViewModel: UI状态管理
  - LiveData: 响应式数据更新
- Material Design组件
- ViewBinding: 视图绑定

## 项目结构

```
app/src/main/
├── java/com/silenthink/memoapp/
│   ├── data/
│   │   ├── dao/            # 数据访问对象
│   │   ├── database/       # 数据库配置
│   │   ├── model/          # 数据模型
│   │   └── repository/     # 数据仓库
│   ├── ui/
│   │   ├── screen/         # 界面活动和适配器
│   │   ├── theme/          # 主题样式
│   │   └── viewmodel/      # 视图模型
│   ├── util/               # 工具类
│   └── MainActivity.kt     # 主活动
└── res/
    ├── layout/             # 布局文件
    ├── menu/               # 菜单资源
    └── values/             # 资源文件
```

## 开发路线

### 里程碑 1: 基础架构搭建 ✅
- 项目初始化
- 依赖配置
- MVVM架构设计

### 里程碑 2: 数据层实现 ✅
- Room数据库设计
- 数据模型定义
- DAO接口实现
- 数据仓库封装

### 里程碑 3: 用户界面开发 ✅
- 登录页面实现
- 备忘录列表页面
- 备忘录详情/编辑页面
- 菜单和导航

### 里程碑 4: 功能完善 (进行中)
- 备忘录搜索功能
- 备忘录分类管理
- 用户偏好设置
- UI优化和动画

### 里程碑 5: 测试与发布 (计划中)
- 单元测试
- UI测试
- 性能优化
- 应用发布

## 使用方法

### 安装
1. 克隆仓库到本地
   ```
   git clone https://github.com/yourusername/MemoApp.git
   ```
2. 使用Android Studio打开项目
3. 构建并运行应用

### 登录
- 用户名: `admin`
- 密码: `password`
- 可选择"记住登录状态"以跳过下次登录

### 备忘录操作
- 点击主页面右下角的"+"按钮添加新备忘录
- 点击列表中的备忘录项查看/编辑详情
- 在详情页面修改内容后点击"保存"按钮
- 通过菜单中的"登出"选项退出应用

## 开发环境

- Android Studio Hedgehog | 2023.1.1
- Kotlin 1.9.0
- Gradle 8.0
- minSdkVersion 30
- targetSdkVersion 35

## 项目依赖

- AndroidX Core: 1.12.0
- AndroidX AppCompat: 1.6.1
- Material Components: 1.11.0
- ConstraintLayout: 2.1.4
- RecyclerView: 1.3.2
- Room: 2.6.1
- Lifecycle Components: 2.7.0
- Navigation Components: 2.7.6

## Commit 提交规范

为保持代码提交的一致性和可读性，本项目采用以下commit图标分类：

| 图标 | 类型 | 说明 |
|------|------|------|
| ✨ | feat | 新功能 |
| 🐛 | fix | 修复bug |
| 📝 | docs | 文档更新 |
| 💄 | style | 代码格式修改，非功能性更改 |
| ♻️ | refactor | 代码重构，既不修复bug也不添加新功能 |
| ⚡️ | perf | 性能优化 |
| ✅ | test | 添加或修改测试代码 |
| 🔧 | chore | 构建过程或辅助工具的变动 |
| 🔀 | merge | 合并分支 |
| 🚀 | deploy | 部署相关 |
| 🗃️ | db | 数据库相关变更 |
| 🎨 | ui | 用户界面和用户体验相关 |
| 🔒 | security | 安全相关更新 |

### 提交示例

```
✨ feat: 添加用户登录功能
🐛 fix: 修复备忘录列表不刷新的问题
📝 docs: 更新README文档
💄 style: 格式化代码风格
♻️ refactor: 重构数据访问层
⚡️ perf: 优化备忘录列表加载性能
✅ test: 添加登录功能单元测试
🔧 chore: 更新Gradle依赖版本
```
