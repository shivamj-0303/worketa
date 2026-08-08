# ==============================
# Stage 1: Build
# ==============================
FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /build

COPY pom.xml .
COPY mvnw .
COPY .mvn .mvn/

RUN chmod +x mvnw

COPY src src/

RUN ./mvnw clean package -DskipTests -q


# ==============================
# Stage 2: Runtime
# ==============================
FROM eclipse-temurin:21-jre-alpine

RUN addgroup -S appuser && \
    adduser -S appuser -G appuser

WORKDIR /app

COPY --from=builder /build/target/*.jar /app/worketa.jar

RUN mkdir -p /var/log/worketa && \
    chown -R appuser:appuser /app /var/log/worketa

USER appuser

EXPOSE 8080

ENV JAVA_OPTS="-XX:+UseG1GC \
-XX:+ParallelRefProcEnabled \
-XX:MaxGCPauseMillis=200 \
-XX:InitiatingHeapOccupancyPercent=35 \
-XX:+HeapDumpOnOutOfMemoryError \
-XX:HeapDumpPath=/var/log/worketa/heap-dump.hprof"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -Dspring.profiles.active=prod -jar /app/worketa.jar"]
