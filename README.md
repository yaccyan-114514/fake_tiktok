# Fake TikTok - 短视频内容创作应用

一个功能完整的Android短视频内容创作应用，支持图片上传、AI智能文本生成、标签管理、位置定位等功能。

## 📱 项目简介

Fake TikTok 是一个仿 TikTok 风格的内容创作应用，用户可以创建包含图片、文字、标签和位置信息的短视频内容。应用集成了AI文本生成功能，可以根据上传的图片自动生成标题和正文内容。

## ✨ 主要功能

### 1. 内容创作
- **图片管理**
  - 支持从相册选择多张图片
  - 支持拍照获取图片
  - 图片拖拽排序
  - 图片删除功能
  - 图片持久化存储

- **文本编辑**
  - 标题输入（最多100字）
  - 正文输入（最多2000字）
  - 实时字数统计
  - 字数超限颜色提示（灰色/橙色/红色）

### 2. AI智能文本生成
- **一键生成文本**
  - 基于上传图片自动生成标题和正文
  - 使用 Qwen qwen3-vl-flash 视觉语言模型
  - 流式输出显示生成过程
  - 生成内容限制：标题最多100字，正文最多100字
  - 独立的AI生成页面，支持预览和应用生成内容

### 3. 标签管理
- **标签库系统**
  - 标签库（`tag`表）：存储所有可用标签
  - 草稿标签（`draft_tag`表）：存储草稿关联的标签
  - 标签搜索和选择
  - 自定义标签添加
  - 标签以Chip形式显示，支持删除

### 4. 社交功能
- **@好友功能**
  - 好友列表管理
  - 在正文中@好友
  - 好友分组功能

### 5. 位置服务
- **高德地图定位**
  - GPS定位
  - 网络定位
  - 地址反编码
  - 位置信息显示和保存

### 6. 草稿管理
- **自动保存草稿**
  - 退出时提示保存草稿
  - 自动加载最新草稿
  - 草稿包含：标题、正文、图片、标签、@好友、位置信息

## 🏗️ 技术架构

### 开发环境
- **语言**: Java
- **最低SDK版本**: Android 8.0 (API 28)
- **目标SDK版本**: Android 14 (API 36)
- **编译SDK版本**: 36
- **构建工具**: Gradle (Kotlin DSL)

### 核心依赖
- **AndroidX Libraries**
  - AppCompat
  - Material Components
  - RecyclerView
  - CardView
  - ConstraintLayout

- **第三方SDK**
  - 高德地图定位SDK (v6.5.1)
  - Google Play Services Location

- **AI服务**
  - Qwen API (qwen3-vl-flash模型)
  - Base URL: `https://dashscope.aliyuncs.com/compatible-mode/v1/chat/completions`

### 数据库设计
- **SQLite数据库**
  - `draft` - 草稿主表
  - `draft_text` - 草稿文本内容
  - `draft_image` - 草稿图片URI
  - `draft_tag` - 草稿标签关联
  - `draft_mention` - 草稿@好友关联
  - `draft_location` - 草稿位置信息
  - `tag` - 标签库
  - `friend` - 好友列表
  - `group` - 好友分组
  - `group_friend` - 分组好友关联

## 📂 项目结构

```
app/src/main/
├── java/com/example/fake_tiktok/
│   ├── MainActivity.java              # 主入口Activity
│   ├── PostActivity.java              # 内容创作主页面
│   ├── AIGenerateActivity.java        # AI文本生成页面
│   ├── QwenActivity.java              # Qwen API调用封装
│   ├── CameraActivity.java            # 相机功能封装
│   ├── LocationActivity.java          # 定位功能封装
│   ├── Draft.java                     # 草稿数据模型
│   ├── DraftDatabaseHelper.java       # 草稿数据库操作
│   ├── ImageAdapter.java              # 图片列表适配器
│   ├── MentionOptionAdapter.java     # @好友/标签选择适配器
│   ├── FileLogger.java                # 文件日志工具
│   └── DAO/                           # 数据访问对象
│       ├── AppDatabase.java           # 应用数据库管理
│       ├── TagDao.java                # 标签DAO
│       ├── FriendDao.java             # 好友DAO
│       ├── GroupDao.java              # 分组DAO
│       └── Draft*.java                # 草稿相关DAO
├── res/
│   ├── layout/                        # 布局文件
│   │   ├── activity_main.xml
│   │   ├── activity_post.xml
│   │   ├── activity_ai_generate.xml
│   │   └── dialog_*.xml
│   ├── drawable/                      # 图片资源
│   ├── values/                        # 资源值
│   └── mipmap-*/                      # 应用图标
└── AndroidManifest.xml                # 应用清单文件
```

## 🔑 核心功能实现

