.PHONY: help setup start stop test clean build run

# Default target
help:
	@echo "Available commands:"
	@echo "  setup    - Set up the development environment"
	@echo "  start    - Start the PostgreSQL database"
	@echo "  stop     - Stop the PostgreSQL database"
	@echo "  build    - Build the application"
	@echo "  run      - Run the Spring Boot application"
	@echo "  test     - Run tests"
	@echo "  clean    - Clean build artifacts"
	@echo "  logs     - Show application logs"

# Set up the development environment
setup:
	@echo "🚀 Setting up Java Challenge Environment..."
	@./setup.sh

# Start PostgreSQL database
start:
	@echo "🐳 Starting PostgreSQL database..."
	docker-compose up -d
	@echo "✅ Database started. Waiting for it to be ready..."
	@sleep 10
	@echo "✅ Database is ready!"

# Stop PostgreSQL database
stop:
	@echo "🛑 Stopping PostgreSQL database..."
	docker-compose down
	@echo "✅ Database stopped!"

# Build the application
build:
	@echo "🔨 Building the application..."
	mvn clean compile

# Run the Spring Boot application
run:
	@echo "🚀 Starting Spring Boot application..."
	mvn spring-boot:run

# Run tests
test:
	@echo "🧪 Running tests..."
	mvn test

# Clean build artifacts
clean:
	@echo "🧹 Cleaning build artifacts..."
	mvn clean
	@echo "✅ Cleaned!"

# Show application logs
logs:
	@echo "📋 Application logs:"
	docker-compose logs -f postgres

# Health check
health:
	@echo "🏥 Checking application health..."
	@curl -s http://localhost:8080/health | jq . 2>/dev/null || curl -s http://localhost:8080/health

# API test
api-test:
	@echo "🧪 Testing API endpoints..."
	@echo "GET /api/users:"
	@curl -s http://localhost:8080/api/users | jq . 2>/dev/null || curl -s http://localhost:8080/api/users
	@echo ""
	@echo "POST /api/users:"
	@curl -X POST http://localhost:8080/api/users \
		-H "Content-Type: application/json" \
		-d '{"name":"Test User","email":"test@example.com"}' \
		-s | jq . 2>/dev/null || curl -X POST http://localhost:8080/api/users \
		-H "Content-Type: application/json" \
		-d '{"name":"Test User","email":"test@example.com"}' -s 