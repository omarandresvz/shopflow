# 🛒 ShopFlow - E-commerce con Microservicios (Spring Boot)

## 🚀 Descripción

ShopFlow es un backend de e-commerce basado en arquitectura de microservicios, construido con **Spring Boot 3.5.14**, **Java 21** y **Spring Cloud Gateway**.

Este proyecto fue desarrollado como portafolio profesional, aplicando prácticas reales de la industria:

* Arquitectura de microservicios
* Autenticación stateless con JWT
* Seguridad distribuida
* Configuración mediante variables de entorno
* Módulo reutilizable de seguridad y validaciones (`shared`)
* Documentación interactiva de APIs
* Testing unitario y de controladores

---

## 🧱 Arquitectura

```text
Cliente
  ↓
API Gateway (8080)
  ↓
---------------------------------
|        |         |             |
Auth     Product   Order         |
Service  Service   Service       |
(8081)   (8082)    (8083)        |
---------------------------------
```

---

### Conceptos clave

* API Gateway centraliza el acceso
* Cada microservicio valida el JWT de forma independiente
* No se usan sesiones (stateless)
* Infraestructura compartida mediante módulo `shared`

---

## 🔐 Autenticación

Se utiliza **JWT (JSON Web Token)**.

### Flujo

1. Usuario inicia sesión en `auth-service`
2. Recibe un token JWT
3. El token se envía en cada request:

```http
Authorization: Bearer <token>
```

### Ejemplo de token

```json
{
  "sub": "usuario@email.com",
  "userId": 1,
  "role": "ADMIN"
}
```

---

## 📦 Microservicios

### 🔐 auth-service (8081)

Responsable de:

* Registro de usuarios
* Login
* Generación de JWT

Endpoints:

```http
POST /api/v1/auth/register
POST /api/v1/auth/login
GET  /api/v1/auth/me
```

---

### 📦 product-service (8082)

Responsable de:

* Gestión de productos
* Control de stock

Endpoints:

```http
GET    /api/v1/products                         → Público
GET    /api/v1/products/{id}                    → Público
POST   /api/v1/products                         → ADMIN
PUT    /api/v1/products/{id}                    → ADMIN
DELETE /api/v1/products/{id}                    → ADMIN
PATCH  /api/v1/products/{id}/stock/decrease     → Interno
PATCH  /api/v1/products/{id}/stock/increase     → Interno
```

---

### 🧾 order-service (8083)

Responsable de:

* Creación de órdenes
* Consulta de órdenes
* Flujo de estados de órdenes
* Validación de ownership
* Restauración de stock al cancelar órdenes

Endpoints:

```http
POST  /api/v1/orders                 → CUSTOMER
GET   /api/v1/orders/my              → CUSTOMER
PATCH /api/v1/orders/{id}/cancel     → CUSTOMER / ADMIN
PATCH /api/v1/orders/{id}/pay        → ADMIN
PATCH /api/v1/orders/{id}/ship       → ADMIN
PATCH /api/v1/orders/{id}/deliver    → ADMIN
```

---

### 🌐 api-gateway (8080)

Responsable de:

* Enrutamiento
* Punto de entrada único

Rutas:

```text
/api/v1/auth/**      → auth-service
/api/v1/products/**  → product-service
/api/v1/orders/**    → order-service
```

---

## 🔄 Comunicación entre Microservicios

La comunicación entre servicios se realiza mediante REST utilizando `RestClient`.

Ejemplo:

* `order-service` consulta productos en `product-service`
* Validación de stock antes de crear órdenes
* Reducción automática de stock al crear órdenes
* Restauración automática de stock al cancelar órdenes
* Traducción de errores remotos a excepciones de negocio locales

Flujo simplificado:

```text
order-service
    ↓
product-service
    ↓
404 PRODUCT_NOT_FOUND
    ↓
OrderProductNotFoundException
```

---

### 🔐 shared

Módulo compartido reutilizable utilizado por todos los microservicios.

Incluye:

* Validación y generación de JWT
* Filtro de autenticación
* Modelo `CurrentUser`
* Manejo global de excepciones
* `BusinessException`
* `ErrorCode`
* `ErrorResponse`
* Handlers de seguridad (`401` / `403`)
* Configuración reutilizable

---

## 🔁 Flujo de Estados de Órdenes

Las órdenes siguen el siguiente flujo:

```text
CREATED → PAID → SHIPPED → DELIVERED
```

Cancelaciones permitidas:

