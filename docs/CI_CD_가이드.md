# CI/CD 가이드

## 개요
- CI: GitHub Actions에서 백엔드 테스트와 프런트 검증을 수행합니다.
- CD: Actions 탭에서 `Deploy Dev` 워크플로우를 수동 실행해 개발 서버에 배포합니다.
- 배포 성공 시 선택적으로 개발 배포 태그를 생성합니다.

## 워크플로우
- `.github/workflows/ci.yml`
  - 백엔드: `./gradlew test`
  - 프런트: `npm run lint`, `npm run typecheck`, `npm run test:unit`, `npm run build`
- `.github/workflows/deploy-dev.yml`
  - `workflow_dispatch` 로 수동 실행
  - 입력값 `ref` 로 배포 대상 브랜치 또는 커밋 지정
  - 입력값 `create_tag` 로 배포 태그 생성 여부 지정
  - 프런트 빌드 결과를 `abms-api-boot` 정적 리소스로 복사
  - `./gradlew clean build`
  - 개발 서버에 API/Batch JAR 업로드
  - `ops/observability` 업로드
  - API 재기동 후 `/actuator/health` 확인
  - Prometheus target `abms-api` 가 `UP` 인지 확인
  - 성공 시 `deploy-dev-YYYYMMDD-HHMMSS-<shortsha>` 형식의 git 태그 생성

## GitHub Secrets
- `DEV_SERVER_SSH_KEY`
  - 개발 서버 접속용 private key

## GitHub Variables
- `DEV_SERVER_HOST`
  - 개발 서버 호스트 또는 IP
- `DEV_SERVER_USER`
  - 기본값: `ubuntu`
- `DEV_SERVER_LOG_DIR`
  - 기본값: `$HOME/logs/abms`
- `DEV_SERVER_APP_DIR`
  - 기본값: `$HOME`
- `DEV_SERVER_OBSERVABILITY_DIR`
  - 기본값: `$HOME/ops`
- `DEV_SERVER_OBSERVABILITY_SCP_DIR`
  - 기본값: `~/ops`
- `DEV_SERVER_ENV_FILE`
  - 기본값: `$HOME/.env`
- `DEV_SERVER_DOCKER_COMPOSE_CMD`
  - 기본값: `sudo docker compose`

## 서버 사전 조건
- Java 25 런타임 설치
- Docker 및 `docker compose` 사용 가능
- `DEV_SERVER_ENV_FILE` 에 `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` 등 애플리케이션 환경변수 존재
- 개발 서버에서 `sudo docker compose` 를 비대화형으로 실행할 수 있어야 합니다.

## 권장 운영 방식
- PR 검증은 `CI` 로만 처리합니다.
- 개발 서버 반영은 Actions 탭에서 `Deploy Dev` 를 수동 실행합니다.
- 배포 실패 시 GitHub Actions 로그와 개발 서버의 `abms-api.log` 및 `docker compose ps` 를 먼저 확인합니다.
