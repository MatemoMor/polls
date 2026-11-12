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
