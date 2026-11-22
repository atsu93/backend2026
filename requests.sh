#!/usr/bin/env bash

SERVER_URL="http://localhost:8080/api"
TOKEN="super-secret-token-123"

echo "============================================="
echo "===== 1. АВТОРИЗАЦИЯ И СОЗДАНИЕ ПРОФИЛЯ ====="
echo "============================================="
echo "1.1. Запрос профилей БЕЗ ключа (Ожидаем 401 Unauthorized):"
curl -i -s -X GET "$SERVER_URL/profiles"
echo -e "\n"

echo "1.2. Создание профиля с пустым именем из пробелов (Ожидаем 400 Bad Request):"
curl -i -s -X POST "$SERVER_URL/profiles" \
  -H "X-API-KEY: $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"name": "   ", "age": 20}'
echo -e "\n"

echo "1.3. Создание обычного (REGULAR) профиля Алисы 25 лет (Ожидаем 201 Created, type=REGULAR, balance=0.0):"
ALICE_PROFILE=$(curl -s -X POST "$SERVER_URL/profiles" \
  -H "X-API-KEY: $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"name": "Alice", "age": 25}')
echo "$ALICE_PROFILE"
ALICE_ID=$(echo "$ALICE_PROFILE" | grep -o '"id":"[^"]*' | grep -o '[^"]*$')
echo "Профиль Алисы создан: ID = $ALICE_ID"

echo -e "\n=============================================="
echo "== 2. БИЗНЕС-СЦЕНАРИЙ: ПОДПИСКА И ПРОСМОТР ==="
echo "=============================================="
MEDIA_LIST=$(curl -s -X GET "$SERVER_URL/media")
WITCHER_ID=$(echo "$MEDIA_LIST" | sed -E 's/.*"id":"([^"]*)","premium":[a-z]+,"title":"The Witcher".*/\1/')
echo "ID фильма Ведьмак (Рейтинг R, Премиум): $WITCHER_ID"

echo -e "\n2.1. Алиса (REGULAR) пытается посмотреть Ведьмака (Ожидаем 403 Forbidden):"
curl -i -s -X POST "$SERVER_URL/watch" \
  -H "X-API-KEY: $TOKEN" \
  -H "Content-Type: application/json" \
  -d "{\"profileId\": \"$ALICE_ID\", \"mediaId\": \"$WITCHER_ID\"}"
echo -e "\n"

echo "2.2. Попытка купить PREMIUM при балансе 0.0 (Ожидаем 400 Insufficient funds):"
curl -i -s -X POST "$SERVER_URL/upgrade" \
  -H "X-API-KEY: $TOKEN" \
  -H "Content-Type: application/json" \
  -d "{\"profileId\": \"$ALICE_ID\"}"
echo -e "\n"

echo "2.3. Попытка пополнить баланс отрицательной суммой -100 (Ожидаем 400 Bad Request):"
curl -i -s -X POST "$SERVER_URL/topup" \
  -H "X-API-KEY: $TOKEN" \
  -H "Content-Type: application/json" \
  -d "{\"profileId\": \"$ALICE_ID\", \"amount\": -100}"
echo -e "\n"

echo "2.4. Пополнение баланса Алисы на 500.0 (Ожидаем 200 OK):"
curl -i -s -X POST "$SERVER_URL/topup" \
  -H "X-API-KEY: $TOKEN" \
  -H "Content-Type: application/json" \
  -d "{\"profileId\": \"$ALICE_ID\", \"amount\": 500.0}"
echo -e "\n"

echo "2.5. Покупка статуса PREMIUM за 299.0 (Ожидаем 200 OK):"
curl -i -s -X POST "$SERVER_URL/upgrade" \
  -H "X-API-KEY: $TOKEN" \
  -H "Content-Type: application/json" \
  -d "{\"profileId\": \"$ALICE_ID\"}"
echo -e "\n"

echo "2.6. Повторная покупка PREMIUM при уже активной подписке (Ожидаем 409 Conflict):"
curl -i -s -X POST "$SERVER_URL/upgrade" \
  -H "X-API-KEY: $TOKEN" \
  -H "Content-Type: application/json" \
  -d "{\"profileId\": \"$ALICE_ID\"}"
echo -e "\n"

echo "2.7. Обновление имени профиля Алисы через PUT (Ожидаем 200 OK, остаток баланса 201.0, type=PREMIUM):"
curl -i -s -X PUT "$SERVER_URL/profiles/$ALICE_ID" \
  -H "X-API-KEY: $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"name": "Alice Premium"}'
echo -e "\n"

echo "2.8. Повторная попытка Алисы посмотреть Ведьмака с подпиской PREMIUM (Ожидаем 200 OK):"
curl -i -s -X POST "$SERVER_URL/watch" \
  -H "X-API-KEY: $TOKEN" \
  -H "Content-Type: application/json" \
  -d "{\"profileId\": \"$ALICE_ID\", \"mediaId\": \"$WITCHER_ID\"}"
echo -e "\n"

echo "2.9. Проверка счетчика просмотров и истории (viewsCount должен стать 1):"
curl -s -X GET "$SERVER_URL/media/$WITCHER_ID"
echo -e "\n"

echo "2.10. Создание профиля несовершеннолетнего Боба (15 лет) (Ожидаем 201 Created):"
BOB_PROFILE=$(curl -s -X POST "$SERVER_URL/profiles" \
  -H "X-API-KEY: $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"name": "Bob", "age": 15}')
echo "$BOB_PROFILE"
BOB_ID=$(echo "$BOB_PROFILE" | grep -o '"id":"[^"]*' | grep -o '[^"]*$')
echo -e "Профиль Боба создан: ID = $BOB_ID\n"

echo "2.11. Пополнение баланса Боба и покупка PREMIUM за 299.0 (Ожидаем 200 OK):"
curl -s -X POST "$SERVER_URL/topup" \
  -H "X-API-KEY: $TOKEN" \
  -H "Content-Type: application/json" \
  -d "{\"profileId\": \"$BOB_ID\", \"amount\": 300.0}" > /dev/null
curl -i -s -X POST "$SERVER_URL/upgrade" \
  -H "X-API-KEY: $TOKEN" \
  -H "Content-Type: application/json" \
  -d "{\"profileId\": \"$BOB_ID\"}"
echo -e "\n"

echo "2.12. Боб (15 лет, PREMIUM) пытается посмотреть Ведьмака 18+/R (Ожидаем 403 Forbidden):"
curl -i -s -X POST "$SERVER_URL/watch" \
  -H "X-API-KEY: $TOKEN" \
  -H "Content-Type: application/json" \
  -d "{\"profileId\": \"$BOB_ID\", \"mediaId\": \"$WITCHER_ID\"}"
echo -e "\n"

echo "2.13. Удаление тестового профиля Боба DELETE (Ожидаем 204 No Content):"
curl -i -s -X DELETE "$SERVER_URL/profiles/$BOB_ID" \
  -H "X-API-KEY: $TOKEN"
echo -e "\n"

echo "2.14. Проверка защиты от переполнения double при пополнении баланса Алисы (Ожидаем 400 Bad Request):"
curl -i -s -X POST "$SERVER_URL/topup" \
  -H "X-API-KEY: $TOKEN" \
  -H "Content-Type: application/json" \
  -d "{\"profileId\": \"$ALICE_ID\", \"amount\": 1e308}"
echo -e "\n"

echo "============================================="
echo "=== 3. МЕДИАТЕКА, ЧАСТИЧНЫЙ PUT, ФИЛЬТРЫ ===="
echo "============================================="
echo "3.1. Попытка создать фильм с некорректным рейтингом '99+' (Ожидаем 400 Bad Request):"
curl -i -s -X POST "$SERVER_URL/media" \
  -H "X-API-KEY: $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"title": "Broken Movie", "ageRating": "99+", "isPremium": true}'
echo -e "\n"

echo "3.2. Атомарность PUT (валидный title + невалидный ageRating='99+', ожидаем 400 и сохранение старого названия):"
curl -i -s -X PUT "$SERVER_URL/media/$WITCHER_ID" \
  -H "X-API-KEY: $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"title": "Should Not Be Saved", "ageRating": "99+"}'
echo -e "\n"

echo "3.3. Частичное обновление Ведьмака (меняем только title, проверяем что premium остался true, 200 OK):"
curl -i -s -X PUT "$SERVER_URL/media/$WITCHER_ID" \
  -H "X-API-KEY: $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"title": "The Witcher: Extended Edition"}'
echo -e "\n"

echo "3.4. Запрос только бесплатных фильмов (onlyFree=true, Ожидаем 200 OK):"
curl -s -X GET "$SERVER_URL/media?onlyFree=true"
echo -e "\n"

echo "3.5. Запрос фильмов по возрастному рейтингу (ageRating=R, Ожидаем 200 OK):"
curl -s -X GET "$SERVER_URL/media?ageRating=R"
echo -e "\n"

echo "3.6. Невалидный булев параметр фильтрации (onlyFree=maybe, Ожидаем 400 Bad Request):"
curl -i -s -X GET "$SERVER_URL/media?onlyFree=maybe"
echo -e "\n"

echo "============================================="
echo "== 4. HTTP-МЕТОДЫ, ОШИБКИ И УДАЛЕНИЕ CRUD ==="
echo "============================================="
echo "4.1. Неподдерживаемый HTTP-метод PATCH (Ожидаем 405 Method Not Allowed + заголовок Allow):"
curl -i -s -X PATCH "$SERVER_URL/profiles/$ALICE_ID" \
  -H "X-API-KEY: $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"name": "Hacked"}'
echo -e "\n"

echo "4.2. Пустое тело POST-запроса (Ожидаем 400 Bad Request):"
curl -i -s -X POST "$SERVER_URL/watch" \
  -H "X-API-KEY: $TOKEN" \
  -H "Content-Type: application/json"
echo -e "\n"

echo "4.3. Битый JSON в теле запроса (Ожидаем 400 Bad Request):"
curl -i -s -X POST "$SERVER_URL/watch" \
  -H "X-API-KEY: $TOKEN" \
  -H "Content-Type: application/json" \
  -d "{\"profileId\": \"$ALICE_ID\", \"mediaId\":"
echo -e "\n"

echo "4.4. Запрос с несуществующим ID профиля (Ожидаем 404 Not Found):"
curl -i -s -X POST "$SERVER_URL/watch" \
  -H "X-API-KEY: $TOKEN" \
  -H "Content-Type: application/json" \
  -d "{\"profileId\": \"invalid-id-999\", \"mediaId\": \"$WITCHER_ID\"}"
echo -e "\n"

echo "4.5. Слишком большое тело запроса > 64 КБ (Ожидаем 413 Payload Too Large):"
BIG_NAME=$(printf 'A%.0s' {1..70000})
curl -i -s -X POST "$SERVER_URL/profiles" \
  -H "X-API-KEY: $TOKEN" \
  -H "Content-Type: application/json" \
  -d "{\"name\": \"$BIG_NAME\", \"age\": 20}"
echo -e "\n"

echo "4.6. Удаление медиа-ресурса DELETE (Ожидаем 204 No Content):"
curl -i -s -X DELETE "$SERVER_URL/media/$WITCHER_ID" \
  -H "X-API-KEY: $TOKEN"
echo -e "\n"

echo "4.7. Попытка запросить удаленный фильм (Ожидаем 404 Not Found):"
curl -i -s -X GET "$SERVER_URL/media/$WITCHER_ID"
echo -e "\n"

echo "4.8. Удаление профиля Алисы DELETE (Ожидаем 204 No Content):"
curl -i -s -X DELETE "$SERVER_URL/profiles/$ALICE_ID" \
  -H "X-API-KEY: $TOKEN"
echo -e "\n"

echo "============================================="
echo "========= ТЕСТИРОВАНИЕ ЗАВЕРШЕНО ============"
echo "============================================="
