#!/bin/bash
set -e

KEY_FILE="${KEY_FILE:-$HOME/.ssh/your-key.pem}"
SERVER_IP="${SERVER_IP:-YOUR_SERVER_IP}"
LOG_DIR="${LOG_DIR:-\$HOME/logs/abms}"
REMOTE_APP_DIR="${REMOTE_APP_DIR:-\$HOME}"
REMOTE_OBSERVABILITY_DIR="${REMOTE_OBSERVABILITY_DIR:-\$HOME/ops}"

npm run build --prefix ./frontend

rm -rf ./abms-api-boot/src/main/resources/static/*

mkdir -p ./abms-api-boot/src/main/resources/static
cp -r ./frontend/dist/* ./abms-api-boot/src/main/resources/static/

./gradlew clean build

mv ./abms-api-boot/build/libs/*.jar ./abms-api-boot/build/libs/abms-api.jar
mv ./abms-batch-boot/build/libs/*.jar ./abms-batch-boot/build/libs/abms-batch.jar

scp -i "$KEY_FILE" \
    ./abms-api-boot/build/libs/abms-api.jar \
    ubuntu@"$SERVER_IP":~

scp -i "$KEY_FILE" \
    ./abms-batch-boot/build/libs/abms-batch.jar \
    ubuntu@"$SERVER_IP":~/

ssh -i "$KEY_FILE" ubuntu@"$SERVER_IP" "mkdir -p $REMOTE_OBSERVABILITY_DIR"
scp -i "$KEY_FILE" -r \
    ./ops/observability \
    ubuntu@"$SERVER_IP":"$REMOTE_OBSERVABILITY_DIR"/

ssh -i "$KEY_FILE" ubuntu@"$SERVER_IP" <<EOF
set -e
LOG_DIR="$LOG_DIR"
REMOTE_APP_DIR="$REMOTE_APP_DIR"
REMOTE_OBSERVABILITY_DIR="$REMOTE_OBSERVABILITY_DIR"

mkdir -p "\$LOG_DIR"
pkill -f "abms-api.jar" || true
set -a
source .env
set +a

cd "\$REMOTE_APP_DIR"
nohup env APP_ENVIRONMENT=dev APP_LOG_FILE_NAME="\$LOG_DIR/abms-api.log" java -jar abms-api.jar --spring.profiles.active=dev > /dev/null 2>&1 &
echo \$! > "\$LOG_DIR/abms-api.pid"
for i in \$(seq 1 30); do
  if curl -sf http://127.0.0.1:8080/actuator/health > /dev/null; then
    break
  fi
  sleep 2
done

cd "\$REMOTE_OBSERVABILITY_DIR/observability"
ABMS_LOG_DIR="\$LOG_DIR" docker compose up -d
for i in \$(seq 1 30); do
  TARGETS=\$(curl -sf http://127.0.0.1:9090/api/v1/targets || true)
  if echo "\$TARGETS" | grep -q '"job":"abms-api"' && echo "\$TARGETS" | grep -q '"health":"up"'; then
    break
  fi
  if [ "\$i" -eq 30 ]; then
    echo "Prometheus target check failed" >&2
    exit 1
  fi
  sleep 2
done
exit
EOF
