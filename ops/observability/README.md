# ABMS Observability Stack

## 구성
- Prometheus: `/actuator/prometheus` 스크랩
- Grafana: 메트릭/로그 조회 및 알림
- Loki + Promtail: JSON 로그 수집

## 실행
```bash
cd ops/observability
docker compose up -d
```

## 사전 조건
- API 서버는 `APP_LOG_FILE_NAME` 또는 기본값 `logs/abms-api.log`로 JSON 로그를 파일에 기록해야 합니다.
- 운영 서버에서 `ABMS_LOG_DIR`를 실제 로그 디렉터리로 맞춰야 합니다.
- Prometheus는 기본값으로 `host.docker.internal:8080/actuator/prometheus`를 스크랩합니다.
- 다른 주소를 사용하려면 `ABMS_API_TARGET` 환경 변수로 API 타깃을 지정합니다.
- API는 기본적으로 loopback과 사설 대역(`10.0.0.0/8`, `172.16.0.0/12`, `192.168.0.0/16`)에서의 actuator 메트릭 접근을 허용합니다. 더 좁히려면 `APP_ACTUATOR_ALLOWED_IP_RANGES`를 설정합니다.
- Grafana 기본 계정은 `admin` / `admin` 입니다. 바꾸려면 `GRAFANA_ADMIN_USER`, `GRAFANA_ADMIN_PASSWORD` 를 설정합니다.
- Prometheus, Loki, Promtail, Grafana 데이터는 Docker named volume 으로 유지됩니다.

## 기본 확인 포인트
- Grafana: `http://localhost:3000`
- Prometheus: `http://localhost:9090`
- Loki: `http://localhost:3100`
