# Платформа для проведения опросов

Учебный проект на микросервисной архитектуре: три Spring Boot сервиса и один PostgreSQL.

## Описание и пользовательский сценарий

1. Автор создаёт опрос с 2–10 вариантами ответа (Poll Service).
2. Участники голосуют; один `voter_id` может проголосовать в опросе только один раз (Vote Service).
3. Любой пользователь запрашивает результаты: количество голосов и проценты по каждому варианту (Results Service).
4. Автор закрывает опрос (или он закрывается по `ends_at`), после чего новые голоса отклоняются.

## Технологии

- Java 21, Spring Boot 3.3.x, Maven
- Spring Web, Spring Data JPA (Hibernate), Bean Validation, Spring Boot Actuator
- Flyway (миграции), PostgreSQL 16
- Spring `RestClient` с таймаутами подключения и чтения 3 с
- JUnit 5, Mockito, MockMvc
- Docker, Docker Compose

## Сервисы

| Сервис | Порт | Ответственность |
|---|---|---|
| poll-service | 8001 | создание, просмотр и закрытие опросов; схема БД `poll` |
| vote-service | 8002 | приём голосов, защита от повторов через `UNIQUE (poll_id, voter_id)`; схема БД `vote` |
| results-service | 8003 | объединяет данные Poll и Vote Service, считает проценты; своей БД нет |
| db | — | PostgreSQL 16, том `db_data`, наружу порт не публикуется |

Сервисы общаются только по HTTP и не обращаются к таблицам друг друга. Poll и Vote работают под разными пользователями БД (`poll`, `vote`), у каждого права только на свою схему.

## Архитектура

Схема C4 (3 уровня: Context, Container, Component): [`docs/architecture.drawio`](docs/architecture.drawio), картинка: [`docs/architecture.png`](docs/architecture.png).

Как экспортировать PNG:
1. Откройте `docs/architecture.drawio` в [app.diagrams.net](https://app.diagrams.net) или в draw.io Desktop.
2. Выберите нужную страницу (`1. Context`, `2. Container`, `3. Component`).
3. `File → Export as → PNG…`, масштаб 100–200 %, галка *Border width*, нажмите *Export*.
4. Сохраните как `docs/architecture.png`. Для остальных уровней сохраните `architecture-context.png` и `architecture-component.png` рядом.

CLI-вариант (draw.io Desktop): `drawio -x -f png --all-pages -o docs docs/architecture.drawio`.

## Как запустить

Нужны Docker и Docker Compose.

```bash
docker compose up --build
```

Дождитесь, пока все контейнеры станут `healthy` (`docker compose ps`). Настройки по умолчанию подходят без `.env`; чтобы изменить их, скопируйте `.env.example` в `.env`.

Тесты (нужны JDK 21 и Maven):

```bash
cd poll-service && mvn test
cd ../vote-service && mvn test
cd ../results-service && mvn test
```

## Эндпоинты

Все ошибки возвращаются в формате `{"detail": "..."}`.

### Poll Service (8001)

| Метод | Путь | Описание | Коды |
|---|---|---|---|
| POST | `/api/polls` | создать опрос с вариантами (одна транзакция) | 201, 422 |
| GET | `/api/polls/{id}` | опрос с вариантами и `is_active` | 200, 404 |
| GET | `/api/polls` | список опросов | 200 |
| POST | `/api/polls/{id}/close` | закрыть опрос | 200, 404 |
| GET | `/health` | проверка состояния | 200 |

### Vote Service (8002)

| Метод | Путь | Описание | Коды |
|---|---|---|---|
| POST | `/api/votes` | отдать голос `{poll_id, option_id, voter_id}` | 201, 404, 409, 422, 503 |
| GET | `/api/votes/counts?poll_id=...` | `{poll_id, total, counts}` | 200 |
| GET | `/health` | проверка состояния | 200 |

### Results Service (8003)

| Метод | Путь | Описание | Коды |
|---|---|---|---|
| GET | `/api/results/{poll_id}` | результаты с процентами | 200, 404, 503 |
| GET | `/health` | проверка состояния | 200 |

## Примеры curl

```bash
# 1. Создать опрос
curl -s -X POST localhost:8001/api/polls -H 'Content-Type: application/json' \
  -d '{"title":"Любимый язык","options":["Java","Python","Go"]}'

# 2. Проголосовать (подставьте id из ответа выше)
curl -s -X POST localhost:8002/api/votes -H 'Content-Type: application/json' \
  -d '{"poll_id":"<POLL_ID>","option_id":"<OPTION_ID>","voter_id":"user-1"}'

# 3. Тот же voter_id ещё раз -> 409
# 4. Результаты
curl -s localhost:8003/api/results/<POLL_ID>

# 5. Закрыть опрос
curl -s -X POST localhost:8001/api/polls/<POLL_ID>/close
```

Пример ответа результатов:

```json
{"poll_id":"…","title":"Любимый язык","is_active":true,"total_votes":5,
 "results":[{"option_id":"…","text":"Java","count":3,"percent":60.0},
            {"option_id":"…","text":"Python","count":1,"percent":20.0},
            {"option_id":"…","text":"Go","count":1,"percent":20.0}]}
```

## Проверка

### Пошаговый сценарий

Импортируйте `postman/polls.postman_collection.json` и запустите Collection Runner (или выполните `./scripts/collect-evidence.sh`, он делает то же самое curl-ом):

1. Создать опрос с 3 вариантами → 201.
2. Отправить 5 голосов от `voter-1…voter-5` → 201.
3. Повторный голос `voter-1` → **409**.
4. Голос за вариант из другого опроса → **422**.
5. Получить результаты: Java 60.0 %, Python 20.0 %, Go 20.0 %.
6. Закрыть опрос и проголосовать ещё раз → **422**.

### Сохранность данных

```bash
curl -s localhost:8003/api/results/<POLL_ID>   # запомнить цифры
docker compose restart
curl -s localhost:8003/api/results/<POLL_ID>   # цифры те же
```

Данные также переживают `docker compose down` и последующий `docker compose up` (без `-v`), потому что лежат в именованном томе `db_data`.

### Доказательства

Скрипт `scripts/collect-evidence.sh` сохраняет вывод в [`docs/evidence`](docs/evidence/README.md):
[`compose-up.txt`](docs/evidence/compose-up.txt), [`compose-ps.txt`](docs/evidence/compose-ps.txt),
[`api-check.txt`](docs/evidence/api-check.txt), [`persistence-check.txt`](docs/evidence/persistence-check.txt).

## ⚠️ Предупреждение

`docker compose down -v` **удаляет том `db_data`, то есть все опросы и голоса**. После этого `db/init.sql` выполнится заново на пустой базе. Для обычной остановки используйте `docker compose down` без `-v`.

Скрипт `db/init.sql` выполняется только при первой инициализации тома. Если меняете пароли ролей, пересоздайте том и обновите значения в `.env`.
