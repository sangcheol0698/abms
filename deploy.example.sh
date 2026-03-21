#!/bin/bash
set -e

KEY_FILE="${KEY_FILE:-~/.ssh/your-key.pem}"
SERVER_IP="${SERVER_IP:-YOUR_SERVER_IP}"

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

ssh -i "$KEY_FILE" ubuntu@"$SERVER_IP" <<EOF
pkill java
set -a; source .env; set +a;
nohup java -jar abms-api.jar --spring.profiles.active=dev > abms-api.log 2>&1 &
exit
EOF
