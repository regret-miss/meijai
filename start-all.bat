@echo off
chcp 65001 >nul
title 美甲项目一键启动（Redis 自动拉起）

echo ============================================
echo  启动后端 (8082)，Redis 会自动拉起 ...
echo ============================================
set DB_PASSWORD=123456
set VOLC_ARK_API_KEY=%VOLC_ARK_API_KEY%
set NAIL_AI_AESTHETIC_ENABLED=true
set NAIL_AI_AESTHETIC_API_KEY=%NAIL_AI_AESTHETIC_API_KEY%
set LIKEADMIN_WEB_DIRECTORY=G:\desktop\likeadmin_java-master\public
set NAIL_AI_PUBLIC_ENABLED=true
set NAIL_CREATION_BRIDGE_ADMIN_LOGIN_URL=http://127.0.0.1:8082/admin/index.html#/login
set NAIL_CREATION_BRIDGE_PUBLIC_AI_URL=http://127.0.0.1:8082/admin/index.html#/nail/ai
cd /d G:\desktop\likeadmin_java-master\server
java -jar like-admin\target\like-admin-1.0.0.jar
