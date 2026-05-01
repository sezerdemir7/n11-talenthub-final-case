# Backend Deploy

## Server Setup

```bash
sudo apt update
sudo apt install -y docker.io docker-compose-plugin git
sudo usermod -aG docker $USER
```

Log out and log back in after adding the user to the Docker group.

PostgreSQL and RabbitMQ run as containers from `docker-compose.backend.yml`.
Their data is stored in Docker volumes.

## Clone And Configure

```bash
git clone <repo-url>
cd n11-final-case/backend
cp .env.example .env
nano .env
```

Replace every `change-me` value in `.env` with real production values.

If port `80` is already used on the server, set another value:

```properties
FRONTEND_PORT=8080
```

## Config Repo

The config repository should contain `*-prod.properties` files for each service.
Do not use `localhost` or `127.0.0.1` inside production config files for service
dependencies.

Database settings should come from environment variables:

```properties
spring.datasource.url=${DB_URL}
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}
```

Compose passes the internal Docker database URLs:

```text
user-service    -> jdbc:postgresql://postgres:5432/ecommerce_user_service
product-service -> jdbc:postgresql://postgres:5432/ecommerce_product_service
cart-service    -> jdbc:postgresql://postgres:5432/ecommerce_cart_service
order-service   -> jdbc:postgresql://postgres:5432/ecommerce_order_service
payment-service -> jdbc:postgresql://postgres:5432/ecommerce_payment_service
```

`deploy/postgres/init-db.sql` creates service databases on first PostgreSQL
startup.

RabbitMQ settings should also come from environment variables:

```properties
spring.rabbitmq.host=${RABBITMQ_HOST:rabbitmq}
spring.rabbitmq.port=${RABBITMQ_PORT:5672}
spring.rabbitmq.username=${RABBITMQ_USERNAME}
spring.rabbitmq.password=${RABBITMQ_PASSWORD}
```

## Start Full Stack

```bash
docker compose -f docker-compose.backend.yml --env-file .env up -d --build
```

## Logs And Health

```bash
docker compose -f docker-compose.backend.yml ps
docker compose -f docker-compose.backend.yml logs -f api-gateway
```

Eureka:

```text
http://SERVER_IP:8761
```

Gateway health:

```text
http://SERVER_IP:8763/actuator/health
```

Frontend:

```text
http://SERVER_IP
```

Grafana:

```text
http://SERVER_IP:3000
```

Default local Grafana login:

```text
admin / admin
```
