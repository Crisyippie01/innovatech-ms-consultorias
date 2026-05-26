# ms-consultorias

Microservicio de gestión de consultorías para la plataforma **InnovaTech**.

## Descripción

Este microservicio gestiona el requerimiento de Consultorías (RF-2) de InnovaTech. Permite a los clientes solicitar consultorías, realizar seguimiento de estados y consultar historiales.

## Características Principales

- **Solicitar Consultoría**: Registrar solicitudes con tema, descripción y fecha sugerida
- **Gestión de Estados**: Seguimiento de consultorías (PENDIENTE, APROBADA, FINALIZADA, CANCELADA)
- **Historial**: Consultar consultorías por usuario con filtros por estado
- **Arquitectura Event-Driven**: Emite eventos `Consultoria_Solicitada` vía RabbitMQ para integración con otros módulos

## Stack Tecnológico

| Tecnología | Versión |
|------------|---------|
| Spring Boot | 4.0.6 |
| Java | 21 |
| Maven | - |
| Base de Datos (Dev) | H2 (en memoria) |
| Base de Datos (Prod) | MySQL |
| Mensajería | RabbitMQ |
| Documentación | Springdoc OpenAPI / Swagger |

## Requisitos

- Java 21 o superior
- Maven 3.8+
- RabbitMQ 3.x (opcional para desarrollo)
- MySQL 8.x (solo para producción)

## Configuración

### Perfiles

El microservicio soporta dos perfiles:

#### Desarrollo (default) - H2 Database
```bash
mvn spring-boot:run
# o
java -jar target/ms-consultorias-0.0.1-SNAPSHOT.jar
```

#### Producción - MySQL Database
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=prod
# o
java -jar -Dspring.profiles.active=prod target/ms-consultorias-0.0.1-SNAPSHOT.jar
```

### Variables de Entorno (Producción)

| Variable | Descripción | Default |
|----------|-------------|---------|
| `SERVER_PORT` | Puerto del servidor | 8086 |
| `RABBITMQ_HOST` | Host de RabbitMQ | localhost |
| `RABBITMQ_PORT` | Puerto de RabbitMQ | 5672 |
| `RABBITMQ_USERNAME` | Usuario RabbitMQ | rabbit_local_user |
| `RABBITMQ_PASSWORD` | Contraseña RabbitMQ | rabbit_local_password |

### RabbitMQ con Docker

```bash
docker run -d --name rabbitmq \
  -p 5672:5672 \
  -p 15672:15672 \
  rabbitmq:3-management
```

Consola de administración: http://localhost:15672 

## Endpoints API

Base URL: `http://localhost:8086/api/v1/consultorias`

### Documentación Swagger
- Swagger UI: http://localhost:8086/swagger-ui.html
- API Docs: http://localhost:8086/api-docs

### Endpoints Disponibles

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| POST | `/` | Registrar nueva consultoría |
| GET | `/{id}` | Obtener consultoría por ID |
| GET | `/usuario/{usuarioId}` | Listar consultorías por usuario |
| GET | `/usuario/{usuarioId}/estado/{estado}` | Filtrar por usuario y estado |
| PATCH | `/{id}/estado/{estado}` | Actualizar estado de consultoría |

### Ejemplos de Uso

#### Crear Consultoría
```bash
curl -X POST http://localhost:8086/api/v1/consultorias \
  -H "Content-Type: application/json" \
  -d '{
    "usuarioId": 1,
    "tema": "Consultoría de Transformación Digital",
    "descripcion": "Necesitamos asesoría para la migración de nuestros sistemas legacy a la nube",
    "fechaSugerida": "2026-05-15"
  }'
```

**Respuesta:**
```json
{
  "id": 1,
  "usuarioId": 1,
  "tema": "Consultoría de Transformación Digital",
  "descripcion": "Necesitamos asesoría para la migración de nuestros sistemas legacy a la nube",
  "fechaSolicitud": "2026-05-05T10:30:00",
  "fechaSugerida": "2026-05-15",
  "estado": "PENDIENTE"
}
```

#### Listar Consultorías por Usuario
```bash
curl http://localhost:8086/api/v1/consultorias/usuario/1
```

#### Filtrar por Estado
```bash
curl http://localhost:8086/api/v1/consultorias/usuario/1/estado/PENDIENTE
```

#### Actualizar Estado
```bash
curl -X PATCH http://localhost:8086/api/v1/consultorias/1/estado/APROBADA
```

## Arquitectura de Eventos

Cuando se registra exitosamente una consultoría, el sistema emite un evento `Consultoria_Solicitada` a través de RabbitMQ.

### Configuración de RabbitMQ

| Componente | Nombre |
|------------|--------|
| Exchange | `consultoria.exchange` (Topic) |
| Queue | `consultoria.queue` |
| Routing Key | `consultoria.routingkey` |

### Estructura del Evento

```json
{
  "id": 1,
  "usuarioId": 1,
  "tema": "Consultoría de Transformación Digital",
  "descripcion": "Necesitamos asesoría...",
  "fechaSolicitud": "2026-05-05T10:30:00",
  "fechaSugerida": "2026-05-15",
  "estado": "PENDIENTE",
  "eventType": "Consultoria_Solicitada"
}
```

> **Nota**: Si RabbitMQ no está disponible, la consultoría se guarda correctamente y se loguea una advertencia.

## Estados de Consultoría

| Estado | Descripción |
|--------|-------------|
| PENDIENTE | Solicitud recibida, pendiente de revisión |
| APROBADA | Consultoría aprobada, en preparación |
| FINALIZADA | Consultoría completada exitosamente |
| CANCELADA | Consultoría cancelada |

## Estructura del Proyecto

```
com.innovatech.ms_consultorias/
├── config/
│   └── RabbitMQConfig.java          # Configuración de mensajería
├── controller/
│   └── ConsultoriaController.java   # REST Controller
├── dto/
│   ├── request/
│   │   └── ConsultoriaRequestDTO.java
│   └── response/
│       ├── ConsultoriaResponseDTO.java
│       └── ConsultoriaEventDTO.java
├── exception/
│   ├── ConsultoriaNotFoundException.java
│   └── GlobalExceptionHandler.java  # Manejo global de errores
├── model/
│   ├── enums/
│   │   └── EstadoConsultoria.java
│   └── Consultoria.java             # Entidad JPA
├── repository/
│   └── ConsultoriaRepository.java   # Repositorio Spring Data JPA
└── service/
    └── ConsultoriaService.java      # Lógica de negocio
```

## Base de Datos

### Desarrollo (H2)
- **URL**: jdbc:h2:mem:consultorias_db
- **Console**: http://localhost:8086/h2-console
- **Usuario**: sa
- **Contraseña**: (vacía)

### Producción (MySQL)
- **Base de Datos**: `consultorias_db`
- **DDL**: `validate`

## Compilación y Ejecución

### Compilar
```bash
mvn clean package
```

### Ejecutar Tests
```bash
mvn test
```

### Ejecutar Aplicación
```bash
mvn spring-boot:run
```

## Health Checks

- **Health**: http://localhost:8086/actuator/health
- **Info**: http://localhost:8086/actuator/info
- **Metrics**: http://localhost:8086/actuator/metrics (solo dev)

## Licencia

Proyecto desarrollado para InnovaTech - Todos los derechos reservados.

## Integraci�n con BFF
- Ruta base consumida: /api/v1/consultorias
- Healthcheck: /actuator/health


