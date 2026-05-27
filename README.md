# innovatech-ms-consultorias

## Estado de evidencia

| Categoria | Estado |
|---|---|
| Implementado | CRUD de consultorias, productor RabbitMQ, Actuator |
| Configurado | MySQL/H2, RabbitMQ, perfiles, Docker |
| Validado | compilacion |
| Pendiente de validacion runtime | publicacion real del evento y stack completo |
| No evidenciado | consumidor oficial de `Consultoria_Solicitada` |

## 1. Descripcion general
`innovatech-ms-consultorias` es el microservicio encargado de registrar solicitudes de consultoria, consultar historiales por usuario y actualizar estados de atencion dentro de InnovaTech.

## 2. Rol dentro de la arquitectura
El servicio se consume mediante el API Gateway en la ruta `/api/v1/consultorias/**`. Persiste su informacion en base de datos propia y publica un evento RabbitMQ cuando una consultoria se registra correctamente.

Flujo simple:

`Cliente/Frontend -> API Gateway -> innovatech-ms-consultorias -> Base de datos / RabbitMQ`

Relaciones evidenciadas:

- API Gateway enruta a este servicio en `http://consultorias:8086` en Docker.
- BFF consulta este servicio mediante `MS_CONSULTORIAS_URL`.
- El servicio usa H2 en desarrollo y MySQL en produccion.
- El servicio publica mensajes RabbitMQ mediante `RabbitTemplate`.

## 3. Stack tecnico
- Java 21
- Spring Boot 3.5.14
- Maven Wrapper
- Spring Web
- Spring Data JPA
- Spring Security
- JWT
- Spring AMQP / RabbitMQ
- H2 Database
- MySQL Driver
- Spring Boot Actuator
- Springdoc OpenAPI
- Docker

## 4. Puerto del servicio

| Concepto | Valor |
|---|---|
| Puerto esperado | 8086 |
| Puerto configurado | `${SERVER_PORT:8086}` |
| Archivo donde se define | `src/main/resources/application.properties`, `src/main/resources/application-prod.properties`, `Dockerfile` |
| Variable de entorno asociada | `SERVER_PORT` |

## 5. Variables de entorno

| Variable | Descripcion | Valor por defecto | Obligatoria | Riesgo/observacion |
|---|---|---|---|---|
| `SERVER_PORT` | Puerto HTTP del microservicio | `8086` | No | Debe mantenerse alineado con Gateway y Docker. |
| `SPRING_PROFILES_ACTIVE` | Perfil activo de Spring | `dev` | No | En `prod` cambian datasource, Swagger y reglas operativas. |
| `JWT_SECRET` | Secreto para validacion JWT | Sin valor por defecto | Si en entornos no locales | No debe versionarse. |
| `APP_SECURITY_DOCS_PUBLIC` | Habilita acceso publico a Swagger/OpenAPI | `true` en dev, `false` en prod | No | En produccion debe mantenerse controlado. |
| `CONSULTORIAS_MYSQL_HOST` | Host MySQL en produccion | `mysql-consultorias` | Si en prod | No aplica en desarrollo H2. |
| `CONSULTORIAS_MYSQL_PORT` | Puerto MySQL en produccion | `3306` | Si en prod | Debe coincidir con la infraestructura. |
| `CONSULTORIAS_MYSQL_DATABASE` | Base de datos MySQL del servicio | `consultorias_db` | Si en prod | Se evidencia estrategia de base dedicada. |
| `CONSULTORIAS_MYSQL_USERNAME` | Usuario de aplicacion MySQL | Sin valor por defecto | Si en prod | No incluir credenciales reales. |
| `CONSULTORIAS_MYSQL_PASSWORD` | Password de aplicacion MySQL | Sin valor por defecto | Si en prod | No incluir credenciales reales. |
| `RABBITMQ_HOST` | Host RabbitMQ | `localhost` en dev | Si cuando RabbitMQ esta integrado | En prod se define por entorno. |
| `RABBITMQ_PORT` | Puerto RabbitMQ | `5672` | No | Debe coincidir con la infraestructura. |
| `RABBITMQ_USERNAME` | Usuario RabbitMQ | `rabbit_local_user` en dev | Si en prod | No usar `guest` en produccion. |
| `RABBITMQ_PASSWORD` | Password RabbitMQ | `rabbit_local_password` en dev | Si en prod | No incluir secretos reales. |

