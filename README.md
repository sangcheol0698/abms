# ABMS

A modern business management system built with hexagonal architecture, featuring employee management, payroll processing, project tracking, and AI-powered chat assistance.

## Overview

ABMS is an enterprise-grade business management system that demonstrates hexagonal architecture principles with a clear separation between domain logic, application services, and external adapters. The system includes:

- **Employee Management**: Complete employee lifecycle management with department organization
- **Payroll Processing**: Automated payroll calculations and processing
- **Project & Contract Management**: Project tracking with employee assignments and contract management
- **AI Chat Assistant**: Integration with OpenAI for natural language queries
- **Excel Import/Export**: Bulk operations support via Excel files

### Technology Stack

**Backend:**
- **Java 25**
- **Spring Boot 4.0.2**
- Spring Data JPA with QueryDSL 7.0
- MySQL 8.x (via Docker Compose)
- H2 Database (for testing)
- Spring AI 2.0.0-M2 with OpenAI integration
- Error Prone with NullAway for null safety
- P6Spy 2.0.0 for SQL query logging

**Frontend:**
- **Vue 3.5.21** with Composition API
- **TypeScript 5.8.3**
- **Vite 7.1.6** (build tool)
- Pinia 3.0.3 (state management)
- Vue Router 4.5
- TailwindCSS 4.1.13
- Reka UI (Headless UI components)
- Vitest & Playwright (testing)

**Build & Tools:**
- **Gradle 9.x** with Kotlin DSL
- Lombok for code generation
- Apache POI 5.4.1 for Excel processing
- ArchUnit for architecture validation

### Architecture

The project follows **Hexagonal Architecture** (Ports & Adapters) and is organized as a Gradle multi-module backend:

```text
abms/
├── abms-domain               # Domain entities, value objects, domain rules
├── abms-application          # Use cases and inbound/outbound ports
├── abms-adapter-web          # REST API, request/response DTOs, HTTP security config
├── abms-adapter-persistence  # JPA/QueryDSL persistence adapters
├── abms-adapter-integration  # Mail/cache/AI/security services and external integrations
├── abms-adapter-batch        # Spring Batch jobs/steps
├── abms-api-boot             # API runtime assembly and boot entrypoint
├── abms-batch-boot           # Batch runtime assembly and boot entrypoint
└── frontend                  # Vue 3 frontend (outside Gradle multi-module scope)
```

Layer dependency rules are enforced by ArchUnit tests to maintain architectural integrity.

## Requirements

### Prerequisites

- **Java 25**: Configured via Gradle toolchain
- **Gradle 9.x**: Wrapper included (`./gradlew`)
- **Docker**: For MySQL database
- **Docker Compose**: For container orchestration
- **Node.js 22+ & npm**: For frontend development

### Environment Variables

The backend is separated by Spring profiles:
- `local` (default): uses project root `.env` (`spring.config.import`)
- `dev`: uses OS/CI environment variables
- `prod`: uses OS/CI environment variables
- `test`: uses `abms-api-boot/src/test/resources/application-test.yml`

Create local env from template:

```bash
cp .env.example .env
```

`dev`/`prod` require these variables from runtime environment:
- `DB_URL`
- `DB_USERNAME`
- `DB_PASSWORD`
- `AI_OPENAI_API_KEY`

## Getting Started

### 1. Clone the Repository

```bash
git clone <repository-url>
cd abms
```

### 2. Set Up Environment Variables

```bash
# Local profile
cp .env.example .env
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
# API runtime (root alias)
./gradlew bootRun

# API runtime (direct module task)
./gradlew :abms-api-boot:bootRun

# Batch runtime (root alias)
./gradlew bootRunBatch

# Batch runtime with explicit job name
./gradlew :abms-batch-boot:bootRun --args='--spring.batch.job.name=revenueMonthlySummaryJob'
```

The server will start on `http://localhost:8080`.