### AI文本生成流程
1. 用户上传图片后点击"一键生成文本"
2. 跳转到 `AIGenerateActivity`
3. 图片转换为Base64编码
4. 调用Qwen API，使用流式输出
5. 实时显示生成内容
6. 生成完成后可应用或取消

### 图片处理流程
1. 用户选择"拍照获取"或"从相册选择"
2. 获取图片URI
3. 申请持久化权限
4. 添加到RecyclerView显示
5. 支持拖拽排序和删除

### 草稿保存流程
1. 用户编辑内容时自动标记为已更改
2. 退出时提示保存草稿
3. 保存标题、正文、图片URI、标签、@好友、位置信息
4. 下次打开自动加载最新草稿

## 🎨 UI设计

### 颜色方案
- **背景色**: `#000000` (黑色)
- **文本框背景**: `#000000` (黑色)
- **文本框文字**: `#FFFFFF` (白色)
- **主要按钮**: `#FF1493` (粉色)
- **次要按钮**: `#808080` (灰色)
- **按钮文字**: `#FFFFFF` (白色)

### 按钮样式
- **发布按钮**: 粉色背景，白色文字
- **一键生成文本按钮**: 粉色背景，白色图标和文字
- **标签/@好友/位置按钮**: 灰色背景，白色图标和文字

## 🔐 权限说明

应用需要以下权限：
- **存储权限**: 读取图片（Android 13+使用READ_MEDIA_IMAGES）
- **相机权限**: 拍照功能
- **位置权限**: GPS和网络定位
- **网络权限**: AI API调用和定位服务

## 🚀 快速开始

### 环境要求
- Android Studio Hedgehog | 2023.1.1 或更高版本
- JDK 11 或更高版本
- Android SDK 28+

### 安装步骤

1. **克隆项目**
   ```bash
   git clone <repository-url>
   cd fake_tiktok
   ```

2. **配置API密钥**
   - 打开 `app/src/main/java/com/example/fake_tiktok/QwenActivity.java`
   - 修改 `API_KEY` 为你的Qwen API密钥
   - 高德地图API Key已在 `AndroidManifest.xml` 中配置

3. **构建项目**
   ```bash
   ./gradlew build
   ```

4. **运行应用**
   - 在Android Studio中打开项目
   - 连接Android设备或启动模拟器
   - 点击运行按钮

## 📝 使用说明

### 创建内容
1. 点击主界面的"投稿"按钮
2. 添加图片（拍照或从相册选择）
3. 输入标题和正文，或使用"一键生成文本"功能
4. 添加标签和@好友
5. 选择位置（可选）
6. 点击"发布"按钮

### AI文本生成
1. 上传至少一张图片
2. 点击"一键生成文本"按钮
3. 等待AI生成完成（流式显示）
4. 预览生成内容
5. 点击"应用"将内容填入编辑框，或点击"取消"放弃

### 管理标签
1. 点击"标签"按钮
2. 搜索已有标签或输入新标签
3. 从列表选择或点击"确定"添加自定义标签
4. 标签会以Chip形式显示，点击×可删除

### 保存草稿
- 编辑内容后退出会自动提示保存草稿
- 下次打开应用会自动加载最新草稿
- 发布内容后会自动删除草稿

## 🗄️ 数据库说明

### 数据库文件位置
- 草稿数据库: `/data/data/com.example.fake_tiktok/databases/drafts.db`
- 应用数据库: `/data/data/com.example.fake_tiktok/databases/app.db`

### 数据库版本
- 草稿数据库版本: 1
- 应用数据库版本: 2

### 表结构
- `tag` 表：标签库，存储所有可用标签
- `draft_tag` 表：草稿标签关联表
- `friend` 表：好友列表
- `group` 表：好友分组
- `group_friend` 表：分组好友关联

## 🔧 配置说明

### Qwen API配置
- **API Base URL**: `https://dashscope.aliyuncs.com/compatible-mode/v1/chat/completions`
- **模型**: `qwen3-vl-flash`
- **API Key**: 在 `QwenActivity.java` 中配置
- **Max Tokens**: 300（限制生成长度）

### 高德地图配置
- **API Key**: `69e95ce9ceb96f19effc13a62faed28f`
- **SDK版本**: 6.5.1
- **功能**: 仅定位服务

## 📱 支持的Android版本

- **最低支持**: Android 8.0 (API 28)
- **目标版本**: Android 14 (API 36)
- **测试版本**: Android 8.0 - Android 14

## 🐛 已知问题

1. 图片权限在某些Android版本可能需要手动授权
2. 定位功能需要设备开启GPS或网络定位
3. AI生成功能需要网络连接

## 📄 许可证

本项目仅供学习和研究使用。

## 👥 贡献

欢迎提交Issue和Pull Request。

## 📞 联系方式

如有问题或建议，请通过Issue反馈。

---

**注意**: 本项目中的API密钥仅用于演示，生产环境请使用环境变量或配置文件管理密钥。

