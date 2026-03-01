# ABMS

헥사고날 아키텍처로 구축된 현대적인 비즈니스 관리 시스템으로, 직원 관리, 급여 처리, 프로젝트 추적 및 AI 기반 채팅 지원 기능을 제공합니다.

## 개요

ABMS는 도메인 로직, 애플리케이션 서비스 및 외부 어댑터 간의 명확한 분리를 통해 헥사고날 아키텍처 원칙을 입증하는 엔터프라이즈급 비즈니스 관리 시스템입니다. 시스템은 다음을 포함합니다:

- **직원 관리**: 부서 조직을 포함한 전체 직원 수명 주기 관리
- **급여 처리**: 자동화된 급여 계산 및 처리
- **프로젝트 및 계약 관리**: 직원 배정 및 계약 관리를 포함한 프로젝트 추적
- **AI 채팅 어시스턴트**: 자연어 질의를 위한 OpenAI 통합
- **Excel 가져오기/내보내기**: Excel 파일을 통한 대량 작업 지원

### 기술 스택

**백엔드:**
- **Java 25**
- **Spring Boot 4.0.2**
- Spring Data JPA 및 QueryDSL 7.0
- MySQL 8.x (Docker Compose 사용)
- H2 Database (테스트용)
- Spring AI 2.0.0-M2 (OpenAI 통합)
- Error Prone 및 NullAway (Null 안전성)
- P6Spy 2.0.0 (SQL 쿼리 로깅)

**프론트엔드:**
- **Vue 3.5.21** (Composition API)
- **TypeScript 5.8.3**
- **Vite 7.1.6** (빌드 도구)
- Pinia 3.0.3 (상태 관리)
- Vue Router 4.5
- TailwindCSS 4.1.13
- Reka UI (Headless UI 컴포넌트)
- Vitest 및 Playwright (테스트)

**빌드 및 도구:**
- **Gradle 9.x** (Kotlin DSL)
- Lombok (코드 생성)
- Apache POI 5.4.1 (Excel 처리)
- ArchUnit (아키텍처 검증)

### 아키텍처

이 프로젝트는 **헥사고날 아키텍처** (Ports & Adapters)를 따르며 Gradle 멀티 모듈 백엔드로 구성되어 있습니다:

```text
abms/
├── abms-domain               # 도메인 엔티티, 값 객체, 도메인 규칙
├── abms-application          # 유스케이스 및 인바운드/아웃바운드 포트
├── abms-adapter-web          # REST API, 요청/응답 DTO, HTTP 보안 설정
├── abms-adapter-persistence  # JPA/QueryDSL 영속성 어댑터
├── abms-adapter-integration  # 메일/캐시/AI/보안 서비스 및 외부 통합
├── abms-adapter-batch        # Spring Batch 작업/단계
├── abms-api-boot             # API 런타임 조립 및 부트 진입점
├── abms-batch-boot           # 배치 런타임 조립 및 부트 진입점
└── frontend                  # Vue 3 프론트엔드 (Gradle 멀티 모듈 범위 밖)
```

계층 의존성 규칙은 아키텍처 무결성을 유지하기 위해 ArchUnit 테스트에 의해 강제됩니다.

## 요구 사항

### 필수 조건

- **Java 25**: Gradle 툴체인을 통해 구성됨
- **Gradle 9.x**: 래퍼 포함 (`./gradlew`)
- **Docker**: MySQL 데이터베이스용
- **Docker Compose**: 컨테이너 오케스트레이션용
- **Node.js 22+ 및 npm**: 프론트엔드 개발용

### 환경 변수

백엔드는 Spring 프로파일로 구분됩니다:
- `local` (기본값): 프로젝트 루트 `.env` 사용 (`spring.config.import`)
- `dev`: OS/CI 환경 변수 사용
- `prod`: OS/CI 환경 변수 사용
- `test`: `abms-api-boot/src/test/resources/application-test.yml` 사용

템플릿에서 로컬 env 생성:

```bash
cp .env.example .env
```

`dev`/`prod`는 런타임 환경에서 다음 변수가 필요합니다:
- `DB_URL`
- `DB_USERNAME`
- `DB_PASSWORD`
- `AI_OPENAI_API_KEY`

## 시작하기

### 1. 저장소 복제

```bash
git clone <repository-url>
cd abms
```

### 2. 환경 변수 설정

```bash
# 로컬 프로파일
cp .env.example .env
```

### 3. 데이터베이스 시작

```bash
docker compose up -d
```

**데이터베이스 구성:**
- 서비스: MySQL 8.x
- 호스트: `localhost`
- 포트: **3307** (표준 3306 아님)
- 데이터베이스: `abms`
- 사용자명: `myuser`
- 비밀번호: `secret`
- 루트 비밀번호: `verysecret`

**참고:** Spring Boot는 Docker Compose 수명 주기를 관리합니다 (`start_only`로 구성됨). 데이터베이스는 애플리케이션 재시작 간에 유지됩니다.

### 4. 프로젝트 빌드

```bash
./gradlew build
```

이 작업은 다음을 수행합니다:
- Error Prone + NullAway 정적 분석으로 Java 소스 컴파일
- QueryDSL Q-클래스 생성
- 모든 테스트 실행
- 실행 가능한 JAR 생성

### 5. 애플리케이션 실행

