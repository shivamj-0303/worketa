# Multi-stage production Dockerfile for WORKETA
# Stage 1: Build
FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /build

# Copy Maven files
COPY pom.xml .
COPY mvnw .
COPY .mvn .mvn/

# Copy source
COPY src src/

# Build with optimizations
RUN ./mvnw clean package -DskipTests -q

# Stage 2: Runtime
FROM eclipse-temurin:21-jre-alpine

# Add non-root user for security
RUN addgroup -g 1000 appuser && adduser -D -u 1000 -G appuser appuser

WORKDIR /app

# Copy JAR from builder
COPY --from=builder /build/target/worketa-*.jar worketa.jar

# Create log directory with proper permissions
RUN mkdir -p /var/log/worketa && chown -R appuser:appuser /var/log/worketa /app

# Switch to non-root user
USER appuser

# Health check
HEALTHCHECK --interval=30s --timeout=5s --start-period=30s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

# Expose port
EXPOSE 8080

# JVM tuning for production
ENV JAVA_OPTS="-XX:+UseG1GC \
    -XX:+ParallelRefProcEnabled \
    -XX:+UnlockDiagnosticVMOptions \
    -XX:G1SummarizeRSetStatsPeriod=1 \
    -XX:SurvivorRatio=10 \
    -XX:MaxGCPauseMillis=200 \
    -XX:InitiatingHeapOccupancyPercent=35 \
    -XX:+HeapDumpOnOutOfMemoryError \
    -XX:HeapDumpPath=/var/log/worketa/heap-dump.hprof"

# Run Spring Boot with production profile
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -Dspring.profiles.active=prod -jar worketa.jar"]
