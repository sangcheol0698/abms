# ABMS

A modern business management system built with hexagonal architecture, featuring employee management, payroll processing, project tracking, and AI-powered chat assistance.

## Overview

ABMS is an enterprise-grade business management system that demonstrates hexagonal architecture principles with a clear separation between domain logic, application services, and external adapters. The system includes:

- **Employee Management**: Complete employee lifecycle management with department organization
- **Payroll Processing**: Automated payroll calculations and processing
- **Project & Contract Management**: Project tracking with employee assignments
- **AI Chat Assistant**: Integration with OpenAI for natural language queries
- **Excel Import/Export**: Bulk operations support via Excel files

### Technology Stack

**Backend:**
- Java 25 with virtual threads
- Spring Boot 4.0.0
- Spring Data JPA with QueryDSL 7.0
- MySQL 8.x (via Docker Compose)
- H2 Database (for testing)
- Spring AI with OpenAI integration
- Error Prone with NullAway for null safety
- P6Spy for SQL query logging

**Frontend:**
- Vue 3.5 with Composition API
- TypeScript 5.8
- Vite 7 (build tool)
- Pinia (state management)
- Vue Router 4.5
- TailwindCSS 4.1
- Vitest (unit testing)
- Playwright (e2e testing)

**Build & Tools:**
- Gradle 9.x with Kotlin DSL
- Lombok for code generation
- Apache POI for Excel processing
- ArchUnit for architecture validation

### Architecture

The project follows **Hexagonal Architecture** (Ports & Adapters) with three main layers:

```
kr.co.abacus.abms/
├── domain/           # Core business logic, entities, value objects
├── application/      # Use cases, application services, port interfaces
└── adapter/          # External interfaces (REST API, persistence, integrations, security)
```

Layer rules are enforced by ArchUnit tests to maintain architectural integrity.

## Requirements

### Prerequisites

- **Java 25**: Configured via Gradle toolchain
- **Gradle 9.x**: Wrapper included (`./gradlew`)
- **Docker**: For MySQL database
- **Docker Compose**: For container orchestration
- **Node.js & npm**: For frontend development (if working with UI)

### Environment Variables

Create a `.env` file in the project root:

```properties
AI_OPENAI_API_KEY=your_openai_api_key_here
```

**Note:** The application will start without this key, but AI chat features will not be available.

## Getting Started

### 1. Clone the Repository

```bash
git clone <repository-url>
cd abms
```

### 2. Set Up Environment Variables

```bash
# Create .env file
echo "AI_OPENAI_API_KEY=your_openai_api_key_here" > .env
```

### 3. Start Database

```bash
docker compose up -d
```

**Database Configuration:**
- Service: MySQL 8.x
- Host: `localhost`
- Port: **3307** (not standard 3306)
- Database: `abms`
- Username: `myuser`
- Password: `secret`
- Root Password: `verysecret`

**Note:** Spring Boot manages the Docker Compose lifecycle (configured as `start_only`). The database persists between application restarts.

### 4. Build the Project

```bash
./gradlew build
```

This will:
- Compile Java sources with Error Prone + NullAway static analysis
- Generate QueryDSL Q-classes
- Run all tests
- Create executable JAR

### 5. Run the Application

```bash
./gradlew bootRun
```

The server will start on `http://localhost:8080`.

**Key Features Enabled:**
- Virtual threads for improved concurrency
- JPA schema auto-creation (`ddl-auto: create`)
- Sample data initialization (via `InitData` class in `local`/`default` profiles)
- P6Spy SQL logging with actual parameters

### 6. Frontend Development (Optional)

```bash
cd frontend
npm install
npm run dev
```

The frontend development server will start on `http://localhost:5173`.

## Available Scripts

### Backend (Gradle)

```bash
# Build project
./gradlew build

# Run application
./gradlew bootRun

# Run tests
./gradlew test

# Run specific test class
./gradlew test --tests "kr.co.abacus.abms.domain.shared.MoneyTest"

# Run specific test method
./gradlew test --tests "kr.co.abacus.abms.domain.shared.MoneyTest.add"

# Clean build artifacts
./gradlew clean

# Generate AsciiDoc documentation
./gradlew asciidoctor
```

### Frontend (npm)

```bash
# Start development server (port 5173)
npm run dev

# Build for production
npm run build

# Preview production build
npm run preview

# Run unit tests
npm run test

# Lint TypeScript and Vue files
npm run lint

# Fix linting issues
npm run lint:fix

# Type checking
npm run typecheck

# Format code
npm run format

# Format and write
npm run format:write
```

### Docker

