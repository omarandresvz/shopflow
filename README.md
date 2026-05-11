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

Endpoints:

```http
GET    /api/v1/products           → Público
POST   /api/v1/products           → ADMIN
PUT    /api/v1/products/{id}      → ADMIN
DELETE /api/v1/products/{id}      → ADMIN
```

---

### 🧾 order-service (8083)

Responsable de:

* Creación de órdenes
* Consulta de órdenes por usuario

Endpoints:

```http
POST /api/v1/orders        → CUSTOMER
GET  /api/v1/orders/my     → CUSTOMER
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
  "path": "/api/v1/products/10"
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

### 1. Login

```http
POST /api/v1/auth/login
```

---

### 2. Crear producto (ADMIN)

```http
POST /api/v1/products
Authorization: Bearer <ADMIN_TOKEN>
```

---

### 3. Crear orden (CUSTOMER)

```http
POST /api/v1/orders
Authorization: Bearer <CUSTOMER_TOKEN>
```

---

### 4. Ver órdenes

```http
GET /api/v1/orders/my
Authorization: Bearer <CUSTOMER_TOKEN>
```

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
* Manejo de stock
* Estados de órdenes

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

# 👨‍💻 Autor

**Omar Vergara Zamorano**
Backend Developer