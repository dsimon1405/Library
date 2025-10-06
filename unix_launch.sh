#!/bin/bash

# load variables from .env
if [ -f .env ]; then
  export $(grep -v '^#' .env | xargs)
else
  echo ".env файл не найден!"
  exit 1
fi
echo "LIBRARY_DB_PORT=$LIBRARY_DB_PORT"
echo "LIBRARY_DB_USERNAME=$LIBRARY_DB_USERNAME"
echo "LIBRARY_DB_PASSWORD=$LIBRARY_DB_PASSWORD"


rmdir -p unix
mkdir -p unix/logs
mkdir -p unix/pids

echo "Starting eureka-server"
nohup java -jar eureka-server/target/eureka-server-0.0.1-SNAPSHOT.jar > unix/logs/eureka-server.log 2>&1 &
echo $! > unix/pids/eureka-server.pid
sleep 10

echo "Starting auth-service"
nohup java -jar auth-service/target/auth-service-0.0.1-SNAPSHOT.jar > unix/logs/auth-service.log 2>&1 &
echo $! > unix/pids/auth-service.pid

echo "Starting lib-service"
nohup java -jar lib-service/target/lib-service-0.0.1-SNAPSHOT.jar > unix/logs/lib-service.log 2>&1 &
echo $! > unix/pids/lib-service.pid

echo "Starting user-service"
nohup java -jar user-service/target/user-service-0.0.1-SNAPSHOT.jar > unix/logs/user-service.log 2>&1 &
echo $! > unix/pids/user-service.pid

echo "Starting gateway"
nohup java -jar gateway/target/gateway-0.0.1-SNAPSHOT.jar > unix/logs/gateway.log 2>&1 &
echo $! > unix/pids/gateway.pid

echo "All services started!"
read -p "Press Enter to exit..."