```bash
# Start database
docker compose up -d

# Stop database
docker compose down

# Stop and remove volumes (full reset)
docker compose down -v

# View logs
docker compose logs -f

# Check status
docker compose ps
```

## Testing

### Test Organization

Tests follow the hexagonal architecture structure:

```
src/test/java/kr/co/abacus/abms/
├── domain/                    # Domain model tests (unit tests)
├── application/              # Application service tests (integration)
├── adapter/api/              # API tests (integration)
├── support/                  # Test base classes and utilities
└── HexagonalArchitectureTest.java  # ArchUnit tests
```

### Test Types

1. **Domain Unit Tests**: Pure unit tests for domain models and value objects
2. **Application Integration Tests**: Service tests with Spring context and database
3. **API Integration Tests**: REST API tests using MockMvc and RestTestClient
4. **Architecture Tests**: ArchUnit validation of hexagonal architecture rules

### Running Tests

```bash
# Run all tests
./gradlew test

# Run with coverage
./gradlew test jacocoTestReport

# Run specific test
./gradlew test --tests "EmployeeTest"

# Run tests in a package
./gradlew test --tests "kr.co.abacus.abms.domain.*"
```

### Test Configuration

- **Mockito Agent**: Automatically attached via JVM args for Java 25 compatibility
- **Test Profile**: Uses H2 in-memory database (not MySQL)
- **Transactions**: Auto-rollback after each test (via `@Transactional`)
- **NullAway**: Disabled for test code

## Project Structure

### Backend Structure

```
src/main/java/kr/co/abacus/abms/
├── AbmsApplication.java           # Spring Boot entry point
├── domain/                        # Domain layer (business logic)
│   ├── employee/                  # Employee aggregate
│   ├── department/                # Department aggregate
│   ├── payroll/                   # Payroll aggregate
│   ├── project/                   # Project aggregate
│   ├── contract/                  # Contract aggregate
│   ├── account/                   # Account aggregate
│   ├── party/                     # Party aggregate
│   ├── positionhistory/           # Position history aggregate
│   ├── projectassignment/         # Project assignment aggregate
│   ├── chat/                      # Chat aggregate
│   └── shared/                    # Shared value objects (Money, Period, etc.)
├── application/                   # Application layer (use cases)
│   ├── employee/
│   │   ├── inbound/               # Use cases (Manager, Finder)
│   │   ├── outbound/              # Ports (repository interfaces)
│   │   └── dto/                   # Commands/queries
│   ├── department/
│   ├── payroll/
│   ├── project/
│   ├── positionhistory/
│   ├── party/
│   └── chat/                      # AI chat service
└── adapter/                       # Adapter layer (external interfaces)
    ├── api/                       # REST controllers and DTOs
    │   └── common/InitData.java   # Sample data initialization
    ├── infrastructure/            # JPA repository implementations
    ├── security/                  # Security configuration
    └── integration/               # External API clients

src/main/resources/
└── application.yml                # Application configuration

src/test/resources/
├── application.yml                # Test profile configuration
└── junit-platform.properties      # JUnit platform settings

src/test/java/kr/co/abacus/abms/
├── domain/                        # Domain tests
├── application/                   # Service tests
├── adapter/api/                  # API tests
├── support/                       # Test utilities
│   ├── IntegrationTestBase.java
│   ├── ApiIntegrationTestBase.java
│   └── AssertThatUtils.java
└── HexagonalArchitectureTest.java
```

### Frontend Structure

```
frontend/
├── src/
│   ├── main.ts                    # Application entry point
│   ├── App.vue                    # Root component
│   ├── components/                # Reusable Vue components
│   ├── views/                     # Page components
│   ├── stores/                    # Pinia state management
│   ├── router/                    # Vue Router configuration
│   ├── services/                  # API service layer
│   ├── types/                     # TypeScript type definitions
│   └── assets/                    # Static assets
├── public/                        # Public static files
├── package.json                   # npm dependencies and scripts
├── vite.config.ts                 # Vite configuration
├── tsconfig.json                  # TypeScript configuration
└── tailwind.config.js             # TailwindCSS configuration
```

### Configuration Files

- `build.gradle.kts`: Gradle build configuration with dependencies
- `settings.gradle.kts`: Gradle project settings
- `compose.yaml`: Docker Compose configuration for MySQL
- `.env`: Environment variables (API keys)
- `spy.log`: P6Spy SQL query log (generated at runtime)
- `HELP.md`: Spring Boot generated help documentation
- `AGENTS.md`: Agent-related documentation

## API Documentation

### Base URL

- Backend: `http://localhost:8080`
- Frontend: `http://localhost:5173` (dev server)

### Key Endpoints

