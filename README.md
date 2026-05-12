# 🛒 ShopFlow - E-commerce con Microservicios (Spring Boot)

## 🚀 Descripción

ShopFlow es un backend de e-commerce basado en arquitectura de microservicios, construido con **Spring Boot 3.5.14**, **Java 21** y **Spring Cloud Gateway**.

Este proyecto fue desarrollado como portafolio profesional, aplicando prácticas reales de la industria:

* Arquitectura de microservicios
* Autenticación stateless con JWT
* Seguridad distribuida
* Configuración mediante variables de entorno
* Módulo reutilizable de seguridad y validaciones (`shared`)

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

Archivo de ejemplo:

```text
.env.example
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

## 🐳 Base de datos con Docker

El proyecto utiliza PostgreSQL ejecutándose en Docker.

Ejemplo de ejecución:

```bash
docker compose up -d
```

Bases de datos necesarias:

```sql
CREATE DATABASE auth_db;
CREATE DATABASE product_db;
CREATE DATABASE order_db;
```

Configuración local:

```text
Host: localhost
Port: 5433
User: postgres
Password: postgres
```

---

## ▶️ Cómo ejecutar el proyecto

### 1. Clonar repositorio

```bash
git clone https://github.com/omarandresvz/shopflow.git
cd shopflow
```

---

### 2. Crear archivo `.env`

Basado en `.env.example`

---

### 3. Levantar base de datos

```bash
docker compose up -d
```

---

### 4. Compilar proyecto

Desde la raíz del proyecto:

```bash
mvn clean install
```

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

---

## 📌 Mejoras futuras

* Dockerización completa del sistema
* API Documentation (Swagger/OpenAPI)
* Comunicación asíncrona con Kafka/RabbitMQ
* Notificaciones

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

# 👨‍💻 Autor

**Omar Vergara Zamorano**
Backend Developer