**Runtime Defaults:**
- Virtual threads for improved concurrency
- JPA schema auto-creation (`ddl-auto: create`)
- Sample data initialization (via `InitData` class in `local` profile)
- P6Spy SQL logging with actual parameters
- API boot default: `spring.batch.job.enabled=false`
- Batch boot default: `spring.batch.job.enabled=true`

### 6. Frontend Development

```bash
cd frontend
npm install
npm run dev
```

The frontend development server will start on `http://localhost:5173`.
`frontend/.env.development` is used for development mode and `frontend/.env.production` for production build.

## Available Scripts

### Backend (Gradle)

```bash
# Build project
./gradlew build

# Run API application (root alias)
./gradlew bootRun

# Run API application (direct module task)
./gradlew :abms-api-boot:bootRun

# Run Batch application (root alias)
./gradlew bootRunBatch

# Run Batch application with explicit job
./gradlew :abms-batch-boot:bootRun --args='--spring.batch.job.name=employeeCostJob'

# Run tests
./gradlew test

# Run specific test class
./gradlew :abms-domain:test --tests "kr.co.abacus.abms.domain.shared.MoneyTest"

# Clean build artifacts
./gradlew clean
```

### Frontend (npm)

```bash
# Start development server (port 5173)
npm run dev

# Uses frontend/.env.production by default
# Build for production
npm run build

# Preview production build
npm run preview

# Run unit tests
npm run test

# Lint & Fix
npm run lint:fix

# Type checking
npm run typecheck
```

### Docker

```bash
# Start database
docker compose up -d

# Stop database
docker compose down

# Stop and remove volumes (full reset)
docker compose down -v
```

## Testing

### Test Organization

Tests follow the hexagonal architecture structure:

```text
abms-domain/src/test/java/kr/co/abacus/abms/
└── domain/                            # Domain model unit tests

abms-api-boot/src/test/java/kr/co/abacus/abms/
├── adapter/api/                       # API integration tests
├── support/                           # Integration test base/utilities
└── HexagonalArchitectureTest.java     # ArchUnit tests

abms-batch-boot/src/test/java/kr/co/abacus/abms/
└── adapter/batch/                     # Batch integration tests
```

### Test Types

1. **Domain Unit Tests**: Pure unit tests for domain models and value objects
2. **Application Integration Tests**: Service tests with Spring context and database
3. **API Integration Tests**: REST API tests using MockMvc and RestTestClient
4. **Architecture Tests**: ArchUnit validation of hexagonal architecture rules

## Project Structure

### Backend Structure

```text
abms-domain/src/main/java/kr/co/abacus/abms/domain
abms-application/src/main/java/kr/co/abacus/abms/application
abms-adapter-web/src/main/java/kr/co/abacus/abms/adapter
abms-adapter-persistence/src/main/java/kr/co/abacus/abms/adapter
abms-adapter-integration/src/main/java/kr/co/abacus/abms/adapter
abms-adapter-batch/src/main/java/kr/co/abacus/abms/adapter
abms-api-boot/src/main/java/kr/co/abacus/abms/AbmsApiApplication.java
abms-batch-boot/src/main/java/kr/co/abacus/abms/AbmsBatchApplication.java
```

### Frontend Structure

```text
frontend/
├── src/
│   ├── main.ts                    # Application entry point
│   ├── App.vue                    # Root component
│   ├── core/                      # Core infrastructure (API, Router, Utils)
│   ├── components/                # Shared UI components
│   ├── features/                  # Feature-based modules (Chat, Employee, etc.)
│   ├── assets/                    # Static assets
│   └── style.css                  # Global styles (Tailwind)
├── public/                        # Public static files
├── package.json                   # npm dependencies and scripts
└── vite.config.ts                 # Vite configuration
```

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

**AI Chat:**
- `POST /api/v1/chat/message` - Send chat message to AI assistant
- `POST /api/v1/chat/stream` - Stream chat responses (SSE)
- `GET /api/v1/chat/sessions` - List recent chat sessions

---

**Last Updated:** 2026-03-01
