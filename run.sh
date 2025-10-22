#!/bin/bash

# User Management System - Quick Start Script

set -e

echo "==================================="
echo "User Management System - Quick Start"
echo "==================================="

# Function to check if command exists
command_exists() {
    command -v "$1" >/dev/null 2>&1
}

# Check prerequisites
echo "Checking prerequisites..."

if ! command_exists docker; then
    echo "Error: Docker is not installed."
    exit 1
fi

if ! command_exists docker-compose; then
    echo "Error: Docker Compose is not installed."
    exit 1
fi

echo "Prerequisites OK!"

# Create .env file if it doesn't exist
if [ ! -f .env ]; then
    echo "Creating .env file from .env.example..."
    cp .env.example .env
    echo ".env file created. Please update it with your configuration."
fi

# Start services
echo ""
echo "Starting services with Docker Compose..."
docker-compose up -d

echo ""
echo "Waiting for services to be ready..."
sleep 15

# Check if application is running
if curl -f http://localhost:8080/actuator/health > /dev/null 2>&1; then
    echo ""
    echo "==================================="
    echo "Application is running successfully!"
    echo "==================================="
    echo ""
    echo "API: http://localhost:8080"
    echo "Swagger UI: http://localhost:8080/swagger-ui.html"
    echo "Health Check: http://localhost:8080/actuator/health"
    echo ""
    echo "Default Admin Credentials:"
    echo "  Username: admin"
    echo "  Password: admin123"
    echo ""
    echo "To view logs: docker-compose logs -f app"
    echo "To stop: docker-compose down"
else
    echo ""
    echo "Warning: Application may not be ready yet."
    echo "Check logs with: docker-compose logs -f app"
fi
