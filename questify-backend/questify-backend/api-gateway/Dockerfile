# ==============================================================================
# Stage 1: Build API Gateway from source
# ==============================================================================
FROM maven:3.9-eclipse-temurin-21-alpine AS build

WORKDIR /workspace

COPY pom.xml .
COPY api-gateway/pom.xml api-gateway/
COPY api-gateway/src api-gateway/src

RUN mvn -B -f api-gateway/pom.xml clean package -DskipTests

# ==============================================================================
# Stage 2: Production Runtime with JVM Memory Optimization
# ==============================================================================
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

RUN apk add --no-cache curl wget

COPY --from=build /workspace/api-gateway/target/*.jar app.jar

ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -Xms64m -Xmx256m"

EXPOSE 8000

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
