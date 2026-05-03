# N11 Final Case - Backend Mikroservis Mimarisi

Bu proje, N11 benzeri bir pazaryeri sisteminin backend tarafını mikroservis mimarisiyle modelleyen bir e-ticaret uygulamasıdır. Ana odak; Spring Boot servisleri, API Gateway, servis keşfi, merkezi konfigürasyon, JWT güvenliği, RabbitMQ tabanlı event iletişimi, PostgreSQL veri kalıcılığı, saga akışları, test altyapısı ve Docker Compose ile çalıştırılabilir backend ortamıdır.

Frontend bu projede yalnızca backend API'lerini tüketen istemci katmanı olarak konumlanır. README'nin ana kapsamı backend servisleri ve servisler arası akıştır.

## İçindekiler

- [Kullanılan Teknolojiler](#kullanılan-teknolojiler)
- [Canlı Ortam](#canlı-ortam)
- [Genel Mimari](#genel-mimari)
- [Backend Servisleri](#backend-servisleri)
- [Servisler Arası İletişim](#servisler-arası-iletişim)
- [Gateway ve Güvenlik](#gateway-ve-güvenlik)
- [Domain Akışları](#domain-akışları)
- [RabbitMQ Event Yapısı](#rabbitmq-event-yapısı)
- [Veritabanı Yapısı](#veritabanı-yapısı)
- [API Özeti](#api-özeti)
- [Çalıştırma](#çalıştırma)
- [Testler](#testler)
- [Gözlemlenebilirlik](#gözlemlenebilirlik)
- [CI/CD ve Dağıtım](#cicd-ve-dağıtım)
- [Proje Yapısı](#proje-yapısı)
- [Güvenlik Notları](#güvenlik-notları)

## Kullanılan Teknolojiler

Backend tarafında kullanılan ana teknolojiler aşağıdaki gibidir:

| Alan | Teknoloji |
| --- | --- |
| Dil | Java 21 |
| Framework | Spring Boot 3.5.x |
| Build | Maven |
| Mikroservis altyapısı | Spring Cloud, Eureka, Config Server |
| Gateway | Spring Cloud Gateway |
| Güvenlik | Spring Security, JWT, JJWT |
| Senkron iletişim | REST, OpenFeign |
| Asenkron iletişim | RabbitMQ, Spring AMQP |
| Veri erişimi | Spring Data JPA, Hibernate |
| Veritabanı | PostgreSQL |
| Test veritabanı | H2 |
| Test | JUnit 5, Mockito, Spring Boot Test |
| Dokümantasyon | Springdoc OpenAPI |
| Logging | Logback JSON encoder, correlation id, access log |
| Monitoring | Actuator, Micrometer, Loki, Promtail, Grafana |
| Dış servis | AWS S3, Iyzico |
| Container | Docker, Docker Compose |
| CI/CD | Jenkins |

## Canlı Ortam

Proje canlı ortamda aşağıdaki adresten erişilebilir:

| Uygulama | Link |
| --- | --- |
| Canlı Uygulama | [N11 Final Case](http://34.107.45.87/) |

## Genel Mimari

Backend, domain bazlı ayrılmış Spring Boot servislerinden oluşur. Dış dünyaya açılan ana giriş noktası `api-gateway` servisidir. Gateway, gelen isteklerde JWT kontrolünü yapar, kullanıcı kimliğini downstream servislere header olarak taşır ve servis keşfi üzerinden ilgili servise yönlendirir.

Senkron ihtiyaçlarda REST/Feign kullanılır. Sipariş, stok, ödeme, sepet temizleme, satıcı aktif/pasif durumu ve bildirim gibi asenkron süreçlerde RabbitMQ eventleri kullanılır.

![N11 backend mikroservis mimari diyagramı](../docs/architecture-overview.svg)

## Backend Servisleri

| Servis | Port | Ana Sorumluluk |
| --- | ---: | --- |
| `api-gateway` | `8763` | Tek giriş noktası, JWT doğrulama, route yönetimi, CORS, correlation id, access log |
| `discovery-server` | `8761` | Eureka tabanlı servis keşfi |
| `config-server` | `8762` | Merkezi konfigürasyon yönetimi |
| `user-service` | `8764` | Kullanıcı, auth, refresh token, adres, satıcı profili, satıcı onay/suspend süreçleri |
| `product-service` | `8767` | Ürün, kategori, stok yönetimi, ürün görseli, satıcı durumuna göre ürün aktivasyonu |
| `cart-service` | `8768` | Kullanıcı sepeti ve sepet kalemleri |
| `order-service` | `8769` | Checkout, sipariş yönetimi, sipariş iptali, saga koordinasyonu |
| `payment-service` | `8770` | Ödeme alma, ödeme sonucu eventleri, Iyzico entegrasyonu |
| `notification-service` | `8771` | Bildirim kayıtları, okunma durumu, WebSocket ile anlık bildirim |
| `common-lib` | - | Ortak DTO, exception, base entity, event, RabbitMQ constant, logging ve security helper sınıfları |

### Servis Sorumluluk Detayları

`api-gateway`

- Public ve protected endpoint ayrımı yapar.
- JWT token'ı doğrular.
- Token içinden `userId` ve role bilgilerini çıkarır.
- Downstream servislere `X-User-Id` ve `X-User-Roles` headerlarını ekler.
- Correlation id üretir veya mevcut correlation id'yi taşır.
- Access log üretir.

`user-service`

- Kullanıcı kayıt/giriş işlemlerini yönetir.
- Access token ve refresh token üretir.
- Refresh token kayıt, doğrulama ve logout akışlarını yönetir.
- Kullanıcı profil ve adres işlemlerini yürütür.
- Satıcı başvurusu, satıcı profili, admin onayı ve satıcı suspend süreçlerini yönetir.
- Satıcı durumu değiştiğinde RabbitMQ üzerinden `SellerActivatedEvent` veya `SellerSuspendedEvent` yayınlar.

`product-service`

- Ürün ve kategori CRUD işlemlerini yürütür.
- Ürün arama, filtreleme ve slug ile erişim sağlar.
- Checkout sırasında stok rezervasyonu yapar.
- Ödeme başarısızlığı veya sipariş iptali gibi durumlarda stok telafisi yapar.
- Satıcı suspend olduğunda ilgili satıcının ürünlerini pasife alır.
- Ürün görselleri için S3 entegrasyonuna sahiptir.

`cart-service`

- Kullanıcının aktif sepetini yönetir.
- Ürün ekleme, ürün miktarı güncelleme ve sepet temizleme işlemlerini yapar.
- Sipariş başarıyla tamamlandığında `CartClearRequestedEvent` tüketerek sepeti temizler.

`order-service`

- Checkout sürecini başlatır.
- Kullanıcının sepet, ürün ve adres bilgilerini doğrular.
- Siparişi oluşturur ve `OrderCreatedEvent` yayınlar.
- Stok ve ödeme eventlerine göre sipariş durumunu günceller.
- Sipariş iptali ve zaman aşımı gibi süreçlerde telafi eventleri yayınlar.

`payment-service`

- Ödeme isteklerini işler.
- Iyzico sandbox/production endpointleri üzerinden ödeme alır.
- Ödeme başarılıysa `PaymentSucceededEvent`, başarısızsa `PaymentFailedEvent` yayınlar.
- Sipariş iptal eventlerini dinleyerek ödeme tarafındaki telafi sürecini çalıştırır.

`notification-service`

- Sipariş ve iptal eventlerini dinleyerek kullanıcı bildirimi oluşturur.
- Okunmamış bildirimleri ve bildirim geçmişini sunar.
- WebSocket üzerinden anlık bildirim gönderir.

## Servisler Arası İletişim

Sistemde iki iletişim modeli vardır:

| Model | Kullanım Alanı | Örnek |
| --- | --- | --- |
| Senkron REST/Feign | Anlık doğrulama veya veri okuma gereken durumlar | Checkout sırasında sepet/ürün/kullanıcı bilgisi alma |
| Asenkron RabbitMQ event | Süreç devam edebilir, telafi gerekebilir veya servisler gevşek bağlı kalmalıdır | OrderCreated, StockReserved, PaymentSucceeded, SellerSuspended |

![Backend servisler arası iletişim diyagramı](../docs/backend-communication-flow.svg)

## Gateway ve Güvenlik

`api-gateway`, dış istemciden gelen tüm backend istekleri için güvenlik sınırıdır.

JWT doğrulama davranışı:

1. İstek public path listesinde mi kontrol edilir.
2. Public değilse `Authorization: Bearer ...` header'ı aranır.
3. Token imzası ve claim bilgileri doğrulanır.
4. `userId` claim'i zorunlu kabul edilir.
5. Role bilgileri normalize edilerek downstream servise taşınır.
6. Servislere şu headerlar eklenir:
    - `X-User-Id`
    - `X-User-Roles`

Public path örnekleri:

- `/api/v1/auth/**`
- `/api/v1/products`
- `/api/v1/products/search/**`
- `/api/v1/categories/filters`
- `/swagger-ui/**`
- `/v3/api-docs/**`
- `/actuator/health`
- `/actuator/info`
- `/ws/**`

![API Gateway JWT güvenlik akışı](../docs/gateway-security-flow.svg)

## Domain Akışları

### Checkout Saga Akışı

Checkout akışı tek servis transaction'ı değildir. Sipariş, stok, ödeme ve sepet temizleme süreçleri RabbitMQ eventleri ile ilerler.

![Checkout saga akışı](../docs/checkout-saga-flow.svg)

### Ödeme Başarısızlığı

Ödeme başarısızlığı, sipariş iptali ve satıcı durum değişikliği gibi telafi akışları aşağıdaki backend event diyagramında birlikte gösterilir.

### Sipariş İptali

Sipariş iptalinde `OrderCancelledEvent` yayınlanır. Product service stok telafisini, payment service ödeme iptal/iade tarafını, notification service bildirim tarafını yönetir.

### Satıcı Onay ve Suspend Akışı

Satıcı onay ve suspend kararları `user-service` tarafından verilir. Bu kararlar RabbitMQ üzerinden `product-service` tarafına aktarılır.

![Telafi ve satıcı durum event akışları](../docs/domain-event-flows.svg)

## RabbitMQ Event Yapısı

Ortak event ve routing key tanımları `common-lib` altında tutulur. Böylece servisler aynı event contract'ını kullanır.

Ana exchange:

| Değer | Açıklama |
| --- | --- |
| `ecommerce.saga.exchange` | Saga ve domain eventlerinin yayınlandığı ana exchange |

Öne çıkan routing keyler:

| Routing Key | Üreten Servis | Tüketen Servisler | Amaç |
| --- | --- | --- | --- |
| `order.created` | `order-service` | `product-service`, `notification-service` | Sipariş oluşturuldu |
| `stock.reserved` | `product-service` | `payment-service` | Stok başarıyla rezerve edildi |
| `stock.reservation.failed` | `product-service` | `order-service` | Stok rezervasyonu başarısız |
| `payment.succeeded` | `payment-service` | `order-service` | Ödeme başarılı |
| `payment.failed` | `payment-service` | `order-service`, `product-service` | Ödeme başarısız ve stok telafisi gerekir |
| `cart.clear.requested` | `order-service` | `cart-service` | Başarılı sipariş sonrası sepet temizleme |
| `seller.activated` | `user-service` | `product-service` | Satıcı onaylandı |
| `seller.suspended` | `user-service` | `product-service` | Satıcı askıya alındı |
| `order.cancelled` | `order-service` | `product-service`, `payment-service`, `notification-service` | Sipariş iptal edildi |
| `order.expired` | `order-service` | `product-service` | Sipariş zaman aşımı |

Öne çıkan event sınıfları:

- `OrderCreatedEvent`
- `OrderConfirmedEvent`
- `OrderCancelledEvent`
- `OrderExpiredEvent`
- `StockReservedEvent`
- `StockReservationFailedEvent`
- `PaymentSucceededEvent`
- `PaymentFailedEvent`
- `CartClearRequestedEvent`
- `SellerActivatedEvent`
- `SellerSuspendedEvent`

## Veritabanı Yapısı

Docker Compose tek PostgreSQL instance'ı başlatır. `postgres-init` servisi her mikroservis için ayrı database oluşturur.

| Database | Servis |
| --- | --- |
| `ecommerce_user_service` | `user-service` |
| `ecommerce_product_service` | `product-service` |
| `ecommerce_cart_service` | `cart-service` |
| `ecommerce_order_service` | `order-service` |
| `ecommerce_payment_service` | `payment-service` |
| `ecommerce_notification_service` | `notification-service` |

Bu yaklaşım her servisin kendi verisine sahip olması prensibini korur. Servisler başka bir servisin tablosuna doğrudan erişmek yerine API veya event üzerinden iletişim kurar.

## API Özeti

Tüm backend istekleri API Gateway üzerinden `/api/v1/...` yapısıyla servis edilir.

### Auth ve Kullanıcı

| Metot | Endpoint | Açıklama |
| --- | --- | --- |
| `POST` | `/api/v1/auth/register` | Müşteri kaydı |
| `POST` | `/api/v1/auth/register/seller` | Satıcı başvurulu kayıt |
| `POST` | `/api/v1/auth/login` | Kullanıcı girişi |
| `POST` | `/api/v1/auth/refresh-token` | Access token yenileme |
| `POST` | `/api/v1/auth/logout` | Refresh token iptali |
| `GET` | `/api/v1/users/me` | Aktif kullanıcı bilgisi |
| `PUT` | `/api/v1/users/me` | Profil güncelleme |
| `GET` | `/api/v1/users/{id}` | Kullanıcı detayı |
| `GET` | `/api/v1/users/internal/{id}` | Servis içi kullanıcı bilgisi |

### Adres

| Metot | Endpoint | Açıklama |
| --- | --- | --- |
| `POST` | `/api/v1/users/me/addresses` | Adres oluşturma |
| `GET` | `/api/v1/users/me/addresses` | Kullanıcı adresleri |
| `PUT` | `/api/v1/users/me/addresses/{addressId}` | Adres güncelleme |
| `PATCH` | `/api/v1/users/me/addresses/{addressId}/default` | Varsayılan adres seçme |
| `DELETE` | `/api/v1/users/me/addresses/{addressId}` | Adres silme |

### Satıcı

| Metot | Endpoint | Açıklama |
| --- | --- | --- |
| `GET` | `/api/v1/sellers/me` | Satıcı profilim |
| `PUT` | `/api/v1/sellers/me` | Satıcı profili güncelleme |
| `GET` | `/api/v1/sellers` | Satıcı filtreli listeleme |
| `GET` | `/api/v1/sellers/applications/pending` | Bekleyen satıcı başvuruları |
| `PATCH` | `/api/v1/sellers/{sellerProfileId}/status` | Satıcı onay/red/suspend işlemleri |

### Ürün ve Kategori

| Metot | Endpoint | Açıklama |
| --- | --- | --- |
| `POST` | `/api/v1/products` | Ürün oluşturma |
| `PUT` | `/api/v1/products/{id}` | Ürün güncelleme |
| `GET` | `/api/v1/products/{id}` | Ürün detayı |
| `GET` | `/api/v1/products/slug/{slug}` | Slug ile ürün detayı |
| `GET` | `/api/v1/products` | Ürün listeleme ve filtreleme |
| `GET` | `/api/v1/products/seller/{sellerId}` | Satıcı ürünleri |
| `DELETE` | `/api/v1/products/{id}` | Ürün silme |
| `GET` | `/api/v1/categories` | Kategori listesi |
| `GET` | `/api/v1/categories/tree` | Kategori ağacı |
| `GET` | `/api/v1/categories/filters` | Filtre kategorileri |

### Sepet, Sipariş, Ödeme

| Metot | Endpoint | Açıklama |
| --- | --- | --- |
| `GET` | `/api/v1/carts` | Aktif sepet |
| `POST` | `/api/v1/carts` | Sepete ürün ekleme |
| `DELETE` | `/api/v1/carts` | Sepeti temizleme |
| `POST` | `/api/v1/orders/checkout` | Checkout başlatma |
| `GET` | `/api/v1/orders/{orderId}` | Sipariş detayı |
| `GET` | `/api/v1/orders/my-orders` | Kullanıcının siparişleri |
| `GET` | `/api/v1/orders/{orderId}/items` | Sipariş kalemleri |
| `DELETE` | `/api/v1/orders/{orderId}` | Sipariş iptali |
| `POST` | `/api/v1/payments/pay` | Ödeme alma |

### Bildirim

| Metot | Endpoint | Açıklama |
| --- | --- | --- |
| `GET` | `/api/v1/notifications/unread` | Okunmamış bildirimler |
| `GET` | `/api/v1/notifications` | Sayfalı bildirim listesi |
| `PATCH` | `/api/v1/notifications/read-all` | Tüm bildirimleri okundu yapma |
| `PATCH` | `/api/v1/notifications/{id}/read` | Tek bildirimi okundu yapma |
| `WS` | `/ws` | WebSocket bağlantısı |

## Frontend Notu

Frontend React/Vite ile geliştirilmiştir, ancak bu dokümanın ana kapsamı backend'dir. Frontend container içinde Nginx ile servis edilir:

- `/api/` istekleri `api-gateway:8763` servisine proxy edilir.
- `/ws` istekleri `notification-service:8771` servisine proxy edilir.
- Token yönetimi Axios interceptor ile yapılır.

## Çalıştırma

### Gereksinimler

- Java 21
- Maven 3.9+
- Docker
- Docker Compose

Frontend'i ayrıca geliştirmek isterseniz Node.js 22+ gerekir, fakat backend stack Docker Compose ile çalıştırılabilir.

### Environment Dosyası

Backend için `backend/.env.example` dosyasından `backend/.env` oluşturulur.

```env
POSTGRES_USER=postgres
POSTGRES_PASSWORD=change-me
RABBITMQ_USERNAME=guest
RABBITMQ_PASSWORD=change-me
CONFIG_GIT_URI=https://github.com/your-org/your-config-repo.git
CONFIG_GIT_BRANCH=main
GIT_USERNAME=change-me
GIT_TOKEN=change-me
JWT_KEY=change-me
JWT_ISSUER_URI=http://keycloak:8080/realms/microservice-realm/protocol/openid-connect/token
JWT_CLIENT_SECRET=change-me
AWS_ACCESS_KEY_ID=change-me
AWS_SECRET_ACCESS_KEY=change-me
AWS_S3_BUCKET_NAME=change-me
IYZICO_API_KEY=change-me
IYZICO_SECRET_KEY=change-me
IYZICO_BASE_URL=https://sandbox-api.iyzipay.com
FRONTEND_PORT=80
VITE_API_BASE_URL=/api
VITE_APP_NAME=N11 Marketplace
```

### Docker Compose ile Backend Stack

```powershell
git clone https://github.com/sezerdemir7/n11-talenthub-final-case.git
cd n11-talenthub-final-case\backend
copy .env.example .env
docker compose -f docker-compose.backend.yml --env-file .env up -d --build
```

Servis adresleri:

| Uygulama | Adres |
| --- | --- |
| API Gateway | `http://localhost:8763` |
| Eureka Dashboard | `http://localhost:8761` |
| Config Server | `http://localhost:8762` |
| RabbitMQ Management | `http://localhost:15672` |
| PostgreSQL | `localhost:5433` |
| Grafana | `http://localhost:3000` |
| Loki | `http://localhost:3100` |

### Tek Servis Çalıştırma

Örnek:

```powershell
git clone https://github.com/sezerdemir7/n11-talenthub-final-case.git
cd n11-talenthub-final-case\backend\user-service
mvn spring-boot:run
```

Servisleri tek tek çalıştırırken Config Server, Discovery Server, PostgreSQL ve RabbitMQ bağımlılıklarının aktif olduğundan emin olunmalıdır.

## Testler

Backend testleri unit ve integration olarak ayrılır.

Unit testlerde:

- Repository, publisher, Feign client ve dış servis bağımlılıkları mocklanır.
- Service katmanı iş kuralları doğrulanır.
- JWT, auth, seller, address gibi domain davranışları izole test edilir.

Integration testlerde:

- Spring context yüklenir.
- H2 test veritabanı kullanılır.
- Security context ve profile ayarları test ortamına göre hazırlanır.
- RabbitMQ gibi dış bağımlılıklar mock veya test profile ile izole edilir.

Doğrulanmış test komutları:

```powershell
cd C:\Users\Demirr\Desktop\n11-final-case\backend\user-service
mvn test
```

Son doğrulamada `user-service` için 68 test geçti.

```powershell
cd C:\Users\Demirr\Desktop\n11-final-case\backend\api-gateway
mvn test
```

Son doğrulamada `api-gateway` için 12 test geçti.

Test notları:

- Mockito dynamic agent uyarısı test hatası değildir.
- Spring Boot 3.4 sonrası `@MockBean` deprecated olduğu için yeni testlerde `@MockitoBean` tercih edilmelidir.
- Integration testler gerçek RabbitMQ veya Config Server'a bağımlı olmamalıdır.

## Gözlemlenebilirlik

Backend servislerinde log ve takip edilebilirlik için correlation id yaklaşımı kullanılır.

![Backend loglama ve gözlemlenebilirlik akışı](../docs/observability-flow.svg)

Öne çıkanlar:

- `X-Correlation-Id` Gateway tarafından üretilir veya taşınır.
- Access log ile method, path, status, süre ve client IP loglanır.
- Logback JSON encoder kullanılır.
- Actuator endpointleri operasyonel sağlık kontrolü sağlar.
- Loki, Promtail ve Grafana Docker Compose ile ayağa kalkar.

## CI/CD ve Dağıtım

`Jenkinsfile`, backend deploy sürecini otomatikleştirir.

Pipeline adımları:

1. Repository checkout edilir.
2. Jenkins credential olarak saklanan backend environment dosyası `backend/.env` olarak kopyalanır.
3. Docker Compose config doğrulaması yapılır.
4. Backend stack build edilerek ayağa kaldırılır.
5. Container durumları raporlanır.

Ana deploy komutu:

```powershell
docker compose -f docker-compose.backend.yml --env-file .env up -d --build
```

## Proje Yapısı

```text
n11-final-case/
|-- README.md
|-- Jenkinsfile
|-- docs/
|   |-- architecture-overview.svg
|   |-- backend-communication-flow.svg
|   |-- checkout-saga-flow.svg
|   |-- domain-event-flows.svg
|   |-- gateway-security-flow.svg
|   `-- observability-flow.svg
|-- backend/
|   |-- api-gateway/
|   |-- cart-service/
|   |-- common-lib/
|   |-- config-server/
|   |-- discovery-server/
|   |-- notification-service/
|   |-- order-service/
|   |-- payment-service/
|   |-- product-service/
|   |-- user-service/
|   |-- deploy/
|   |-- logs/
|   |-- docker-compose.backend.yml
|   |-- Dockerfile
|   `-- .env.example
`-- frontend/
    |-- src/
    |-- Dockerfile
    |-- nginx.conf
    |-- package.json
    `-- vite.config.js
```

## Güvenlik Notları

- JWT secret, Git token, AWS key, Iyzico key ve database şifreleri repository içine yazılmamalıdır.
- Config değerleri `.env`, Jenkins credentials veya güvenli secret manager üzerinden verilmelidir.
- Downstream servisler `X-User-Id` ve `X-User-Roles` headerlarını yalnızca API Gateway'den geldiyse güvenilir kabul etmelidir.
- RabbitMQ management, PostgreSQL, Grafana ve Actuator endpointleri production ortamda public internete açılmamalıdır.
- CORS ayarları production ortamda izin verilen origin listesiyle sınırlandırılmalıdır.

## Backend Geliştirme Notları

- Yeni servis eklenirse Eureka registration, Config Server import, Docker Compose service ve Gateway route birlikte güncellenmelidir.
- Yeni domain eventleri `common-lib` içinde tanımlanmalı ve routing key/queue bilgisi merkezi tutulmalıdır.
- Kritik domainlerde unit test yanında integration test de yazılmalıdır.
- Saga akışlarında her başarı eventinin hata/telafi senaryosu da düşünülmelidir.
- Servisler başka servislerin tablolarına doğrudan erişmemelidir; API veya event üzerinden iletişim kurulmalıdır.
