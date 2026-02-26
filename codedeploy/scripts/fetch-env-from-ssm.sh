#!/usr/bin/env bash
set -euo pipefail

AWS_REGION="ap-northeast-2"
OUT_FILE="/home/ubuntu/deploy/chat/application-prod.yml"
PARAM_BASE="/commitme/v2/prod/chat-server"

umask 077

# SSM 값 조회 함수 (SecureString 복호화 포함)
get_ssm() {
  local name="$1"
  aws ssm get-parameter \
    --region "$AWS_REGION" \
    --name "$name" \
    --with-decryption \
    --query 'Parameter.Value' \
    --output text
}

# 1) SSM에서 "변하는 값"만 읽기
DOMAIN="$(get_ssm "${PARAM_BASE}/DOMAIN")"

DB_URL="$(get_ssm "${PARAM_BASE}/PROD_DB_NAME")"
DB_USER="$(get_ssm "${PARAM_BASE}/PROD_MYSQL_ID")"
DB_PASS="$(get_ssm "${PARAM_BASE}/PROD_MYSQL_PW")"

JWT_SECRET="$(get_ssm "${PARAM_BASE}/JWT_SECRET")"

S3_BUCKET_NAME="$(get_ssm "${PARAM_BASE}/S3_BUCKET_NAME")"
S3_ACCESS_KEY="$(get_ssm "${PARAM_BASE}/S3_ACCESS_KEY")"
S3_SECRET_KEY="$(get_ssm "${PARAM_BASE}/S3_SECRET_KEY")"

REDIS_IP="$(get_ssm "${PARAM_BASE}/REDIS_IP")"
REDIS_PW="$(get_ssm "${PARAM_BASE}/REDIS_PW")"

MONGO_URI="$(get_ssm "${PARAM_BASE}/MONGO_URI")"

# 2) application-prod.yml 생성 (prod에서 바뀌는 것만 override)
cat > "$OUT_FILE" <<YAML
server:
  port: 8080

spring:
  application:
    name: Chat
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: "${DB_URL}"
    username: "${DB_USER}"
    password: "${DB_PASS}"

  jpa:
    hibernate:
      ddl-auto: validate
    properties:
      hibernate:
        format_sql: false
    show-sql: false
    
  flyway:
    enabled: false
    locations: classpath:db/migration
    baseline-on-migrate: true
    baseline-version: "20260204.0"

  data:
    mongodb:
      uri: "${MONGO_URI}"
    redis:
      host: "${REDIS_IP}"
      port: 6379
      password: "${REDIS_PW}"
        
app:
  cors:
    allowed-origins: "${DOMAIN}"
    allowed-methods:
      - "GET"
      - "POST"
      - "PATCH"
      - "DELETE"
      - "OPTIONS"
    allowed-headers:
      - "*"
    exposed-headers:
      - "Set-Cookie"
    allow-credentials: true
    max-age: 600
  s3:
    bucket: "${S3_BUCKET_NAME}"
    region: "ap-northeast-2"
    access-key: "${S3_ACCESS_KEY}"
    secret-key: "${S3_SECRET_KEY}"
    cdn-base-url: "https://cdn.commit-me.com"
    presign-duration-minutes: 30
  loadtest:
    enabled: false

security:
  jwt:
    secret: "${JWT_SECRET}"
    access-expiration: 1h
    refresh-expiration: 7d

    
YAML

echo "[OK] wrote $OUT_FILE"