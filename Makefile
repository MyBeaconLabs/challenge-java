.PHONY: help setup start stop reset-db test clean build run

# Default target
help:
	@echo "Available commands:"
	@echo "  setup    - Set up the development environment"
	@echo "  start    - Start the PostgreSQL database"
	@echo "  stop     - Stop the PostgreSQL database"
	@echo "  reset-db - Stop the database and delete its data"
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

# Drop all local data (Flyway recreates the schema on next run)
reset-db:
	docker-compose down -v

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

# API smoke test
api-test:
	@curl -s http://localhost:8080/api/users; echo
	@curl -s http://localhost:8080/api/users/1/wallets; echo