**Employee Management:**
- `GET /api/employees` - List all employees
- `GET /api/employees/{id}` - Get employee by ID
- `POST /api/employees` - Create new employee
- `PUT /api/employees/{id}` - Update employee
- `DELETE /api/employees/{id}` - Delete employee (soft delete)
- `GET /api/employees/excel/download` - Download employees as Excel
- `POST /api/employees/excel/upload` - Upload employees from Excel

**Department Management:**
- `GET /api/departments/organization-chart` - Organization chart
- `GET /api/departments/{departmentId}` - Get department details
- `GET /api/departments/{departmentId}/employees` - List department employees
- `POST /api/departments/{departmentId}/assign-team-leader` - Assign team leader

**Payroll:**
- TODO - Document payroll endpoints

**Project Management:**
- TODO - Document project endpoints

**AI Chat:**
- `POST /api/v1/chat/message` - Send chat message to AI assistant
- `POST /api/v1/chat/stream` - Stream chat responses (SSE)
- `GET /api/v1/chat/sessions` - List recent chat sessions
- `GET /api/v1/chat/sessions/favorites` - List favorite chat sessions
- `GET /api/v1/chat/sessions/{sessionId}` - Get chat session details
- `POST /api/v1/chat/sessions/{sessionId}/favorite` - Toggle favorite

**Actuator:**
- `GET /actuator/health` - Health check endpoint

## Development

### Code Style

The project follows strict code style conventions:

- **Indentation**: 4 spaces
- **Null Safety**: JSpecify with `@NullMarked` on packages, `@Nullable` for nullable fields
- **Static Analysis**: Error Prone with NullAway enabled (compile-time null checking)
- **Naming**: 
  - Entities: `Employee`, `Department`
  - Services: `EmployeeManager` (write), `EmployeeFinder` (read)
  - Repositories: `EmployeeRepository`
  - DTOs: `EmployeeCreateRequest`, `EmployeeResponse`

### Null Safety

All packages must have `package-info.java`:

```java
@NullMarked
package kr.co.abacus.abms.domain.employee;

import org.jspecify.annotations.NullMarked;
```

Use `@Nullable` for nullable fields/parameters:

```java
import org.jspecify.annotations.Nullable;

@Nullable
private String optionalField;
```

### Domain-Driven Design

- **Aggregates**: Each aggregate root manages its own consistency
- **Value Objects**: Immutable records (e.g., `Money`, `Email`, `Period`)
- **Factories**: Static factory methods for entity creation (`create()`, `startWith()`)
- **Soft Delete**: All entities support soft deletion via `BaseEntity`
- **Domain Events**: Can be published for cross-aggregate coordination

### QueryDSL

Generated Q-classes are located in `build/generated/sources/annotationProcessor/`. If you get compilation errors about missing Q-classes:

```bash
./gradlew clean build
```

## Profiles

### Available Profiles

- `default`: Default profile with sample data initialization (uses MySQL via Docker)
- `local`: Local development with sample data (uses MySQL via Docker)
- `test`: Test profile (uses H2 in-memory database)

### Activating Profiles

**Via Gradle:**
```bash
./gradlew bootRun --args='--spring.profiles.active=local'
```

**Via Environment Variable:**
```bash
export SPRING_PROFILES_ACTIVE=local
./gradlew bootRun
```

### Profile-Specific Configuration

- `local`/`default`: Initializes sample departments and employees via `InitData`
- `test`: Uses H2 database, no sample data initialization

## Troubleshooting

### Common Issues

**Issue: NullAway compilation errors**

Solution: Ensure all packages have `package-info.java` with `@NullMarked` annotation.

**Issue: QueryDSL Q-classes not found**

Solution: Clean and rebuild the project:
```bash
./gradlew clean build
```

**Issue: Mockito errors in tests**

Solution: Mockito agent is auto-configured. Verify `jvmArgs("-javaagent:${mockitoAgent.asPath}")` in `build.gradle.kts`.

**Issue: Database connection fails**

Solution: 
- Verify Docker is running: `docker compose ps`
- Check port 3307 (not 3306): `netstat -an | grep 3307`
- Restart database: `docker compose restart mysql`

**Issue: ArchUnit tests fail**

Solution: Ensure code follows hexagonal architecture rules:
- Domain layer should not depend on application or adapter
- Application layer should not depend on adapter
- Check import statements in failing classes

**Issue: Port 8080 already in use**

Solution: Change the port in `application.yml`:
```yaml
server:
  port: 8081
```

Or kill the process using port 8080:
```bash
# macOS/Linux
lsof -ti:8080 | xargs kill -9
```

---

**Version:** 0.0.2-SNAPSHOT  
**Last Updated:** 2026-01-27
