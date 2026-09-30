#!/bin/bash

echo "🚀 Setting up Java Challenge Environment..."

# Check if Docker is running
if ! docker info > /dev/null 2>&1; then
    echo "❌ Docker is not running. Please start Docker and try again."
    exit 1
fi

# Check if Java 17+ is installed
if ! command -v java &> /dev/null; then
    echo "❌ Java is not installed. Please install Java 17 or higher."
    exit 1
fi

JAVA_VERSION=$(java -version 2>&1 | head -n 1 | cut -d'"' -f2 | cut -d'.' -f1)
if [ "$JAVA_VERSION" -lt 17 ]; then
    echo "❌ Java version $JAVA_VERSION is too old. Please install Java 17 or higher."
    exit 1
fi

echo "✅ Java version: $(java -version 2>&1 | head -n 1)"

# Check if Maven is installed
if ! command -v mvn &> /dev/null; then
    echo "❌ Maven is not installed. Please install Maven 3.6+."
    exit 1
fi

echo "✅ Maven version: $(mvn -version | head -n 1)"

# Start PostgreSQL
echo "🐳 Starting PostgreSQL database..."
docker-compose up -d

# Wait for database to be ready
echo "⏳ Waiting for database to be ready..."
sleep 10

# Check if database is ready
if docker-compose exec postgres pg_isready -U challenge_user -d challenge_db > /dev/null 2>&1; then
    echo "✅ Database is ready!"
else
    echo "❌ Database failed to start. Please check Docker logs."
    exit 1
fi

# Build the application
echo "🔨 Building the application..."
mvn clean compile

if [ $? -eq 0 ]; then
    echo "✅ Application built successfully!"
    echo ""
    echo "🎉 Setup complete! You can now run the application with:"
    echo "   mvn spring-boot:run"
    echo ""
    echo "📋 The application will be available at: http://localhost:8080"
    echo "📋 API documentation: http://localhost:8080/api/users"
    echo ""
    echo "📖 Check the README.md file for challenge instructions and API examples."
else
    echo "❌ Build failed. Please check the error messages above."
    exit 1
fi 