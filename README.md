# 💳 Venta Service

## 📌 Descripción
Microservicio central dentro del ecosistema de ElectrodoStore, encargado de gestionar el proceso de ventas de la plataforma.

Este servicio actúa como **orquestador de ventas**, asegurando la consistencia del negocio mediante la integración con otros microservicios, validando:

- Existencia del cliente
- Disponibilidad de productos
- Consistencia del stock antes y después de la compra

---

## 🧩 Responsabilidades

- Registrar nuevas ventas
- Consultar ventas por ID o por cliente
- Actualizar y eliminar ventas
- Validar y descontar stock de productos
- Mantener snapshots de cliente y productos al momento de la compra
- Manejar errores distribuidos mediante ErrorDecoder

---

## ⚙️ Tecnologías utilizadas

- Java + Spring Boot
- Spring Data JPA
- MySQL
- Spring Cloud (Eureka Client)
- OpenFeign
- Resilience4j (Circuit Breaker + Retry)

---

## 🗄️ Modelo de dominio

### 🧾 Venta

| Campo | Descripción |
|---|---|
| `id` | Identificador único |
| `date` | Fecha de la venta |
| `totalItems` | Cantidad total de productos |
| `totalPrice` | Precio total calculado |
| `listProducts` | Snapshot de productos |
| `client` | Snapshot del cliente |

### 📌 Snapshots

Los snapshots preservan el estado del cliente y los productos **en el momento exacto de la venta**, evitando inconsistencias ante futuros cambios en los datos originales.

---

## 🔗 Endpoints principales

| Método | Endpoint | Descripción |
|---|---|---|
| `GET` | `/ventas` | Listar todas las ventas |
| `GET` | `/ventas/{id}` | Obtener venta por ID |
| `GET` | `/ventas/traer-ventas-de-cliente/{clientId}` | Ventas por cliente |
| `POST` | `/ventas` | Registrar nueva venta |
| `PUT` | `/ventas/{id}` | Actualizar venta completa |
| `PATCH` | `/ventas/{id}` | Actualización parcial |
| `DELETE` | `/ventas/{id}` | Eliminar venta |

---

## 🔄 Integración con otros servicios

### 👤 cliente-service
- Validación de existencia del cliente
- Obtención de datos para snapshot

| Método | Endpoint | Descripción |
|---|---|---|
| `GET` | `/clientes/{clientId}` | Obtener cliente por ID |

### 🛍️ producto-service
- Consulta de productos
- Validación de stock
- Descuento y reposición de stock


| Método | Endpoint | Descripción |
|---|---|---|
| `GET` | `/productos/{productoId}` | Obtener producto por ID |
| `POST` | `/productos/traer-productos-por-ids` | Obtener lista de productos por IDs |
| `POST` | `/productos/verificar-stock` | Validar stock de productos |
| `PATCH` | `/productos/descontar-stock` | Descontar stock |
| `PATCH` | `/productos/reponer-stock` | Reponer stock |

La comunicación se realiza mediante **Spring Cloud OpenFeign**.

---

## 🛡️ Resiliencia (Circuit Breaker + Retry)

La comunicación con servicios externos está protegida mediante **Resilience4j**:

- **Circuit Breaker** → Evita llamadas repetidas a un servicio caído
- **Retry** → Reintenta automáticamente en fallos transitorios
- **Fallback** → Proporciona una respuesta controlada en caso de error

### ⚙️ Configuración base
```yaml
slidingWindowSize: 10
failureRateThreshold: 50
waitDurationInOpenState: 30s
```

### 🔀 Circuitos separados por operación

| Circuito | Uso | Excepciones ignoradas |
|---|---|---|
| `producto-service-read` | Consultas de producto | `ProductoNotFoundException` |
| `producto-service-write` | Stock (validar/descontar/reponer) | `ProductoNotFoundException`, `ProductoStockInsuficienteException` |
| `cliente-service` | Validación de cliente | `ClienteNotFoundException` |

---

## 🔁 Estrategia de manejo de errores

Los errores se clasifican en dos categorías:

- **Errores de negocio** → se propagan directamente
- **Errores de infraestructura** → se convierten en `ServiceUnavailable`
```java
if (ex instanceof BusinessException be) {
    throw be;
}
throw new ServiceUnavailableException("Error de comunicación...");
```

---

## 🔍 Manejo de errores distribuido (ErrorDecoder)

- Se implementan `ErrorDecoder` personalizados para traducir errores HTTP en excepciones de dominio:
```java
switch (VentaErrorCode.valueOf(error.getErrorCode())) {
    case PRODUCT_NOT_FOUND:
        return new ProductoNotFoundException(error.getMensaje());
}
```

Esto permite mantener consistencia en el dominio sin depender de códigos HTTP externos.

---

## ⚠️ Manejo global de excepciones

Centralizado mediante `@RestControllerAdvice`.

### Excepciones manejadas:
- `VentaNotFoundException`
- `ClienteNotFoundException`
- `ProductoNotFoundException`
- `ProductoStockInsuficienteException`
- `ServiceUnavailable`

### Estructura de respuesta de error:
```json
{
  "timestamp": "2026-03-28T12:00:00",
  "status": 404,
  "error": "NOT_FOUND",
  "errorCode": "PRODUCT_NOT_FOUND",
  "mensaje": "Producto no encontrado"
}
```

---

## 💡 Decisiones de diseño

- Snapshots para consistencia histórica de ventas
- Circuitos separados por tipo de operación (lectura / escritura)
- Separación entre errores de negocio e infraestructura
- Manejo centralizado de excepciones
- DTOs diferenciados para entrada y salida de datos
- Comunicación desacoplada mediante Feign

---

## ▶️ Ejecución local

**Con Maven**
```bash
# Corre en el puerto 9191
mvn spring-boot:run
```

**Con Docker**
```bash
docker build -t venta-service .
```

> ⚠️ Requiere que **Config Server** y **Eureka Server** estén corriendo antes de iniciar este servicio.

---

## 🔌 Configuración de red

| Propiedad | Valor                  |
|---|------------------------|
| Puerto interno | `9191`                 |
| Acceso externo | ❌ Solo vía API Gateway |

---

## 🚀 Mejoras futuras

- Implementación de autenticación (JWT / OAuth2)
- Eventos asincrónicos con Kafka / RabbitMQ
- Observabilidad (tracing distribuido + logs centralizados)
- Pruebas automatizadas

---