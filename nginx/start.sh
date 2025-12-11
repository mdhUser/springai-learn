#!/bin/bash

# Spring AI RAG 前端启动脚本

echo "🚀 启动 Spring AI RAG 前端服务..."

# 检查Docker是否运行
if ! docker info > /dev/null 2>&1; then
    echo "❌ Docker 未运行，请先启动 Docker"
    exit 1
fi

# 停止现有容器
echo "🛑 停止现有容器..."
docker-compose down

# 启动nginx前端服务
echo "🌐 启动 Nginx 前端服务..."
docker run -d \
  --name springai-frontend \
  --restart unless-stopped \
  -p 3000:80 \
  -v "$(pwd)/html:/usr/share/nginx/html" \
  -v "$(pwd)/nginx.conf:/etc/nginx/nginx.conf" \
  nginx:alpine

# 检查容器状态
if docker ps | grep -q springai-frontend; then
    echo "✅ 前端服务启动成功！"
    echo ""
    echo "📱 访问地址："
    echo "   前端界面: http://localhost:3000"
    echo "   后端API:  http://localhost:8090"
    echo ""
    echo "🔧 管理命令："
    echo "   查看日志: docker logs -f springai-frontend"
    echo "   停止服务: docker stop springai-frontend"
    echo "   删除容器: docker rm -f springai-frontend"
    echo ""
    echo "🎉 享受使用 Spring AI RAG 智能助手！"
else
    echo "❌ 前端服务启动失败，请检查错误信息"
    docker logs springai-frontend
    exit 1
fi