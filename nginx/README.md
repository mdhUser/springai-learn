# Spring AI RAG 智能助手前端

一个基于 Spring AI 的 RAG（检索增强生成）智能助手的现代化 Web 前端界面。

## 🌟 功能特性

### 💬 智能对话
- 支持多种 AI 模型选择（DeepSeek、Qwen、Llama 等）
- 实时流式对话体验
- 支持 Markdown 格式渲染
- 清空对话历史功能

### 📚 RAG 知识库问答
- 基于上传文档的智能问答
- 支持多个知识库管理
- 知识库选择器，可选择特定知识库进行问答
- 自动刷新知识库列表

### 📁 文档上传管理
- 支持多种文档格式（PDF、DOC、DOCX、TXT、MD）
- 拖拽上传支持
- 实时上传进度显示
- 知识库标签管理

### 🎨 现代化界面
- 响应式设计，支持移动端
- 美观的 Material Design 风格
- 流畅的动画效果
- 直观的用户体验

## 🚀 快速开始

### 前置要求
- Docker 已安装并运行
- Spring Boot 后端服务运行在 8090 端口

### 启动服务

1. **使用启动脚本（推荐）**
   ```bash
   cd nginx
   ./start.sh
   ```

2. **手动启动**
   ```bash
   cd nginx
   docker run -d \
     --name springai-frontend \
     -p 3000:80 \
     -v "$(pwd)/html:/usr/share/nginx/html" \
     -v "$(pwd)/nginx.conf:/etc/nginx/nginx.conf" \
     nginx:alpine
   ```

### 访问应用
- 前端界面: http://localhost:3000
- 后端API: http://localhost:8090

## 📁 项目结构

```
nginx/
├── html/                    # 前端静态文件
│   ├── index.html          # 主页面
│   ├── css/
│   │   └── style.css       # 样式文件
│   └── js/
│       └── app.js          # 应用逻辑
├── nginx.conf              # Nginx 配置
├── docker-compose.yml      # Docker Compose 配置
├── start.sh               # 启动脚本
└── README.md              # 说明文档
```

## 🔧 配置说明

### API 端点配置
在 `js/app.js` 中修改 API 基础地址：
```javascript
const API_BASE_URL = 'http://localhost:8090';
```

### Nginx 代理配置
在 `nginx.conf` 中配置后端代理：
```nginx
location /api/ {
    proxy_pass http://host.docker.internal:8090;
    # ... 其他配置
}
```

## 🎯 使用指南

### 1. 智能对话
1. 选择 AI 模型
2. 在输入框中输入问题
3. 按 Enter 发送消息
4. 查看 AI 实时回复

### 2. RAG 问答
1. 首先上传文档创建知识库
2. 在聊天界面选择知识库
3. 提问时 AI 会基于文档内容回答

### 3. 文档上传
1. 切换到"文档上传"标签
2. 输入知识库名称
3. 选择或拖拽文件到上传区域
4. 点击"开始上传"

### 4. 知识库管理
1. 切换到"知识库管理"标签
2. 查看所有已创建的知识库
3. 使用"刷新列表"更新显示

## 🛠️ 开发说明

### 技术栈
- **前端**: HTML5, CSS3, JavaScript (ES6+)
- **样式**: CSS Variables, Flexbox, Grid
- **服务器**: Nginx
- **容器**: Docker

### 核心功能实现
- **流式响应**: 使用 Fetch API 的 ReadableStream
- **文件上传**: FormData + 拖拽 API
- **响应式设计**: CSS Media Queries
- **状态管理**: 原生 JavaScript

### API 集成
- `GET /api/v1/ollama/generate_stream` - 普通对话
- `GET /api/v1/ollama/generate_stream_rag` - RAG 问答
- `GET /api/v1/rag/query_rag_tag_list` - 获取知识库列表
- `POST /api/v1/rag/upload_file` - 上传文档

## 🔍 故障排除

### 常见问题

1. **前端无法访问**
   - 检查 Docker 是否运行
   - 确认端口 3000 未被占用
   - 查看容器日志: `docker logs springai-frontend`

2. **API 请求失败**
   - 确认后端服务运行在 8090 端口
   - 检查 CORS 配置
   - 查看浏览器开发者工具的网络面板

3. **文件上传失败**
   - 检查文件大小限制（默认 100MB）
   - 确认文件格式支持
   - 查看后端错误日志

### 调试命令
```bash
# 查看容器状态
docker ps

# 查看前端日志
docker logs -f springai-frontend

# 进入容器调试
docker exec -it springai-frontend sh

# 重启服务
docker restart springai-frontend
```

## 📝 更新日志

### v1.0.0 (2024-12-08)
- ✨ 初始版本发布
- 💬 智能对话功能
- 📚 RAG 知识库问答
- 📁 文档上传管理
- 🎨 现代化界面设计

## 🤝 贡献

欢迎提交 Issue 和 Pull Request！

## 📄 许可证

MIT License