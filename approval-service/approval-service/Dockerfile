# ==============================================================================
# Stage 1: Build Approval Service from source
# ==============================================================================
FROM maven:3.9-eclipse-temurin-21-alpine AS build

WORKDIR /workspace

COPY pom.xml .
COPY src ./src

RUN mvn -B clean package -Dmaven.test.skip=true

# ==============================================================================
# Stage 2: Production Runtime with JVM Memory Optimization
# ==============================================================================
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

RUN apk add --no-cache curl wget

COPY --from=build /workspace/target/*.jar app.jar

ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -Xms64m -Xmx256m"

EXPOSE 8087

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
