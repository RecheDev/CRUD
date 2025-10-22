# User Management System

Spring Boot 3.2.5 application implementing JWT authentication, role-based access control, and comprehensive security features. Built with Domain-Driven Design principles and includes a React frontend.

## Features

- **Authentication**: JWT-based with token blacklisting on logout
- **Authorization**: Role-based access control (USER, ADMIN, MODERATOR)
- **Security**: BCrypt password hashing, account lockout, rate limiting, security headers
- **Audit Logging**: Comprehensive logging of authentication and authorization events
- **API Documentation**: Interactive Swagger UI
- **Monitoring**: Prometheus metrics via Spring Boot Actuator
- **Frontend**: React 18 with TypeScript and Tailwind CSS

## Tech Stack

**Backend**
- Java 21, Spring Boot 3.2.5
- PostgreSQL 16 with Flyway migrations
- Spring Security + JWT
- MapStruct for DTO mapping
- JUnit 5, Mockito, REST Assured, Testcontainers

**Frontend**
- React 18, TypeScript 5.5
- Vite, React Router, Axios
- Tailwind CSS

## Quick Start

### Using Docker (Recommended)

```bash
# Copy environment template
cp .env.example .env

# Edit .env with your settings
# IMPORTANT: JWT_SECRET must be 64+ characters

# Start everything
docker-compose up -d

# Check health
curl http://localhost:8080/actuator/health
```

Access:
- API: http://localhost:8080
- Swagger: http://localhost:8080/swagger-ui.html
- Frontend: http://localhost:3000

### Manual Setup

**Prerequisites**: Java 21, PostgreSQL 16, Maven 3.x

```bash
# Create database
createdb usermanagement

# Configure environment
cp .env.example .env
# Edit .env with your database credentials and JWT secret

# Build and run
./mvnw clean install
./mvnw spring-boot:run
```

**Frontend**:
```bash
cd frontend
npm install
npm run dev
```

## Configuration

Create a `.env` file in the project root:

```bash
# Database
DB_HOST=localhost
DB_PORT=5432
DB_NAME=usermanagement
DB_USER=your_user
DB_PASSWORD=your_password

# JWT (REQUIRED - minimum 64 characters)
JWT_SECRET=your-secret-key-min-64-chars-change-in-production-use-strong-random-value
JWT_EXPIRATION=86400000

# CORS
CORS_ALLOWED_ORIGINS=http://localhost:3000,http://localhost:8080

# Optional: Initial admin account
ADMIN_USERNAME=admin
ADMIN_PASSWORD=SecurePassword123!
ADMIN_EMAIL=admin@example.com
```

## API Endpoints

### Authentication
- `POST /api/auth/register` - Register new user
- `POST /api/auth/login` - Login (returns JWT)
- `POST /api/auth/logout` - Logout (blacklists token)

### User Management
- `GET /api/users` - List users (admin only)
- `GET /api/users/{id}` - Get user
- `GET /api/users/me` - Get current user
- `PUT /api/users/{id}` - Update user
- `DELETE /api/users/{id}` - Delete user (admin only)

### Monitoring
- `GET /actuator/health` - Health check
- `GET /actuator/metrics` - Prometheus metrics (admin only)

## Security

**Password Policy**
- Minimum 12 characters
- Must contain: uppercase, lowercase, digit, special character

**Account Lockout**
- 5 failed attempts → 30 minute lock
- Auto-unlock after timeout

**Rate Limiting**
- 60 requests/minute per IP
- 1000 requests/hour per IP

**Other**
- Token blacklisting on logout
- Security headers (CSP, X-Frame-Options, XSS Protection)
- CORS configuration
- Audit logging

## Architecture

The project follows Domain-Driven Design with three distinct layers:

```
API Layer (Controllers + DTOs)
    ↓
Domain Layer (Business Logic)
    ↓
Persistence Layer (JPA Entities)
```

**Key Points**:
- JPA entities contain only database mapping
- Business logic lives in Domain Models
- Value Objects enforce type safety
- MapStruct handles conversions between layers

See `DOMAIN_ARCHITECTURE.md` for detailed architecture documentation.

## Project Structure

```
src/main/java/com/portfolio/usermanagement/
├── config/              # Spring configuration
├── controller/          # REST endpoints
├── dto/                 # Request/response DTOs
├── domain/
│   ├── model/           # Domain models (business logic)
│   └── valueobject/     # Value objects (Email, Username, etc.)
├── entity/              # JPA entities
├── repository/          # Data access
├── service/             # Service layer
├── security/jwt/        # JWT implementation
├── mapper/              # MapStruct mappers
├── validation/          # Custom validators
└── audit/               # Audit logging

src/test/java/
├── unit/                # Unit tests
├── integration/         # Integration tests (Testcontainers)
└── api/                 # API tests (REST Assured)
```

## Development

### Common Commands

```bash
# Run tests
./mvnw test

# Run with coverage
./mvnw verify
open target/site/jacoco-merged/index.html

# Run single test
./mvnw test -Dtest=UserServiceTest

# Database migrations
./mvnw flyway:migrate
./mvnw flyway:info

# Build production JAR
./mvnw clean package -DskipTests
java -jar target/user-management-1.0.0.jar
```

### Testing

Test coverage requirements:
- Line coverage: 75% minimum
- Branch coverage: 70% minimum

Enforced by JaCoCo during `mvn verify`.

## Logging

- Application logs: `logs/application.log` (30 day retention)
- Audit logs: `logs/audit.log` (90 day retention)
- Daily rotation

Enable debug logging:
```bash
export LOGGING_LEVEL_ROOT=DEBUG
```

## Troubleshooting

**"JWT secret must be at least 64 characters long"**
- Set JWT_SECRET to 64+ characters in .env

**Cannot connect to database**
- Check PostgreSQL is running: `pg_isready`
- Verify credentials in .env

**Account locked**
- Wait 30 minutes for auto-unlock
- Or have admin unlock manually

**Rate limit exceeded (429)**
- Wait 60 seconds before retrying
- Check if you're hitting 60 req/min or 1000 req/hour limit

## Deployment

### Production Checklist

- [ ] Use HTTPS
- [ ] Set strong JWT_SECRET (64+ chars, random)
- [ ] Change default admin password
- [ ] Configure CORS for production domains only
- [ ] Set up database backups
- [ ] Configure log aggregation
- [ ] Set up Prometheus monitoring
- [ ] Use Docker secrets for sensitive values
- [ ] Review security headers configuration

### Docker Production

```bash
docker build -t usermanagement:latest .
docker-compose -f docker-compose.prod.yml up -d
```

## Contributing

1. Fork the repo
2. Create a feature branch
3. Write tests for your changes
4. Ensure tests pass and coverage meets requirements
5. Submit a PR

Follow the existing code style and architecture patterns.

## License

Available for educational and portfolio purposes.
