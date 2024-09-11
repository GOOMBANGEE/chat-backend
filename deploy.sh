#!/bin/bash

# 사용자에 맞게 설정해야하는 변수
# CONF_FILE_NAME, NGINX_CONTAINER_NAME, HEALTH_PATH

# 스크립트 실행 중 에러 발생 시 중지
set -e

# image
IMAGE_NAME="localhost:5000/chat"
LATEST_IMAGE_TAG="latest"
PREVIOUS_IMAGE_TAG="previous"

# container
CONTAINER1="chat1"
PORT1=8080
CONTAINER2="chat2"
PORT2=8081

# nginx
CONF_PATH=/docker/nginx/conf.d/${CONF_FILE_NAME}
NGINX_CONTAINER=${NGINX_CONTAINER_NAME}


# 컨테이너 실행
function start_container {
  CONTAINER=$1
  echo "=== start container $CONTAINER ==="
  docker-compose up -d $CONTAINER
}

# 컨테이너가 실행 중이면 중지
function stop_container {
  CONTAINER=$1
  echo "=== stop container $CONTAINER ==="
  # 컨테이너가 실행 중이지 않으면 에러 무시
  docker-compose stop $CONTAINER || true
  # 컨테이너 삭제
  docker-compose rm $CONTAINER || true
  # 사용중이던 이미지 삭제
  docker rmi $IMAGE_NAME:$PREVIOUS_IMAGE_TAG
}

# 문제 발생 시 롤백
function rollback {
  CURRENT_CONTAINER=$1
  NEW_CONTAINER=$2

  echo "=== rollback previous version ==="
  stop_container $NEW_CONTAINER

  # 새로운 이미지 삭제
  docker rmi $IMAGE_NAME:$LATEST_IMAGE_TAG || true
  # previous 이미지 latest 로 복구
  docker tag $IMAGE_NAME:$PREVIOUS_IMAGE_TAG $IMAGE_NAME:$LATEST_IMAGE_TAG
  # previous 로 태그된 이미지 삭제
  docker rmi $IMAGE_NAME:$PREVIOUS_IMAGE_TAG || true
}

# 현재 실행중인 이미지 가져오기
if docker inspect $IMAGE_NAME:$LATEST_IMAGE_TAG > /dev/null 2>&1; then
  echo "=== tag the current latest image as $PREVIOUS_IMAGE_TAG ==="
  # 현재 실행중인 이미지를 이전버전으로 기록
  docker tag $IMAGE_NAME:$LATEST_IMAGE_TAG $IMAGE_NAME:$PREVIOUS_IMAGE_TAG
fi

# 현재 실행중인 컨테이너 가져오기
if docker ps --format "{{.Names}}" | grep -w $CONTAINER1 > /dev/null; then
  echo "=== currently running container is $CONTAINER1 ==="
  CURRENT_CONTAINER=$CONTAINER1
  NEW_CONTAINER=$CONTAINER2
else
  echo "=== currently running container is $CONTAINER2 ==="
  CURRENT_CONTAINER=$CONTAINER2
  NEW_CONTAINER=$CONTAINER1
fi

# 새로운 이미지 가져오기
docker pull $IMAGE_NAME:$LATEST_IMAGE_TAG

# 최신 버전으로 컨테이너 업데이트
echo "=== deploying new version ==="
if [ "$CURRENT_CONTAINER" = "$CONTAINER1" ]; then
  start_container $CONTAINER2
  NEW_CONTAINER_PORT=$PORT2
else
  start_container $CONTAINER1
  NEW_CONTAINER_PORT=$PORT1
fi

# 컨테이너가 켜질때까지 대기
sleep 10

# 새롭게 켜진 컨테이너가 정상작동하는지 확인
echo "=== check new container ==="
HEALTH_URL="htp:/localhost:$NEW_CONTAINER_PORT/${HEALTH_PATH}"
HEALTH_STATUS=$(curl -s -o /dev/null -w "%{http_code}" $HEALTH_URL)

if [ "$HEALTH_STATUS" -ne 200 ]; then
  echo "=== 새로운 컨테이너 오류발생. 롤백시작. 코드: $HEALTH_STATUS ==="
  rollback $CURRENT_CONTAINER $NEW_CONTAINER
  exit 1
fi

echo "> $NEW_CONTAINER_PORT 포트로 nginx 트래픽 라우팅 재설정"
if [ "$NEW_CONTAINER_PORT" = "$PORT1" ]; then
  sed -i "s/server localhost:$PORT2/server localhost:$PORT1/g" "$CONF_PATH"
else
  sed -i "s/server localhost:$PORT1/server localhost:$PORT2/g" "$CONF_PATH"
fi

# docker nginx 컨테이너에서 nginx 리로드
docker exec $NGINX_CONTAINER nginx -s reload

echo "=== new version deployed successfully ==="
