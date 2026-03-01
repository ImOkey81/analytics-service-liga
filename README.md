# univerliga-analytics-service

Production-grade MVP analytics microservice for aggregated reports and chart data.

## Stack
- Java 21
- Spring Boot 3.3
- Spring Web MVC, Validation
- Spring Security OAuth2 Resource Server (JWT)
- springdoc OpenAPI + Swagger UI
- Spring Data JPA + PostgreSQL
- Flyway migrations
- Actuator

## Run
```bash
docker compose up --build
```

Service URLs:
- Swagger: http://localhost:8080/swagger-ui/index.html
- OpenAPI docs: http://localhost:8080/v3/api-docs
- Health: http://localhost:8080/actuator/health

## Modes
- `PROVISIONING_MODE=mock` (default): in-memory feedback dataset and runtime aggregation.
- `PROVISIONING_MODE=readmodel`: read from read-model tables (`rm_*`) filled by RabbitMQ ingestion (`feedback.review.*`).

## Broker ingestion (RabbitMQ)
- Exchange: `univerliga.feedback.events`
- Queue: `univerliga.analytics.feedback.inbox`
- DLQ: `univerliga.analytics.feedback.dlq`
- Routing keys: `feedback.review.created`, `feedback.review.updated`, `feedback.review.deleted`
- Idempotency: table `ingestion_processed_events` (unique by `event_id`)
- Fact store: table `rm_feedback_events` (upsert by `feedback_id`)
- Projection: incremental reproject only for impacted `(departmentId, teamId)` scopes plus department-global KPI row.

### Event contract
```json
{
  "eventId": "8b7b568c-b57b-4e46-87fb-8134e50f06a7",
  "type": "FeedbackCreated",
  "occurredAt": "2026-03-01T09:15:00Z",
  "source": "feedback-service",
  "payload": {
    "feedbackId": "rev_1001",
    "authorPersonId": "p_11",
    "targetPersonId": "p_12",
    "targetName": "Olga K.",
    "departmentId": "d_1",
    "teamId": "t_2",
    "categoryId": "cat_communication",
    "categoryName": "Communication",
    "subcategoryId": "sub_communication_good",
    "subcategoryName": "Constructive communication",
    "rating": 5,
    "feedbackDate": "2026-03-01"
  }
}
```

## Network feature flag
- `ANALYTICS_FEATURE_NETWORK=false` by default.
- If `false`, `/api/v1/reports/network/interactions` returns `404 NOT_FOUND`.

## Get token (password grant)
Client:
- `client_id=univerliga-analytics`
- `client_secret=univerliga-analytics-secret`

Example (manager):
```bash
TOKEN=$(curl -s -X POST 'http://localhost:8081/realms/univerliga/protocol/openid-connect/token' \
  -H 'Content-Type: application/x-www-form-urlencoded' \
  -d 'grant_type=password' \
  -d 'client_id=univerliga-analytics' \
  -d 'client_secret=univerliga-analytics-secret' \
  -d 'username=manager' \
  -d 'password=manager' | jq -r .access_token)
```

Users:
- `admin/admin` -> `ROLE_ADMIN`
- `manager/manager` -> `ROLE_MANAGER`
- `hr/hr` -> `ROLE_HR`
- `employee/employee` -> `ROLE_EMPLOYEE`

## cURL examples
```bash
BASE='http://localhost:8080'
AUTH="Authorization: Bearer $TOKEN"
```

System:
```bash
curl -s "$BASE/api/v1/system/version" -H "$AUTH"
```

Summary:
```bash
curl -s "$BASE/api/v1/reports/summary?periodFrom=2026-01-01&periodTo=2026-03-31&departmentId=d_1&teamId=t_2" -H "$AUTH"
```

Positivity by person:
```bash
curl -s "$BASE/api/v1/reports/charts/positivity-by-person?periodFrom=2026-01-01&periodTo=2026-03-31&departmentId=d_1&teamId=t_2&limit=20&sort=total" -H "$AUTH"
```

Subcategory frequency:
```bash
curl -s "$BASE/api/v1/reports/charts/subcategory-frequency?periodFrom=2026-01-01&periodTo=2026-03-31&departmentId=d_1&teamId=t_2&limit=30&sort=total" -H "$AUTH"
```

Trend:
```bash
curl -s "$BASE/api/v1/reports/charts/trend?metric=responses&granularity=month&from=2026-01-01&to=2026-03-31&departmentId=d_1&teamId=t_2" -H "$AUTH"
```

Ratings by category:
```bash
curl -s "$BASE/api/v1/reports/charts/ratings-by-category?periodFrom=2026-01-01&periodTo=2026-03-31&departmentId=d_1&teamId=t_2" -H "$AUTH"
```

Dashboard:
```bash
curl -s "$BASE/api/v1/reports/dashboard?periodFrom=2026-01-01&periodTo=2026-03-31&departmentId=d_1&teamId=t_2" -H "$AUTH"
```

Insights:
```bash
curl -s "$BASE/api/v1/reports/insights/top-subcategories?periodFrom=2026-01-01&periodTo=2026-03-31&departmentId=d_1&teamId=t_2&limit=5" -H "$AUTH"
```

Export CSV:
```bash
curl -s "$BASE/api/v1/reports/export?format=csv&periodFrom=2026-01-01&periodTo=2026-03-31&departmentId=d_1&teamId=t_2&mode=aggregated" -H "$AUTH" -o analytics.csv
```

Export XLSX:
```bash
curl -s "$BASE/api/v1/reports/export?format=xlsx&periodFrom=2026-01-01&periodTo=2026-03-31&departmentId=d_1&teamId=t_2&mode=aggregated" -H "$AUTH" -o analytics.xlsx
```

Network interactions (requires `ANALYTICS_FEATURE_NETWORK=true`):
```bash
curl -s "$BASE/api/v1/reports/network/interactions?periodFrom=2026-01-01&periodTo=2026-03-31&teamId=t_2" -H "$AUTH"
```

## Security rules
- Public: `/actuator/**`, `/swagger-ui/**`, `/v3/api-docs/**`
- JWT required: `/api/v1/**`
- `/api/v1/reports/**`: only `ADMIN|MANAGER|HR`
- `EMPLOYEE` gets `403` on report endpoints.

## Request ID and error format
- Accepts `X-Request-Id`, generates UUID when absent.
- Returns `X-Request-Id` in response headers.
- Includes request id in success `meta.requestId` and error `error.requestId`.
# analytics-service-liga
