# ==============================================================================
# Stage 1: Build Auth Service from source
# ==============================================================================
FROM maven:3.9-eclipse-temurin-21-alpine AS build

WORKDIR /workspace

COPY pom.xml .
COPY auth-service/pom.xml auth-service/
COPY auth-service/src auth-service/src

RUN mvn -B -f auth-service/pom.xml clean package -Dmaven.test.skip=true

# ==============================================================================
# Stage 2: Production Runtime with JVM Memory Optimization
# ==============================================================================
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

RUN apk add --no-cache curl wget

COPY --from=build /workspace/auth-service/target/*.jar app.jar

ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -Xms64m -Xmx256m"

EXPOSE 8081

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
