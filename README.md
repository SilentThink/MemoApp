# MemoApp - 智能安卓备忘录应用

## 项目概述

MemoApp是一个功能丰富的智能安卓备忘录应用，集成了AI智能分类功能，提供用户登录、备忘录管理、智能分类等功能。应用采用MVVM架构模式设计，使用Room数据库存储备忘录信息，集成DeepSeek AI API实现智能分类，通过现代化UI设计提供优秀的用户体验。

## 功能特点

### 🔐 用户认证
- 用户登录/注册
- 记住登录状态
- 安全登出

### 📝 备忘录管理
- 创建、编辑、删除备忘录
- 备忘录列表查看
- 图片附件支持
- 分类标签管理
- 优先级设置
- 全文搜索功能
- 多种排序方式(时间、优先级、标题)

### 🤖 AI智能功能
- DeepSeek API集成
- 智能内容分类建议
- AI分类置信度显示
- 分类理由说明
- API密钥配置管理

### 💾 数据备份
- 完整数据备份创建
- 备份文件管理
- 数据恢复功能
- 备份详情查看

## 技术架构

### MVVM架构
- **Model**: Room数据库实体和数据访问对象
- **View**: XML布局、Activity/Fragment + Jetpack Compose
- **ViewModel**: 连接UI和数据层的中间件

### 主要技术栈
- **开发语言**: Kotlin
- **UI框架**: 
  - 传统View系统 (XML布局)
  - Jetpack Compose (现代声明式UI)
- **架构组件**:
  - Room: 数据持久化
  - ViewModel: UI状态管理
  - LiveData: 响应式数据更新
  - Navigation Components: 页面导航
- **网络请求**: 
  - Retrofit2: HTTP客户端
  - OkHttp3: 网络拦截和日志
  - Gson: JSON序列化
- **图片处理**: Glide
- **AI服务**: DeepSeek API
- **异步处理**: Kotlin Coroutines

## 项目结构

```
app/src/main/
├── java/com/silenthink/memoapp/
│   ├── data/
│   │   ├── api/            # API接口定义
│   │   │   └── DeepSeekApiService.kt
│   │   ├── dao/            # 数据访问对象
│   │   ├── database/       # 数据库配置
│   │   ├── model/          # 数据模型
│   │   ├── repository/     # 数据仓库
│   │   └── service/        # 业务服务层
│   │       └── AiCategoryService.kt
│   ├── ui/
│   │   ├── screen/         # 界面活动和适配器
│   │   ├── viewmodel/      # 视图模型
│   │   ├── theme/          # 主题样式
│   │   ├── helper/         # UI辅助类
│   │   └── util/           # UI工具类
│   ├── util/               # 通用工具类
│   └── MainActivity.kt     # 主活动
└── res/
    ├── layout/             # 布局文件
    ├── menu/               # 菜单资源
    ├── drawable/           # 图片资源
    ├── anim/               # 动画资源
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

### 里程碑 4: AI功能集成 ✅
- DeepSeek AI API集成
- 智能分类服务实现
- AI分类功能界面
- 网络请求优化

### 里程碑 5: 功能完善 (进行中)
- 备忘录搜索优化
- 用户偏好设置
- 数据备份与恢复
- 性能优化

### 里程碑 6: 测试与发布 (计划中)
- 单元测试
- UI测试
- 性能测试
- 应用发布

## 使用方法

### 安装
1. 克隆仓库到本地
   ```bash
   git clone https://github.com/yourusername/MemoApp.git
   ```
2. 使用Android Studio打开项目
3. 配置DeepSeek API密钥（如需使用AI功能）
4. 构建并运行应用

### 登录
- 可选择"记住登录状态"以跳过下次登录

### 备忘录操作
- 点击主页面右下角的"+"按钮添加新备忘录
- 点击列表中的备忘录项查看/编辑详情
- 在详情页面修改内容后点击"保存"按钮
- 使用AI分类功能自动为备忘录分类
- 通过菜单中的"登出"选项退出应用

## 开发环境

- **Android Studio**: Hedgehog | 2023.1.1 或更高版本
- **Kotlin**: 1.9.0+
- **Gradle**: 8.11.1
- **Java**: 11
- **minSdkVersion**: 30
- **targetSdkVersion**: 35
- **compileSdkVersion**: 35

## 项目依赖

### 核心依赖
- AndroidX Core KTX: 最新版本
- AndroidX Lifecycle Runtime KTX: 最新版本
- AndroidX Activity Compose: 最新版本

### UI框架
- **Jetpack Compose BOM**: 最新版本
- **传统View系统**:
  - AndroidX AppCompat: 1.6.1
  - Material Components: 1.11.0
  - ConstraintLayout: 2.1.4
  - RecyclerView: 1.3.2
  - CardView: 1.0.0

### 架构组件
- **Navigation Components**: 2.7.6
- **Room Database**: 2.6.1
- **Lifecycle Components**: 2.7.0

### 网络和数据处理
- **Retrofit2**: 2.9.0
- **OkHttp3**: 4.12.0
- **Gson**: 2.10.1
- **Kotlin Coroutines**: 1.7.3

### 图片处理
- **Glide**: 4.16.0

### 测试框架
- JUnit
- AndroidX Test
- Espresso

## AI功能配置

### DeepSeek API设置
1. 获取DeepSeek API密钥
2. 在项目中配置API密钥
3. 确保网络权限已添加到AndroidManifest.xml

### AI分类功能
- 自动分析备忘录内容
- 智能生成分类标签
- 支持自定义分类规则

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

```bash
✨ feat: 添加用户登录功能
🐛 fix: 修复备忘录列表不刷新的问题
📝 docs: 更新README文档
💄 style: 格式化代码风格
♻️ refactor: 重构数据访问层
⚡️ perf: 优化备忘录列表加载性能
✅ test: 添加登录功能单元测试
🔧 chore: 更新Gradle依赖版本
```