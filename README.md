# 个人博客系统

> Spring Boot 3 + MyBatis-Plus + Redis 全栈个人博客，Docker 容器化部署 Railway 云平台。

## 🏗️ 技术架构

```
┌─────────────────────────────────────────────────┐
│                   前端层                          │
│   Thymeleaf 模板 + Vue 3 CDN (SPA)               │
│   marked.js Markdown 渲染 + highlight.js 代码高亮  │
└──────────────────┬──────────────────────────────┘
                   │ REST API (JSON)
┌──────────────────▼──────────────────────────────┐
│                  Controller 层                    │
│   柔性 JWT 认证拦截器 → UserContext (ThreadLocal)  │
│   @RestControllerAdvice 全局异常处理               │
└──────────────────┬──────────────────────────────┘
                   │
┌──────────────────▼──────────────────────────────┐
│                  Service 层                       │
│   文章 / 评论 / 用户 / 分类 / 标签 / 点赞收藏       │
│   BCrypt 密码加密    Redis 点赞计数 + 定时同步       │
└──────────────────┬──────────────────────────────┘
                   │
┌──────────────────▼──────────────────────────────┐
│                  数据层                           │
│   MySQL 8.0 (MyBatis-Plus ORM)                   │
│   Redis (缓存 / 点赞收藏 / 反向索引)                │
└─────────────────────────────────────────────────┘
```

## 🛠️ 技术栈

| 类别 | 技术 | 版本 | 选型理由 |
|------|------|:---:|------|
| 框架 | Spring Boot | 3.3.5 | 主流企业级框架，生态完善 |
| 语言 | Java | 17 | LTS 长期支持版本 |
| ORM | MyBatis-Plus | 3.5.7 | 简化 CRUD，保留 SQL 灵活性 |
| 数据库 | MySQL | 8.0 | 关系型数据，社区成熟 |
| 缓存 | Redis | — | 高性能读写，点赞计数实时性 |
| 安全 | BCrypt + JJWT | 6.2.3 / 0.12.6 | 密码不可逆加密 + 无状态 Token |
| 模板 | Thymeleaf + Vue 3 | CDN | 服务端渲染路由 + 客户端响应式 |
| 文档 | SpringDoc OpenAPI | 2.6.0 | Swagger UI 自动生成接口文档 |
| 构建 | Maven + Docker | — | 多阶段构建，镜像精简 |
| 部署 | Railway | — | Git push 自动部署，零运维 |

## ✨ 已实现功能

### 用户系统
- 注册 / 登录（BCrypt 加密 + JWT Token）
- 个人信息查看 / 编辑（昵称、头像）
- 柔性认证拦截器：有 Token 自动解析，无 Token 静默放行

### 文章系统
- Markdown 编辑器 + 实时预览 + 代码高亮
- 发布 / 编辑 / 删除（仅作者本人可操作）
- 分类筛选、关键词搜索、标签云
- 封面图上传（拖拽支持，UUID 防重名）
- 浏览量统计

### 评论系统
- 多级嵌套回复（递归树结构）
- 逻辑删除（MyBatis-Plus @TableLogic）
- 删除父评论自动级联删除子评论

### 点赞收藏系统（Redis 方案）
- Redis Set 存储点赞/收藏用户 → 原子操作，高性能
- Redis Hash 存储实时计数 → O(1) 读取
- 反向索引 `user:like:{userId}` → 支持"我赞过的文章"
- `@Scheduled` 每小时同步 Redis → MySQL，Redis 不可用自动降级读 DB

### 个人主页
- 4 Tab：个人信息 / 我的文章 / 赞过的文章 / 收藏的文章
- 在线编辑昵称和头像

## 🚀 本地运行

```bash
# 1. 确保本地安装了 MySQL 8.0 和 Redis
# 2. 执行建库脚本（在 MySQL 中执行）
source src/main/resources/db/init.sql

# 3. 启动项目
./mvnw spring-boot:run

# 4. 访问
http://localhost:8080              # 首页
http://localhost:8080/swagger-ui/index.html  # API 文档

# 5. 默认管理员账号
# 用户名: admin  密码: admin123
```

## 🐳 Docker 部署

```bash
docker build -t my-blog .
docker run -p 8080:8080 \
  -e MYSQL_URL=mysql://root:password@host:3306/blog \
  -e MYSQLUSER=root \
  -e MYSQLPASSWORD=password \
  -e MYSQLHOST=host \
  -e MYSQLPORT=3306 \
  -e REDISHOST=redis-host \
  -e REDISPORT=6379 \
  -e REDISPASSWORD= \
  -e JWT_SECRET=your-secret-key-32-chars-long-!! \
  my-blog
```

