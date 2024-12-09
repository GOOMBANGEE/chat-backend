# [Chat.goomba](https://chat.goomba.org) API
API for chat.goomba  

STOMP를 이용하여 실시간으로 개인, 그룹채팅을 제공합니다.  
텍스트및 이미지 업로드가 가능합니다.


## Environment

Java 17  
Mysql 8.0  
Gradle

Spring boot 3.3.2  
Spring JPA  
Spring Data JPA  
Querydsl

jwt  
websocket  
lombok  
sentry  
thumbnailator  
imageio

## Deployment

```
git clone https://github.com/GOOMBANGEE/chat-backend.git

cd chat-backend
nano .env

./gradlew build # Linux/mac
./gradlew.bat build # Windows

java -jar ./build/libs/chat-0.0.1.jar
```

## Architecture
<img width="1222" alt="architecture" src="https://github.com/user-attachments/assets/37bdb56b-4078-4104-8726-bc644604f5a1">

## ERD
![erd](https://github.com/user-attachments/assets/fc97cb14-4940-4053-82a3-75e11563a44b)

## Screenshot
로그인화면  
![login](https://github.com/user-attachments/assets/df90ea7d-9266-4d44-ac9e-75d4d0809ecd)

계정생성  
![register](https://github.com/user-attachments/assets/f593be89-06ac-47f5-8a83-972c9219a7af)

서버채팅  
![chat](https://github.com/user-attachments/assets/58fdafcc-e58f-48f9-8025-f5617d18f35c)

계정설정  
![user-setting](https://github.com/user-attachments/assets/0d300484-6ec0-4564-96f5-e2013a596976)

친구목록  
![friend-list](https://github.com/user-attachments/assets/08108ed6-3db1-4f6a-8126-6a548295f75b)

개인채팅 알림  
![notification](https://github.com/user-attachments/assets/50a055ca-a3d5-402b-b940-7b412ce26ba8)
![dm](https://github.com/user-attachments/assets/a513e37b-7596-42e0-ab8f-aee75929e2d3)

채팅검색  
![search](https://github.com/user-attachments/assets/a9c014cb-3e4c-4139-9bbb-4bb23ecdca58)