```bash
# API 런타임 (루트 별칭)
./gradlew bootRun

# API 런타임 (직접 모듈 태스크)
./gradlew :abms-api-boot:bootRun

# 배치 런타임 (루트 별칭)
./gradlew bootRunBatch

# 명시적 작업 이름으로 배치 런타임 실행
./gradlew :abms-batch-boot:bootRun --args='--spring.batch.job.name=revenueMonthlySummaryJob'
```

서버는 `http://localhost:8080`에서 시작됩니다.

**런타임 기본값:**
- 향상된 동시성을 위한 가상 스레드 (Virtual threads)
- JPA 스키마 자동 생성 (`ddl-auto: create`)
- 샘플 데이터 초기화 (`local` 프로파일의 `InitData` 클래스 통해)
- 실제 파라미터가 포함된 P6Spy SQL 로깅
- API 부트 기본값: `spring.batch.job.enabled=false`
- 배치 부트 기본값: `spring.batch.job.enabled=true`

### 6. 프론트엔드 개발

```bash
cd frontend
npm install
npm run dev
```

프론트엔드 개발 서버는 `http://localhost:5173`에서 시작됩니다.
`frontend/.env.development`는 개발 모드에 사용되며, `frontend/.env.production`은 프로덕션 빌드에 사용됩니다.

## 사용 가능한 스크립트

### 백엔드 (Gradle)

```bash
# 프로젝트 빌드
./gradlew build

# API 애플리케이션 실행 (루트 별칭)
./gradlew bootRun

# API 애플리케이션 실행 (직접 모듈 태스크)
./gradlew :abms-api-boot:bootRun

# 배치 애플리케이션 실행 (루트 별칭)
./gradlew bootRunBatch

# 명시적 작업으로 배치 애플리케이션 실행
./gradlew :abms-batch-boot:bootRun --args='--spring.batch.job.name=employeeCostJob'

# 테스트 실행
./gradlew test

# 특정 테스트 클래스 실행
./gradlew :abms-domain:test --tests "kr.co.abacus.abms.domain.shared.MoneyTest"

# 빌드 아티팩트 정리
./gradlew clean
```

### 프론트엔드 (npm)

```bash
# 개발 서버 시작 (포트 5173)
npm run dev

# 기본적으로 frontend/.env.production 사용
# 프로덕션용 빌드
npm run build

# 프로덕션 빌드 미리보기
npm run preview

# 단위 테스트 실행
npm run test

# 린트 및 수정
npm run lint:fix

# 타입 체크
npm run typecheck
```

### Docker

```bash
# 데이터베이스 시작
docker compose up -d

# 데이터베이스 중지
docker compose down

# 볼륨 중지 및 제거 (전체 초기화)
docker compose down -v
```

## 테스트

### 테스트 구성

테스트는 헥사고날 아키텍처 구조를 따릅니다:

```text
abms-domain/src/test/java/kr/co/abacus/abms/
└── domain/                            # 도메인 모델 단위 테스트

abms-api-boot/src/test/java/kr/co/abacus/abms/
├── adapter/api/                       # API 통합 테스트
├── support/                           # 통합 테스트 베이스/유틸리티
└── HexagonalArchitectureTest.java     # ArchUnit 테스트

abms-batch-boot/src/test/java/kr/co/abacus/abms/
└── adapter/batch/                     # 배치 통합 테스트
```

### 테스트 유형

1. **도메인 단위 테스트**: 도메인 모델 및 값 객체에 대한 순수 단위 테스트
2. **애플리케이션 통합 테스트**: Spring 컨텍스트 및 데이터베이스를 포함한 서비스 테스트
3. **API 통합 테스트**: MockMvc 및 RestTestClient를 사용한 REST API 테스트
4. **아키텍처 테스트**: 헥사고날 아키텍처 규칙에 대한 ArchUnit 검증

## 프로젝트 구조

### 백엔드 구조

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

### 프론트엔드 구조

```text
frontend/
├── src/
│   ├── main.ts                    # 애플리케이션 진입점
│   ├── App.vue                    # 루트 컴포넌트
│   ├── core/                      # 핵심 인프라 (API, 라우터, 유틸리티)
│   ├── components/                # 공유 UI 컴포넌트
│   ├── features/                  # 기능 기반 모듈 (채팅, 직원 등)
│   ├── assets/                    # 정적 자산
│   └── style.css                  # 전역 스타일 (Tailwind)
├── public/                        # 공용 정적 파일
├── package.json                   # npm 의존성 및 스크립트
└── vite.config.ts                 # Vite 설정
```

## API 문서

### 기본 URL

- 백엔드: `http://localhost:8080`
- 프론트엔드: `http://localhost:5173` (개발 서버)

### 주요 엔드포인트

**직원 관리:**
- `GET /api/employees` - 모든 직원 목록 조회
- `GET /api/employees/{id}` - ID로 직원 조회
- `POST /api/employees` - 새 직원 생성
- `PUT /api/employees/{id}` - 직원 정보 수정
- `DELETE /api/employees/{id}` - 직원 삭제 (소프트 삭제)

**AI 채팅:**
- `POST /api/v1/chat/message` - AI 어시스턴트에게 채팅 메시지 전송
- `POST /api/v1/chat/stream` - 채팅 응답 스트리밍 (SSE)
- `GET /api/v1/chat/sessions` - 최근 채팅 세션 목록 조회

---

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
