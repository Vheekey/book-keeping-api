#!/bin/sh
set -eu

mkdir -p "${RECEIPT_STORAGE_PATH:-/app/uploads/receipts}"

exec java ${JAVA_OPTS:-} -jar /app/app.jar