```text
CREATED → CANCELLED
PAID    → CANCELLED
```

Estados finales:

```text
DELIVERED
CANCELLED
```

El sistema valida automáticamente:

* Transiciones válidas
* Ownership de órdenes
* Roles de usuario
* Restauración de stock en cancelaciones

---

## 🔐 Reglas de Autorización

**CUSTOMER**

Puede:

* Crear órdenes
* Consultar sus propias órdenes
* Cancelar sus propias órdenes

No puede:

* Marcar órdenes como pagadas
* Despachar órdenes
* Entregar órdenes
* Cancelar órdenes ajenas

**ADMIN**

Puede:

* Gestionar productos
* Cambiar estados de órdenes
* Cancelar cualquier orden

---

## ⚠️ Manejo Global de Errores

El proyecto implementa un sistema centralizado y consistente de manejo de errores.

Características:

* Respuestas JSON estandarizadas
* Manejo global mediante `GlobalExceptionHandler`
* `BusinessException` desacopladas
* `ErrorCode` por microservicio
* Manejo personalizado de errores JWT (`401` / `403`)
* Traducción de errores entre microservicios
* Manejo global de errores en API Gateway

### Ejemplo de respuesta

```json
{
  "timestamp": "2026-05-10T22:00:00",
  "status": 404,
  "error": "Not Found",
  "message": "Producto no encontrado",
  "code": "PRODUCT_NOT_FOUND",
  "path": "/api/v1/products/10",
  "errors": []
}
```


---

## ⚙️ Configuración (Variables de entorno)

El proyecto utiliza variables de entorno para evitar exponer datos sensibles.

Archivos de ejemplo:

```text
.env.docker.example
```

Ejemplo:

```env
# JWT
JWT_SECRET=reemplazar_con_secreto_seguro
JWT_EXPIRATION=3600000

# Auth DB
AUTH_SERVICE_DB_URL=jdbc:postgresql://localhost:5433/auth_db
AUTH_SERVICE_DB_USERNAME=postgres
AUTH_SERVICE_DB_PASSWORD=postgres

# Product DB
PRODUCT_SERVICE_DB_URL=jdbc:postgresql://localhost:5433/product_db
PRODUCT_SERVICE_DB_USERNAME=postgres
PRODUCT_SERVICE_DB_PASSWORD=postgres

# Order DB
ORDER_SERVICE_DB_URL=jdbc:postgresql://localhost:5433/order_db
ORDER_SERVICE_DB_USERNAME=postgres
ORDER_SERVICE_DB_PASSWORD=postgres
```

---

## 🐳 Docker & Docker Compose

El proyecto puede ejecutarse completamente utilizando Docker Compose.

### Servicios incluidos

```text
api-gateway
auth-service
product-service
order-service
postgres
```

**Levantar todo el ecosistema**

```bash
docker compose up --build
```

**Ejecutar en segundo plano**

```bash
docker compose up -d --build
```

**Detener contenedores**

```bash
docker compose down
```

**Reconstruir imágenes**

```bash
docker compose build --no-cache
```
---

### Variables de entorno

Todos los servicios utilizan variables de entorno definidas en:

```text
.env.docker
```

El archivo de ejemplo:

```text
.env.docker.example
```

---

### Comunicación entre servicios

Los servicios se comunican mediante la red Docker interna `shopflow-network`.

```text
api-gateway → auth-service:8081
order-service → product-service:8082
```

---

### Healthchecks

Cada servicio expone endpoints de monitoreo mediante Spring Boot Actuator.

Endpoint utilizado por Docker Compose:

```text
/actuator/health
```

Docker Compose utiliza estos endpoints para verificar disponibilidad de servicios.

---

## ▶️ Cómo ejecutar el proyecto

### 1. Clonar repositorio

```bash
git clone https://github.com/omarandresvz/shopflow.git
cd shopflow
```

---

### 2. Crear archivo `.env.docker`

Basado en `.env.docker.example`

---

### 3. Ejecutar todo el ecosistema

```bash
docker compose up --build
```

---

### 4. Acceder al sistema

**Gateway:**

```text
http://localhost:8080
```

**Swagger:**

```text
http://localhost:8081/swagger-ui.html
http://localhost:8082/swagger-ui.html
http://localhost:8083/swagger-ui.html
```

---

### Persistencia de datos

PostgreSQL utiliza volúmenes Docker para persistir información entre reinicios de contenedores.

---

## 📘 Documentación API (Swagger/OpenAPI)

