# ==============================================================================
# Stage 1: Build Eureka Server from source
# ==============================================================================
FROM maven:3.9-eclipse-temurin-21-alpine AS build

WORKDIR /workspace

# Copy parent and module files
COPY pom.xml .
COPY eureka-server/pom.xml eureka-server/
COPY eureka-server/src eureka-server/src

# Compile and package module
RUN mvn -B -f eureka-server/pom.xml clean package -DskipTests

# ==============================================================================
# Stage 2: Production Runtime with JVM Memory Optimization
# ==============================================================================
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

RUN apk add --no-cache curl wget

COPY --from=build /workspace/eureka-server/target/*.jar app.jar

ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -Xms64m -Xmx256m"

EXPOSE 8761

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
