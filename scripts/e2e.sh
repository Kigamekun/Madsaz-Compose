#!/usr/bin/env bash
# End-to-end terhadap madsaz-BE sungguhan: Postgres + S3 sekali-pakai (Docker), API Node, lalu
#   1) data layer Android di JVM (app/src/test/.../e2e/LiveBackendE2eTest.kt), dan
#   2) bila E2E_DEVICE=1 dan ada perangkat adb: alur UI daftar -> masuk -> keluar di perangkat (androidTest).
# Butuh: Docker, Node, repo madsaz-BE (default ../madsaz-BE, ubah dengan MADSAZ_BE_DIR). Semua nilai hanya untuk uji lokal.
set -euo pipefail
cd "$(dirname "$0")/.."
ROOT="$(pwd)"
BE_DIR="$(cd "${MADSAZ_BE_DIR:-../madsaz-BE}" && pwd)"
PORT="${E2E_PORT:-3300}"
LOG="$(mktemp -t madsaz-e2e-be)"

compose=(docker compose -f "$BE_DIR/docker-compose.e2e.yml")
api_pid=""
cleanup() {
  [ -n "$api_pid" ] && kill "$api_pid" 2>/dev/null || true
  "${compose[@]}" down -v >/dev/null 2>&1 || true
}
trap cleanup EXIT

"${compose[@]}" up -d --wait

export NODE_ENV=test PORT="$PORT"
export DATABASE_URL=postgresql://madsaz:madsazpass@localhost:55432/madsaz
export JWT_SECRET="$(openssl rand -hex 32)"
export OBJECT_STORAGE_ENDPOINT=http://localhost:58333 OBJECT_STORAGE_PUBLIC_ENDPOINT=http://localhost:58333
export OBJECT_STORAGE_ACCESS_KEY_ID=e2e OBJECT_STORAGE_SECRET_ACCESS_KEY=e2e-secret
export ACCESS_TOKEN_TTL=3s MOCK_ANALYSIS_ENABLED=true ML_SERVICE_URL=

(cd "$BE_DIR" && npm run --silent migrate >/dev/null)
(cd "$BE_DIR" && exec node src/server.js >"$LOG" 2>&1) &
api_pid=$!
for _ in $(seq 1 30); do curl -fs "http://127.0.0.1:$PORT/health" >/dev/null 2>&1 && break; sleep 1; done
curl -fs "http://127.0.0.1:$PORT/health" >/dev/null || { echo "BE tidak sehat; log: $LOG"; exit 1; }

# Heap kecil: mesin pengembangan berRAM 8 GB mudah kehabisan memori (swap memakai disk) bila Docker dan Gradle berjalan bersama.
GRADLE_ARGS=(-Dorg.gradle.jvmargs=-Xmx1536m -Pkotlin.daemon.jvmargs=-Xmx1536m)

echo "== Data layer Android vs BE nyata"
MADSAZ_E2E_BASE_URL="http://127.0.0.1:$PORT/api/v1" "$ROOT/gradlew" "${GRADLE_ARGS[@]}" :app:testDebugUnitTest --rerun --tests 'id.madsaz.app.e2e.*'

if [ "${E2E_DEVICE:-0}" = "1" ]; then
  echo "== UI di perangkat (adb reverse ke BE di komputer ini)"
  adb get-state >/dev/null
  adb reverse "tcp:$PORT" "tcp:$PORT"
  adb reverse tcp:58333 tcp:58333
  "$ROOT/gradlew" "${GRADLE_ARGS[@]}" -PmadsazApiBaseUrl="http://localhost:$PORT/api/v1" :app:connectedDebugAndroidTest
fi
echo "E2E selesai"
