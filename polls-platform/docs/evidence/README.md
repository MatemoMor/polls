# Доказательства работы

## Файлы, которые создаёт `scripts/collect-evidence.sh`

| Файл | Что внутри |
|---|---|
| `compose-up.txt` | вывод `docker compose up -d --build` |
| `compose-ps.txt` | `docker compose ps`: все 4 контейнера Up, сервисы `healthy` |
| `api-check.txt` | полный сценарий: создание опроса, 5 голосов (201), повтор (409), чужой вариант (422), результаты, закрытие, голос в закрытый опрос (422) |
| `persistence-check.txt` | результаты до и после `docker compose restart` и итоговая строка «результаты идентичны» |

Запуск: `./scripts/collect-evidence.sh`

## Скриншоты, которые нужно добавить вручную

1. `screenshot-compose-ps.png`: `docker compose ps` с состоянием `healthy`.
2. `screenshot-postman-run.png`: успешный прогон коллекции `postman/polls.postman_collection.json` (Collection Runner, все тесты зелёные).
3. `screenshot-results.png`: ответ `GET /api/results/{poll_id}` с процентами.
4. `screenshot-persistence.png`: результаты до и после `docker compose restart` с одинаковыми цифрами.
5. `screenshot-volume.png`: `docker volume ls` с томом `polls-platform_db_data`.
6. (по желанию) `screenshot-schemas.png`: `docker compose exec db psql -U postgres -d polls -c '\dn'` со схемами `poll` и `vote`.