## 📂 项目结构

```
src/main/java/com/gyc/blog/
├── BlogApplication.java          # 启动类
├── common/                       # 公共层
│   ├── JwtUtil.java              #   JWT 生成/解析
│   ├── JwtInterceptor.java       #   认证拦截器（柔性模式）
│   ├── RedisUtil.java            #   Redis 操作封装
│   ├── Result.java               #   统一响应体
│   └── UserContext.java          #   ThreadLocal 用户上下文
├── config/                       # 配置
│   ├── MyBatisPlusConfig.java    #   分页插件
│   ├── WebConfig.java            #   静态资源映射 + CORS
│   └── SwaggerConfig.java        #   OpenAPI 文档配置
├── entity/                       # 数据库实体
│   ├── Article.java / User.java
│   ├── Comment.java (含 @TableLogic)
│   ├── Category.java / Tag.java / ArticleTag.java
│   └── vo/                       #   视图对象 (VO)
│       ├── ArticleVO.java / CommentVO.java / UserVO.java
├── mapper/                       # MyBatis Mapper
│   ├── ArticleMapper.java        #   + ArticleMapper.xml (动态 SQL)
│   └── CommentMapper.java        #   注解 SQL (嵌套查询)
├── service/                      # 业务层
│   ├── ArticleService / UserService / LikeService ...
│   └── impl/                     #   实现类
│       ├── ArticleServiceImpl    #     文章 CRUD + 权限校验
│       ├── CommentServiceImpl    #     评论树递归构建
│       ├── LikeServiceImpl       #     Redis 点赞收藏 + 定时同步
│       └── UserServiceImpl       #     BCrypt 注册/登录
├── controller/                   # 控制器
│   ├── ArticleController.java    #   REST (/api/article/*)
│   ├── UserController.java       #   REST (/api/user/*)
│   ├── LikeController.java       #   REST (/api/like/*)
│   ├── CommentController.java    #   REST (/api/comment/*)
│   └── PageController.java       #   Thymeleaf 页面路由
└── exception/                    # 异常处理
    ├── BusinessException.java
    └── GlobalExceptionHandler.java

src/main/resources/
├── application.yml               # 主配置 (环境变量兼容)
├── db/init.sql                   # 建库脚本 + 种子数据
├── mapper/ArticleMapper.xml      # MyBatis XML
├── templates/                    # Thymeleaf 页面
│   ├── index.html / detail.html / publish.html
│   ├── edit.html / login.html / register.html
│   └── profile.html
└── static/
    ├── css/style.css             # 全局样式 (含暗色模式)
    ├── js/common.js              # 公共工具 (API/Toast/日期格式化)
    └── img/default-bg.svg        # 文章默认封面
```

## 🔑 关键设计决策

### 1. JWT 柔性认证
拦截器解析 Token 但不阻断请求。需要登录的接口由 Controller 自行判断 `UserContext.getUserId()`，未登录返回 401。这避免了无差别拦截对首页、文章详情等公开页面造成的额外登录跳转。

### 2. Redis 点赞计数 + 定时同步
- **高性能**：点赞/收藏读写走 Redis Set + Hash，O(1) 操作
- **数据安全**：`@Scheduled` 每小时同步到 MySQL，Redis 故障时自动降级读 DB
- **用户维度查询**：`user:like:{userId}` 反向索引 Set，避免全表扫描

### 3. 环境变量驱动的配置
`application.yml` 使用 `${ENV:默认值}` 格式，同一份配置兼容本地开发和 Railway 部署，无需 profile 切换。

### 4. 评论递归树
评论平铺存储（`parent_id` 关联），读取时递归组装为树结构 + `hasChildren` 标志，既保留 SQL 查询效率又方便前端渲染。

## 📡 API 文档

启动后访问 `http://localhost:8080/swagger-ui/index.html` 查看完整 Swagger API 文档。

快速测试可使用项目根目录的 `test-api.http`（IntelliJ HTTP Client 格式，含 22 个测试用例）。

## 🌐 部署

项目部署在 [Railway](https://railway.app) 云平台：
- Git push 到 `main` 分支自动触发 Docker 构建和部署
- MySQL + Redis 均由 Railway 插件提供，环境变量自动注入
- Dockerfile 使用多阶段构建（Maven 编译 + JRE 运行），最终镜像精简