## 6. Base de datos
El servicio usa H2 en desarrollo y MySQL en produccion. Se evidencia una entidad principal y un repositorio dedicado.

| Elemento | Valor |
|---|---|
| Motor | H2 en dev, MySQL en prod |
| Base de datos | `consultorias_db` |
| Entidades | `Consultoria` |
| Repositories | `ConsultoriaRepository` |
| ddl-auto | `update` en dev, `validate` en prod |
| show-sql | `true` en dev, `false` en prod |

Riesgos o pendientes:

- En produccion el esquema debe existir previamente porque el servicio usa `validate`.
- La consola H2 solo se evidencia en desarrollo.

## 7. Endpoints principales

| Metodo | Endpoint | Descripcion | Auth requerida | Request | Response |
|---|---|---|---|---|---|
| `POST` | `/api/v1/consultorias` | Registra una solicitud de consultoria | Si | `ConsultoriaRequestDTO` | `ConsultoriaResponseDTO` |
| `GET` | `/api/v1/consultorias/{id}` | Obtiene una consultoria por identificador | Si | No aplica | `ConsultoriaResponseDTO` |
| `GET` | `/api/v1/consultorias/usuario/{usuarioId}` | Lista consultorias por usuario | Si | No aplica | `List<ConsultoriaResponseDTO>` |
| `GET` | `/api/v1/consultorias/usuario/{usuarioId}/estado/{estado}` | Filtra consultorias por usuario y estado | Si | No aplica | `List<ConsultoriaResponseDTO>` |
| `PATCH` | `/api/v1/consultorias/{id}/estado/{estado}` | Actualiza el estado de una consultoria | Si | No evidenciado en body | `ConsultoriaResponseDTO` |

## 8. Seguridad
- Usa Spring Security: si.
- Valida JWT: si.
- Depende del Gateway: el flujo oficial es via Gateway, aunque el servicio tambien protege sus endpoints.
- Endpoints publicos: `GET /actuator/health`, `GET /actuator/info`, Swagger/OpenAPI solo cuando `APP_SECURITY_DOCS_PUBLIC=true`.
- Endpoints protegidos: los endpoints `/api/v1/consultorias/**`.
- Riesgos detectados:
  - Si se expone directamente fuera de la red interna, el servicio seguiria recibiendo trafico sin pasar por el Gateway.
  - La apertura de documentacion depende de perfil y variable de entorno.

## 9. Integraciones

| Origen | Destino | Tipo | URL/variable | Estado |
|---|---|---|---|---|
| API Gateway | `innovatech-ms-consultorias` | HTTP | `http://consultorias:8086` en Docker | Evidenciado |
| BFF | `innovatech-ms-consultorias` | HTTP | `MS_CONSULTORIAS_URL` | Evidenciado |
| `innovatech-ms-consultorias` | Base de datos propia | JDBC/JPA | H2 dev / `CONSULTORIAS_MYSQL_*` prod | Evidenciado |
| `innovatech-ms-consultorias` | RabbitMQ | AMQP | `RABBITMQ_HOST`, `RABBITMQ_PORT`, `RABBITMQ_USERNAME`, `RABBITMQ_PASSWORD` | Evidenciado |

## 10. Eventos RabbitMQ

| Evento | Exchange | Routing key | Queue | Productor/Consumidor | Estado |
|---|---|---|---|---|---|
| `Consultoria_Solicitada` | `consultoria.exchange` | `consultoria.routingkey` | `consultoria.queue` | Productor | Evidenciado |

No se evidencia consumidor oficial para este evento dentro de este repositorio.

## 11. Ejecucion local

```bash
./mvnw clean package
./mvnw spring-boot:run
```

Perfil de produccion:

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=prod
```

Puntos utiles:

- Swagger UI: `http://localhost:8086/swagger-ui.html` cuando la documentacion publica esta habilitada.
- OpenAPI: `http://localhost:8086/api-docs`
- Health: `http://localhost:8086/actuator/health`
- Info: `http://localhost:8086/actuator/info`
