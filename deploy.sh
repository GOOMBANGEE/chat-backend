#!/bin/bash

# 스크립트 실행 중 에러 발생 시 중지
set -e

# image
IMAGE_NAME="localhost:5000/chat-backend"
LATEST_IMAGE_TAG="latest"
PREVIOUS_IMAGE_TAG="previous"

# service
SERVICE_1="chat1"
PORT_1=8080
SERVICE_2="chat2"
PORT_2=8081
LOCAL_HOST=192.168.1.1

# check health
HEALTH_PORT_1=8000
HEALTH_PORT_2=8001
HEALTH_PATH= {}

# nginx
CONF_PATH= {}
NGINX_CONTAINER= {}
CONTAINER_NAME_1= {}
CONTAINER_NAME_2= {}

# 서비스 실행
function start_service {
  SERVICE=$1
  echo "=== start service $SERVICE ==="
  cd /var/
  docker-compose up -d $SERVICE
}

# 서비스가 실행 중이면 중지
function stop_service {
  SERVICE=$1
  echo "=== stop service $SERVICE ==="
  # 컨테이너가 실행 중이지 않으면 에러 무시
  docker-compose stop $SERVICE || true
  # 컨테이너 삭제
  docker-compose rm $SERVICE || true
  # 사용중이던 이미지 삭제
  docker rmi $IMAGE_NAME:$PREVIOUS_IMAGE_TAG
  docker system prune -f
  docker volume prune -f
  docker network prune -f
}

# 문제 발생 시 롤백
function rollback {
  CURRENT_SERVICE=$1
  NEW_SERVICE=$2

  echo "=== rollback to previous version ==="
  stop_service $NEW_SERVICE

  # 새로운 이미지 삭제
  docker rmi $IMAGE_NAME:$LATEST_IMAGE_TAG || true
  # previous 이미지 latest로 복구
  docker tag $IMAGE_NAME:$PREVIOUS_IMAGE_TAG $IMAGE_NAME:$LATEST_IMAGE_TAG
  # previous로 태그된 이미지 삭제
  docker rmi $IMAGE_NAME:$PREVIOUS_IMAGE_TAG || true

  # 이전 버전의 서비스 재시작
  start_service $CURRENT_SERVICE
}

# 현재 실행중인 이미지 가져오기
if docker inspect $IMAGE_NAME:$LATEST_IMAGE_TAG > /dev/null 2>&1; then
  echo "=== tag the current latest image as $PREVIOUS_IMAGE_TAG ==="
  docker tag $IMAGE_NAME:$LATEST_IMAGE_TAG $IMAGE_NAME:$PREVIOUS_IMAGE_TAG
fi

# 현재 실행중인 서비스 가져오기
if docker ps --format "{{.Names}}" | grep -w $CONTAINER_NAME_1 > /dev/null; then
  echo "=== currently running service is $SERVICE_1 ==="
  CURRENT_SERVICE=$SERVICE_1
  NEW_SERVICE=$SERVICE_2
else
  echo "=== currently running service is $SERVICE_2 ==="
  CURRENT_SERVICE=$SERVICE_2
  NEW_SERVICE=$SERVICE_1
fi

# 새로운 이미지 가져오기
echo "=== pulling new image ==="
docker pull $IMAGE_NAME:$LATEST_IMAGE_TAG

# 최신 버전으로 컨테이너 업데이트
echo "=== deploying new version ==="
if [ "$CURRENT_SERVICE" = "$SERVICE_1" ]; then
  start_service $SERVICE_2
  NEW_SERVICE_PORT=$PORT_2
  NEW_SERVICE_HEALTH_PORT=$HEALTH_PORT_2
else
  start_service $SERVICE_1
  NEW_SERVICE_PORT=$PORT_1
  NEW_SERVICE_HEALTH_PORT=$HEALTH_PORT_1
fi

# 컨테이너가 켜질때까지 대기
sleep 10

# 새롭게 켜진 컨테이너가 정상작동하는지 확인
echo "=== check new service ==="
HEALTH_URL="http://$LOCAL_HOST:$NEW_SERVICE_HEALTH_PORT/$HEALTH_PATH"
echo $HEALTH_URL
HEALTH_STATUS=$(curl -s -o /dev/null -w "%{http_code}" $HEALTH_URL)
echo $HEALTH_STATUS

if [ "$HEALTH_STATUS" -ne 200 ]; then
  echo "=== 새로운 컨테이너 오류발생. 롤백시작. 코드: $HEALTH_STATUS ==="
  rollback $CURRENT_SERVICE $NEW_SERVICE
  exit 1
fi


echo "> $NEW_SERVICE_PORT 포트로 nginx 트래픽 라우팅 재설정"
if [ "$NEW_SERVICE_PORT" = "$PORT_1" ]; then
  sed -i "s/server $LOCAL_HOST:$PORT_2/server $LOCAL_HOST:$PORT_1/g" "$CONF_PATH"
else
  sed -i "s/server $LOCAL_HOST:$PORT_1/server $LOCAL_HOST:$PORT_2/g" "$CONF_PATH"
fi

# docker nginx 컨테이너에서 nginx 리로드
cd /var/
docker exec $NGINX_CONTAINER nginx -s reload

# 이전버전 종료
echo "=== stop previous version ==="
if [ "$CURRENT_SERVICE" = "$SERVICE_1" ]; then
  stop_service $SERVICE_1
else
  stop_service $SERVICE_2
fi

echo "=== new version deployed successfully ==="
