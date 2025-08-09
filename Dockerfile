# Java 17 기반 이미지
FROM openjdk:17

# jar 파일이 저장될 디렉토리
WORKDIR /app

# build/libs/app.jar을 컨테이너에 복사
COPY build/libs/gamproject-1.0.jar app.jar

# 실행 명령
#ENTRYPOINT ["java", "-jar", "app.jar"]
