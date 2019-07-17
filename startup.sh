#!/bin/bash
set -e

echo '开始启动项目.....'
nohup java -jar /app.jar --spring.profiles.active=default