Cada microservicio expone su propia documentación OpenAPI mediante Swagger UI.

URLs disponibles:

```text
http://localhost:8081/swagger-ui.html
http://localhost:8082/swagger-ui.html
http://localhost:8083/swagger-ui.html
```

Características:

* Documentación interactiva
* Soporte para JWT Bearer Token
* Seguridad y respuestas HTTP documentadas

---

## 🔄 Flujo de uso

### 1. Registrar usuario

```http
POST /api/v1/auth/register
```

---

### 2. Login

```http
POST /api/v1/auth/login
```

---

### 3. Crear producto (ADMIN)

```http
POST /api/v1/products
Authorization: Bearer <ADMIN_TOKEN>
```

---

### 4. Consultar productos

```http
GET /api/v1/products
```

---

### 5. Crear orden (CUSTOMER)

```http
POST /api/v1/orders
Authorization: Bearer <CUSTOMER_TOKEN>
```

El sistema:

* Valida productos
* Valida stock
* Calcula total
* Reduce stock automáticamente

---

### 6. Pagar orden (ADMIN)

```http
PATCH /api/v1/orders/{id}/pay
Authorization: Bearer <admin-token>
```

---

### 7. Despachar orden (ADMIN)

```http
PATCH /api/v1/orders/{id}/ship
Authorization: Bearer <admin-token>
```

---

### 8. Entregar orden (ADMIN)

```http
PATCH /api/v1/orders/{id}/deliver
Authorization: Bearer <admin-token>
```

---

### 9. Cancelar orden

**CUSTOMER**

Puede cancelar únicamente sus propias órdenes.

**ADMIN**

Puede cancelar cualquier orden.

```http
PATCH /api/v1/orders/{id}/cancel
```

El sistema:

* Valida ownership
* Valida transición de estado
* Restaura stock automáticamente

---

## 🛠 Tecnologías utilizadas

* Java 21
* Spring Boot 3.5.14
* Spring Security
* Spring Cloud Gateway
* Spring WebFlux
* Spring Data JPA / Hibernate
* PostgreSQL
* Maven
* JWT (jjwt)
* RestClient
* Bean Validation
* Swagger / OpenAPI 
* JUnit 5
* Mockito
* Docker
* Docker Compose
* GitHub Actions (CI)

---

## 🧪 Testing

El proyecto incluye pruebas unitarias y pruebas de controladores para validar la lógica de negocio y los endpoints HTTP.

### Cobertura actual

**Service Tests**

* AuthServiceImplTest
* ProductServiceImplTest
* OrderServiceImplTest

Valida:

* Registro y login
* Validaciones de negocio
* Manejo de excepciones
* Flujo de estados de órdenes
* Validación de stock
* Restauración de stock
* Ownership validation

---

**Controller Tests**

* AuthControllerTest
* ProductControllerTest
* OrderControllerTest

Valida:

* Status HTTP
* Respuestas JSON
* Endpoints REST
* Requests y responses HTTP
* Validaciones HTTP
* Endpoints protegidos

---

**Tecnologías utilizadas en testing**

* JUnit 5
* Mockito
* AssertJ
* MockMvc
* Spring Boot Test
* Spring Security Test

---

**Ejecutar tests**

Desde la raíz del proyecto:

```bash
mvn test
```

---

## 🔄 Continuous Integration

El proyecto utiliza GitHub Actions para validar automáticamente cambios en cada push y Pull Request.

Actualmente el pipeline ejecuta:

```text
✔ Maven build
✔ Unit tests
✔ Controller tests
✔ Docker image validation
```

El workflow se encuentra en:

```text
.github/workflows/ci.yml
```

---

## 📌 Mejoras futuras

* Comunicación asíncrona con Kafka/RabbitMQ
* Notificaciones
* Tests de integración con Testcontainers

---

## 🎯 Objetivo del proyecto

Demostrar:

* Arquitectura de microservicios real
* Seguridad distribuida con JWT
* API Gateway Pattern
* Comunicación REST entre servicios
* Manejo profesional de errores
* Código limpio y mantenible
* Buenas prácticas backend
* Diseño escalable y desacoplado
* Role-Based Access Control (RBAC)
* Ownership validation
* Domain state flow
* Documentación profesional de APIs con Swagger/OpenAPI
* Testing backend profesional
* Arquitectura containerizada con Docker Compose
* Integración continua (CI)
---

# 👨‍💻 Autor

**Omar Vergara Zamorano**
Backend Developer