#!/bin/bash
# WORKETA Backend - Docker Build and Deployment Script
# Purpose: Automate Docker image build, test, and deployment
# Author: WORKETA DevOps Team
# Date: 2026-05-24

set -e

# Color codes for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Configuration
BACKEND_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
IMAGE_NAME="worketa-backend"
IMAGE_TAG="1.0"
IMAGE_FULL="${IMAGE_NAME}:${IMAGE_TAG}"
REGISTRY_URL="${DOCKER_REGISTRY:-docker.io}"
REGISTRY_USER="${DOCKER_USER:-your-username}"
JAR_FILE="target/worketa-0.0.1-SNAPSHOT.jar"

# Functions
log_info() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

log_warn() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Check prerequisites
check_prerequisites() {
    log_info "Checking prerequisites..."
    
    # Check Docker
    if ! command -v docker &> /dev/null; then
        log_error "Docker is not installed"
        exit 1
    fi
    
    # Check Docker daemon
    if ! docker ps &> /dev/null; then
        log_error "Docker daemon is not running. Start it with: sudo systemctl start docker"
        exit 1
    fi
    
    # Check Maven (optional, for building locally first)
    if ! command -v mvn &> /dev/null; then
        log_warn "Maven is not installed. Docker build will compile inside container."
    fi
    
    log_info "All prerequisites met ✓"
}

# Build JAR locally (optional, faster)
build_jar_locally() {
    log_info "Building JAR locally..."
    cd "$BACKEND_DIR"
    ./mvnw clean package -DskipTests
    log_info "JAR build complete ✓"
}

# Build Docker image
build_docker_image() {
    log_info "Building Docker image: $IMAGE_FULL"
    
    cd "$BACKEND_DIR"
    
    docker build \
        -t "$IMAGE_FULL" \
        -f Dockerfile \
        --build-arg JAVA_TOOL_OPTIONS="-Xmx512m" \
        .
    
    log_info "Docker image built successfully ✓"
}

# Test Docker image locally
test_docker_image() {
    log_info "Testing Docker image locally..."
    
    # Check if port 8080 is available
    if netstat -tuln 2>/dev/null | grep -q ":8080 "; then
        log_warn "Port 8080 is already in use. Skipping local test."
        return
    fi
    
    log_info "Starting container..."
    CONTAINER_ID=$(docker run -d \
        -p 8080:8080 \
        -e SPRING_PROFILES_ACTIVE=prod \
        --name worketa-test \
        "$IMAGE_FULL")
    
    log_info "Container started with ID: $CONTAINER_ID"
    
    # Wait for container to be ready
    log_info "Waiting for container to be ready..."
    sleep 10
    
    # Test health endpoint
    if curl -s http://localhost:8080/actuator/health | grep -q "UP"; then
        log_info "Health check passed ✓"
    else
        log_error "Health check failed"
        docker logs worketa-test
        docker rm -f worketa-test
        exit 1
    fi
    
    # Cleanup
    docker rm -f worketa-test
    log_info "Test container cleaned up ✓"
}

# Tag image for registry
tag_for_registry() {
    local registry_image="${REGISTRY_URL}/${REGISTRY_USER}/${IMAGE_NAME}:${IMAGE_TAG}"
    log_info "Tagging image for registry: $registry_image"
    docker tag "$IMAGE_FULL" "$registry_image"
    log_info "Image tagged ✓"
}

# Push to registry
push_to_registry() {
    if [ -z "$DOCKER_USER" ] || [ -z "$DOCKER_PASSWORD" ]; then
        log_warn "DOCKER_USER or DOCKER_PASSWORD not set. Skipping push to registry."
        return
    fi
    
    local registry_image="${REGISTRY_URL}/${REGISTRY_USER}/${IMAGE_NAME}:${IMAGE_TAG}"
    
    log_info "Logging in to Docker registry..."
    echo "$DOCKER_PASSWORD" | docker login -u "$DOCKER_USER" --password-stdin "$REGISTRY_URL"
    
    log_info "Pushing image to registry: $registry_image"
    docker push "$registry_image"
    
    log_info "Image pushed successfully ✓"
}

# Start with docker-compose
start_with_compose() {
    log_info "Starting services with docker-compose..."
    
    cd "$BACKEND_DIR"
    
    if [ -f "docker-compose.prod.yml" ]; then
        docker-compose -f docker-compose.prod.yml up -d
        log_info "Services started with docker-compose.prod.yml ✓"
    elif [ -f "docker-compose.yml" ]; then
        docker-compose -f docker-compose.yml up -d
        log_info "Services started with docker-compose.yml ✓"
    else
        log_error "No docker-compose file found"
        exit 1
    fi
    
    sleep 5
    
    # Check status
    log_info "Service status:"
    docker-compose ps
}

# Display image info
display_info() {
    log_info "Docker image information:"
    docker images "$IMAGE_FULL" --format "table {{.Repository}}\t{{.Tag}}\t{{.Size}}\t{{.CreatedAt}}"
}

# Cleanup
cleanup() {
    log_info "Cleanup:"
    log_info "- Container logs: docker logs <container-id>"
    log_info "- View running containers: docker ps"
    log_info "- Stop services: docker-compose down"
    log_info "- Remove image: docker rmi $IMAGE_FULL"
}

# Main execution
main() {
    log_info "========================================="
    log_info "WORKETA Backend - Docker Build & Deploy"
    log_info "========================================="
    
    # Parse arguments
    case "${1:-build}" in
        build)
            check_prerequisites
            build_docker_image
            display_info
            ;;
        test)
            check_prerequisites
            test_docker_image
            ;;
        full)
            check_prerequisites
            build_docker_image
            test_docker_image
            tag_for_registry
            display_info
            ;;
        compose)
            check_prerequisites
            start_with_compose
            ;;
        push)
            tag_for_registry
            push_to_registry
            ;;
        help)
            show_help
            ;;
        *)
            log_error "Unknown command: $1"
            show_help
            exit 1
            ;;
    esac
    
    cleanup
    log_info "Done ✓"
}

# Help message
show_help() {
    cat << EOF
Usage: $0 [COMMAND]

Commands:
    build       Build Docker image (default)
    test        Build and test Docker image locally
    full        Build, test, tag, and display info
    compose     Start services with docker-compose
    push        Push image to Docker registry
    help        Show this help message

Environment Variables:
    DOCKER_REGISTRY     Docker registry URL (default: docker.io)
    DOCKER_USER         Docker username (required for push)
    DOCKER_PASSWORD     Docker password (required for push)

Examples:
    $0 build                 # Build Docker image
    $0 test                  # Build and test
    $0 full                  # Full workflow
    DOCKER_USER=myuser $0 push   # Push to registry

EOF
}

# Run main function
main "$@"
