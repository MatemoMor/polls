#!/usr/bin/env bash
# Собирает доказательства работы системы в docs/evidence/*.txt
# Запуск из любой директории: ./scripts/collect-evidence.sh
set -uo pipefail

cd "$(dirname "$0")/.."

EV=docs/evidence
mkdir -p "$EV"

POLL=http://localhost:8001
VOTE=http://localhost:8002
RES=http://localhost:8003

wait_healthy() {
  for _ in $(seq 1 90); do
    if curl -fsS "$POLL/health" >/dev/null 2>&1 \
       && curl -fsS "$VOTE/health" >/dev/null 2>&1 \
       && curl -fsS "$RES/health" >/dev/null 2>&1; then
      return 0
    fi
    sleep 2
  done
  echo "Сервисы не стали healthy за 180 с" >&2
  return 1
}

# call METHOD URL [JSON_BODY] - печатает команду, тело ответа и HTTP-код
call() {
  local method=$1 url=$2 body=${3:-}
  if [[ -n "$body" ]]; then
    echo "\$ curl -X $method $url -d '$body'"
    curl -sS -X "$method" "$url" -H 'Content-Type: application/json' -d "$body" -w '\nHTTP %{http_code}\n'
  else
    echo "\$ curl -X $method $url"
    curl -sS -X "$method" "$url" -w '\nHTTP %{http_code}\n'
  fi
  echo
}

# ids_of "<json>" -> UUID-ы в порядке появления (первый - id опроса, дальше - id вариантов)
ids_of() {
  echo "$1" | grep -oE '"id":"[0-9a-f-]{36}"' | cut -d'"' -f4
}

# 1. Сборка и запуск
echo ">>> docker compose up -d --build"
docker compose up -d --build 2>&1 | tee "$EV/compose-up.txt"
wait_healthy || exit 1

# 2. Состояние контейнеров
echo ">>> docker compose ps"
docker compose ps 2>&1 | tee "$EV/compose-ps.txt"

# 3. Сценарий защиты
API="$EV/api-check.txt"
: > "$API"
echo ">>> API scenario (см. $API)"

out=$(call POST "$POLL/api/polls" \
  '{"title":"Любимый язык программирования","options":["Java","Python","Go"]}' | tee -a "$API")
mapfile -t IDS < <(ids_of "$out")
POLL_ID=${IDS[0]}; OPT_A=${IDS[1]}; OPT_B=${IDS[2]}; OPT_C=${IDS[3]}

out2=$(call POST "$POLL/api/polls" '{"title":"Другой опрос","options":["Да","Нет"]}' | tee -a "$API")
mapfile -t IDS2 < <(ids_of "$out2")
OTHER_OPT=${IDS2[1]}

vote() { # voter option
  call POST "$VOTE/api/votes" \
    "{\"poll_id\":\"$POLL_ID\",\"option_id\":\"$2\",\"voter_id\":\"$1\"}" | tee -a "$API" >/dev/null
}

echo "## 5 голосов от разных voter_id (ожидается 201)" | tee -a "$API"
vote voter-1 "$OPT_A"
vote voter-2 "$OPT_A"
vote voter-3 "$OPT_B"
vote voter-4 "$OPT_C"
vote voter-5 "$OPT_A"

echo "## Повторный голос voter-1 (ожидается 409)" | tee -a "$API"
vote voter-1 "$OPT_B"

echo "## Голос за вариант из другого опроса (ожидается 422)" | tee -a "$API"
vote voter-99 "$OTHER_OPT"

echo "## Результаты (ожидается Java 60.0 / Python 20.0 / Go 20.0)" | tee -a "$API"
call GET "$RES/api/results/$POLL_ID" | tee -a "$API" >/dev/null

echo "## Закрытие опроса" | tee -a "$API"
call POST "$POLL/api/polls/$POLL_ID/close" | tee -a "$API" >/dev/null

echo "## Голос в закрытый опрос (ожидается 422)" | tee -a "$API"
vote voter-100 "$OPT_A"

# 4. Сохранность данных после restart
PERSIST="$EV/persistence-check.txt"
: > "$PERSIST"

echo "== Результаты ДО docker compose restart ==" >> "$PERSIST"
before=$(call GET "$RES/api/results/$POLL_ID")
echo "$before" >> "$PERSIST"

echo ">>> docker compose restart"
{ echo "== docker compose restart =="; docker compose restart 2>&1; } >> "$PERSIST"
wait_healthy || exit 1

echo "== Результаты ПОСЛЕ docker compose restart ==" >> "$PERSIST"
after=$(call GET "$RES/api/results/$POLL_ID")
echo "$after" >> "$PERSIST"

if [[ "$before" == "$after" ]]; then
  echo "== ИТОГ: результаты идентичны, данные сохранились ==" | tee -a "$PERSIST"
else
  echo "== ИТОГ: РЕЗУЛЬТАТЫ ОТЛИЧАЮТСЯ ==" | tee -a "$PERSIST"
  exit 1
fi

echo "Готово. Файлы сохранены в $EV/"
