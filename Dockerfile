FROM maven:3.9.11-eclipse-temurin-21-alpine AS builder

WORKDIR /app

COPY pom.xml pom.xml
COPY auth-service ./auth-service
COPY common ./common
COPY eureka-server ./eureka-server
COPY gateway ./gateway
COPY lib-service ./lib-service
COPY user-service ./user-service

RUN mvn clean package -DskipTests


#   Create images for servises. Copy jars from build stage to new images

FROM eclipse-temurin:21-jre-alpine AS eureka-server
WORKDIR /app
COPY --from=builder /app/eureka-server/target/eureka-server-0.0.1-SNAPSHOT.jar .
ENTRYPOINT ["java", "-jar", "eureka-server-0.0.1-SNAPSHOT.jar"]

FROM eclipse-temurin:21-jre-alpine AS auth-service
WORKDIR /app
COPY --from=builder /app/auth-service/target/auth-service-0.0.1-SNAPSHOT.jar .
ENTRYPOINT ["java", "-jar", "auth-service-0.0.1-SNAPSHOT.jar"]

FROM eclipse-temurin:21-jre-alpine AS lib-service
WORKDIR /app
COPY --from=builder /app/lib-service/target/lib-service-0.0.1-SNAPSHOT.jar .
ENTRYPOINT ["java", "-jar", "lib-service-0.0.1-SNAPSHOT.jar"]

FROM eclipse-temurin:21-jre-alpine AS user-service
WORKDIR /app
COPY --from=builder /app/user-service/target/user-service-0.0.1-SNAPSHOT.jar .
ENTRYPOINT ["java", "-jar", "user-service-0.0.1-SNAPSHOT.jar"]

FROM eclipse-temurin:21-jre-alpine AS gateway
WORKDIR /app
COPY --from=builder /app/gateway/target/gateway-0.0.1-SNAPSHOT.jar .
ENTRYPOINT ["java", "-jar", "gateway-0.0.1-SNAPSHOT.jar"]
