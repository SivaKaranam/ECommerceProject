# ECommerceProject

Backend capstone project for the Scaler Neovarsity / Woolf Master's in Computer Science (Backend Specialization). A microservices e-commerce backend covering authentication and authorization, search, payments, messaging and background jobs, deployment, the data layer, testing, and observability.

## Architecture

Seven Spring Boot 3 / Java 17 services in one Maven multi-module reactor, registered with Eureka and routed through a single API gateway.

```
Client -> API Gateway (Spring Cloud Gateway) -> Eureka-registered services

  user-service          registration/login, JWT issuance and decoding, cookie-based
  :8081                 refresh tokens with rotation, CSRF protection on the refresh
                         endpoint, and a separate OAuth2 authorization server
                         (client_credentials grant) for machine-to-machine callers

  product-service        product/category catalog, paginated and sortable listing,
  :8082                  Elasticsearch-backed full-text search

  order-service          shopping cart, checkout, calls product-service and
  :8083                  payment-service directly (Feign), publishes/consumes
                         order and payment events over Kafka

  payment-service        Razorpay payment link creation, signature-verified
  :8084                  webhook handling, scheduled reconciliation for
                         payments whose webhook never arrived

  notification-service   Kafka consumer, sends order confirmation and payment
  :8085                  receipt emails, dead-letter topic for messages that
                         keep failing

  api-gateway            routes /api/** to the right service by name via Eureka
  :8080

  service-registry        Eureka server the other six services register with
  :8761
```

`common` is an eighth module, a small shared library (JWT resource-server config, exception handling, a request-id logging filter) that user/product/order/payment-service depend on. It isn't a runnable service.

Each business service owns its own MySQL database (`userdb`, `productdb`, `orderdb`, `paymentdb`) with its own Flyway migrations, so a bug in one service's queries can't touch another service's data even though they can share a single MySQL instance in development.

## Tech stack

- Java 17, Spring Boot 3.4, Maven multi-module reactor
- Spring Cloud: Eureka (service discovery), Spring Cloud Gateway, OpenFeign
- Spring Security: custom JWT auth (shared HMAC secret across services) plus Spring Authorization Server for a genuine OAuth2 flow
- MySQL + Flyway migrations, one schema per service
- Elasticsearch (product search)
- Kafka (order-created, payment-completed events, dead-letter topic in notification-service)
- Razorpay (payment gateway)
- Docker + docker-compose for local orchestration, one Dockerfile per service
- GitHub Actions CI (builds and tests the full reactor, builds all seven images)

## Running locally

Optional but recommended: create a `.env` file in the repo root with Razorpay test-mode credentials, so checkout actually reaches Razorpay's real API instead of failing on the placeholder keys:

```
RAZORPAY_KEY_ID=rzp_test_xxxxxxxxxxxx
RAZORPAY_KEY_SECRET=xxxxxxxxxxxxxxxxxxxx
```

Free test-mode keys: razorpay.com > Settings > API Keys. Without this, everything else works normally, and only payment link creation will fail with an authentication error from Razorpay's API, which is expected, not a bug.

```bash
docker compose up --build
```

This brings up MySQL, Elasticsearch, Kafka/Zookeeper, MailHog (catches outgoing emails, UI at `http://localhost:8025`), and all seven services. Everything goes through the gateway at `http://localhost:8080`. Eureka's dashboard is at `http://localhost:8761`. Each service also exposes `/actuator/health` and `/actuator/prometheus` directly on its own port.

This was verified end to end with real Razorpay test credentials: register, login, add to cart, checkout, a real Razorpay payment link created, the order-created Kafka event consumed, and a real confirmation email received in MailHog.

## Running tests

```bash
./mvnw test
```

Runs the full reactor: unit tests (Mockito), Spring MVC slice tests, repository slice tests against H2, and context-load tests, across all eight modules.

## Folders

- `common/`, `service-registry/`, `api-gateway/`, `user-service/`, `product-service/`, `order-service/`, `payment-service/`, `notification-service/`: one Maven module each
- `docs/`: the original PRD notes
- `scripts/`: placeholder for local setup helpers
- `deployments/`: placeholder for deployment-related assets
- `.github/workflows/`: CI pipeline
- `Scaler_Neovarsity_Academy_Project_Report.docx`: the project report
