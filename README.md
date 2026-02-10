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

The project follows **Hexagonal Architecture** (Ports & Adapters) with three main layers:

```text
kr.co.abacus.abms/
├── domain/           # Core business logic, entities, value objects
├── application/      # Use cases, application services, port interfaces (inbound/outbound)
└── adapter/          # External interfaces (REST API, persistence, integrations, security)
```

Layer rules are enforced by ArchUnit tests to maintain architectural integrity.

## Requirements

### Prerequisites

- **Java 25**: Configured via Gradle toolchain
- **Gradle 9.x**: Wrapper included (`./gradlew`)
- **Docker**: For MySQL database
- **Docker Compose**: For container orchestration
- **Node.js 22+ & npm**: For frontend development

### Environment Variables

Configure OpenAI API Key.

Create a `.env` file in the project root:

```properties
AI_OPENAI_API_KEY=sk-proj-xxxxxxxxxxxxxxxxxxxxxxxx
```

**Note:** If the API key is missing or invalid, AI chat features will fail.

## Getting Started

### 1. Clone the Repository

```bash
git clone <repository-url>
cd abms
```

### 2. Set Up Environment Variables

```bash
# Create .env file
echo "AI_OPENAI_API_KEY=sk-proj-..." > .env
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

### 6. Frontend Development

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

# Clean build artifacts
./gradlew clean
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
src/test/java/kr/co/abacus/abms/
├── domain/                    # Domain model tests (unit tests)
├── application/               # Application service tests (integration)
├── adapter/api/               # API tests (integration)
├── support/                   # Test base classes and utilities
└── HexagonalArchitectureTest.java  # ArchUnit tests
```

### Test Types

1. **Domain Unit Tests**: Pure unit tests for domain models and value objects
2. **Application Integration Tests**: Service tests with Spring context and database
3. **API Integration Tests**: REST API tests using MockMvc and RestTestClient
4. **Architecture Tests**: ArchUnit validation of hexagonal architecture rules

## Project Structure

### Backend Structure

```text
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
│   ├── project/
│   ├── contract/
│   ├── party/
│   └── chat/                      # AI chat service
└── adapter/                       # Adapter layer (external interfaces)
    ├── api/                       # REST controllers and DTOs
    │   └── common/InitData.java   # Sample data initialization
    ├── infrastructure/            # JPA repository implementations
    ├── security/                  # Security configuration
    └── integration/               # External API clients
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

**Last Updated:** 2026-02-10
