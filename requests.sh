#!/usr/bin/env bash

SERVER_URL="http://localhost:8080/api"
TOKEN="super-secret-token-123"

echo "============================================="
echo "======== 1. ПРОВЕРКА КЛЮЧЕЙ ДОСТУПА ========="
echo "============================================="
echo "Попытка запросить профили БЕЗ ключа (Ожидаем 401 Unauthorized):"
curl -i -X GET "$SERVER_URL/profiles"
echo -e "\n"

echo "Создание KIDS профиля с правильным токеном X-API-KEY (Ожидаем 201 Created):"
KIDS_PROFILE=$(curl -s -X POST "$SERVER_URL/profiles" -H "X-API-KEY: $TOKEN" -H "Content-Type: application/json" -d '{"name": "Alice", "type": "KIDS"}')
echo $KIDS_PROFILE
KIDS_ID=$(echo $KIDS_PROFILE | grep -o '"id":"[^"]*' | grep -o '[^"]*$')
echo "Профиль Алисы создан: ID = $KIDS_ID"

echo -e "\n=============================================="
echo "========== 2. СЦЕНАРИЙ КИНOТЕАТРА ============"
echo "=============================================="
MEDIA_LIST=$(curl -s -X GET "$SERVER_URL/media")
WITCHER_ID=$(echo $MEDIA_LIST | sed -E 's/.*"id":"([^"]*)","premium":[a-z]+,"title":"The Witcher".*/\1/')
echo "ID фильма Ведьмак (Рейтинг R, Премиум): $WITCHER_ID"

echo -e "\nШаг А: Алиса пытается посмотреть Ведьмака (Ожидаем отказ 400 из-за ограничений):"
curl -i -X POST "$SERVER_URL/watch" -H "Content-Type: application/json" -d "{\"profileId\": \"$KIDS_ID\", \"mediaId\": \"$WITCHER_ID\"}"
echo -e "\n"

echo "Шаг Б: Алиса совершает операцию: покупает Премиум статус за 299 рублей (Ожидаем 200 OK):"
curl -i -X POST "$SERVER_URL/upgrade" -H "Content-Type: application/json" -d "{\"profileId\": \"$KIDS_ID\"}"
echo -e "\n"

echo "Шаг В: Изменение данных профиля (PUT) — Обновляем имя Алисы после апгрейда (Ожидаем 200 OK):"
curl -i -X PUT "$SERVER_URL/profiles/$KIDS_ID" -H "X-API-KEY: $TOKEN" -H "Content-Type: application/json" -d "{\"name\": \"Alice Premium\", \"type\": \"ADULT\"}"
echo -e "\n"

echo "Шаг Г: Повторная попытка Алисы посмотреть Ведьмака (Теперь статус ADULT, ожидаем успех 200 OK):"
curl -i -X POST "$SERVER_URL/watch" -H "Content-Type: application/json" -d "{\"profileId\": \"$KIDS_ID\", \"mediaId\": \"$WITCHER_ID\"}"
echo -e "\n"

echo "Шаг Д: Проверка изменения состояния (viewsCount фильма должен стать 1):"
curl -s -X GET "$SERVER_URL/media/$WITCHER_ID"
echo -e "\n"

echo "============================================="
echo "=== 3. ДОПОЛНИТЕЛЬНАЯ ПРОВЕРКА ФИЛЬТРАЦИИ ==="
echo "============================================="
echo "Запрос только бесплатных фильмов (onlyFree=true):"
curl -s -X GET "$SERVER_URL/media?onlyFree=true"
echo -e "\n"

echo "Запрос фильмов с фильтрацией по возрасту (ageRating=18):"
curl -s -X GET "$SERVER_URL/media?ageRating=18"
echo -e "\n"

echo "============================================="
echo "====== 4. ТЕСТЫ НА УСТОЙЧИВОСТЬ И CRUD ======"
echo "============================================="
echo "Шаг Е: Отправка некорректного JSON для проверки устойчивости (Ожидаем 400 Bad Request):"
curl -i -X POST "$SERVER_URL/watch" -H "Content-Type: application/json" -d "{\"profileId\": \"$KIDS_ID\", \"mediaId\":"
echo -e "\n"

echo "Шаг Ж: Запрос с несуществующим ID профиля (Ожидаем отказ 404 Not Found):"
curl -i -X POST "$SERVER_URL/watch" -H "Content-Type: application/json" -d "{\"profileId\": \"invalid-id-999\", \"mediaId\": \"$WITCHER_ID\"}"
echo -e "\n"

echo "Шаг З: Удаление медиа-ресурса (DELETE) по ID (Ожидаем 200 OK):"
curl -i -X DELETE "$SERVER_URL/media/$WITCHER_ID" -H "X-API-KEY: $TOKEN"
echo -e "\n"

echo "Шаг И: Попытка запросить удаленный фильм (Ожидаем отказ 404 Not Found):"
curl -i -X GET "$SERVER_URL/media/$WITCHER_ID"
echo -e "\n"

echo "============================================="
echo "========= ТЕСТИРОВАНИЕ ЗАВЕРШЕНО ============"
echo "============================================="
