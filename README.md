# Java Spring Boot Challenge

This is a Java code challenge using Spring Boot with PostgreSQL database. The application provides a REST API for managing users.

## 🚀 Quick Start

### Prerequisites
- Java 17 or higher
- Maven 3.6+
- Docker and Docker Compose

### Setup Instructions

#### Option 1: Using the setup script (Recommended)
```bash
# Clone the repository
git clone <repository-url>
cd challenge-java

# Run the setup script
./setup.sh
```

#### Option 2: Manual setup
1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd challenge-java
   ```

2. **Start the PostgreSQL database**
   ```bash
   docker-compose up -d
   ```
   This will start PostgreSQL on port 5432 with the following credentials:
   - Database: `challenge_db`
   - Username: `challenge_user`
   - Password: `challenge_password`

3. **Run the Spring Boot application**
   ```bash
   mvn spring-boot:run
   ```
   The application will start on `http://localhost:8080`

4. **Verify the setup**
   ```bash
   curl http://localhost:8080/api/users
   ```

#### Option 3: Using Makefile commands
```bash
# Set up everything
make setup

# Or use individual commands
make start    # Start database
make build    # Build application
make run      # Run application
make test     # Run tests
make health   # Check application health
make help     # Show all available commands
```

## 📋 Challenge Requirements

### Current Implementation
The application currently includes:
- ✅ Spring Boot 3.2.0 with Java 17
- ✅ PostgreSQL database with Docker Compose
- ✅ JPA/Hibernate for database operations
- ✅ REST API with CRUD operations for Users
- ✅ Input validation
- ✅ Basic error handling

### Challenge Tasks

#### Task 1: Add Product Management
Create a new `Product` entity with the following fields:
- `id` (Long, primary key)
- `name` (String, required)
- `description` (String)
- `price` (BigDecimal, required)
- `category` (String, required)
- `stockQuantity` (Integer, required)
- `createdAt` (LocalDateTime)
- `updatedAt` (LocalDateTime)

Implement:
- Product entity with proper JPA annotations
- ProductRepository interface
- ProductService with business logic
- ProductController with REST endpoints
- Add validation for price (must be positive) and stockQuantity (must be non-negative)

#### Task 2: Implement Order Management
Create an `Order` system with:
- `Order` entity with:
  - `id` (Long, primary key)
  - `userId` (Long, foreign key to User)
  - `orderDate` (LocalDateTime)
  - `status` (OrderStatus enum: PENDING, CONFIRMED, SHIPPED, DELIVERED, CANCELLED)
  - `totalAmount` (BigDecimal)
  - `createdAt` (LocalDateTime)
  - `updatedAt` (LocalDateTime)

- `OrderItem` entity with:
  - `id` (Long, primary key)
  - `orderId` (Long, foreign key to Order)
  - `productId` (Long, foreign key to Product)
  - `quantity` (Integer)
  - `unitPrice` (BigDecimal)
  - `subtotal` (BigDecimal)

Implement:
- Proper relationships between entities
- Order and OrderItem repositories
- Order service with business logic for creating orders
- Order controller with endpoints for creating and retrieving orders
- Validation to ensure order items have valid products and quantities

#### Task 3: Add Advanced Features
Implement the following advanced features:

1. **Search and Filtering**
   - Add search functionality to find products by name or description
   - Add filtering by category and price range
   - Add pagination support for large datasets

2. **Exception Handling**
   - Create custom exceptions (ProductNotFoundException, OrderNotFoundException, etc.)
   - Implement a global exception handler
   - Return proper HTTP status codes and error messages

3. **Data Transfer Objects (DTOs)**
   - Create DTOs for request/response objects
   - Implement mapping between entities and DTOs
   - Use DTOs in controllers instead of entities

4. **Unit Tests**
   - Write unit tests for services
   - Write integration tests for controllers
   - Achieve at least 80% code coverage

#### Task 4: Bonus Features (Optional)
- Implement user authentication and authorization
- Add API documentation using Swagger/OpenAPI
- Implement caching for frequently accessed data
- Add logging with different levels
- Create database migrations using Flyway or Liquibase

## 🛠️ API Endpoints

### Users
- `GET /api/users` - Get all users
- `GET /api/users/{id}` - Get user by ID
- `GET /api/users/email/{email}` - Get user by email
- `POST /api/users` - Create new user
- `PUT /api/users/{id}` - Update user
- `DELETE /api/users/{id}` - Delete user

### Example Requests

**Create a user:**
```bash
curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d '{
    "name": "John Doe",
    "email": "john.doe@example.com"
  }'
```

**Get all users:**
```bash
curl http://localhost:8080/api/users
```

## 🗄️ Database Schema

The application uses PostgreSQL with the following initial schema:

```sql
CREATE TABLE users (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

## 🧪 Testing

Run tests with:
```bash
mvn test
```

## 📁 Project Structure

```
src/
├── main/
│   ├── java/com/challenge/
│   │   ├── JavaChallengeApplication.java
│   │   ├── controller/
│   │   │   └── UserController.java
│   │   ├── entity/
│   │   │   └── User.java
│   │   ├── repository/
│   │   │   └── UserRepository.java
│   │   └── service/
│   │       └── UserService.java
│   └── resources/
│       └── application.yml
└── test/
    └── java/com/challenge/
        └── JavaChallengeApplicationTests.java
```

## 🐳 Docker

The project includes Docker Compose for easy database setup:

```yaml
version: '3.8'
services:
  postgres:
    image: postgres:15-alpine
    environment:
      POSTGRES_DB: challenge_db
      POSTGRES_USER: challenge_user
      POSTGRES_PASSWORD: challenge_password
    ports:
      - "5432:5432"
```

## 📝 Notes

- The application uses Hibernate's `ddl-auto: update` for automatic schema generation
- SQL queries are logged for debugging purposes
- The application includes devtools for hot reloading during development
- CORS is enabled for all origins (configure appropriately for production)

## 🎯 Evaluation Criteria

Your solution will be evaluated based on:
- Code quality and organization
- Proper use of Spring Boot features
- Database design and relationships
- API design and RESTful principles
- Error handling and validation
- Test coverage
- Documentation
- Performance considerations

Good luck with the challenge! 🚀 