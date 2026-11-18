# MPAZ Pañol - Backend (microservicios Spring Boot)

Backend REST del sistema de inventario, reserva y préstamo de equipos de la Escuela Escritora Marcela Paz.
Cada microservicio es un proyecto Maven independiente con su propio `Dockerfile`.
La base de datos es **PostgreSQL en Supabase** (nube). Todos los datos son ficticios.

## Microservicios

| Servicio | Puerto | Responsabilidad |
|---|---|---|
| `api-gateway` | 8080 (público) | Punto de entrada único. Valida el token y enruta. |
| `auth-service` | 8081 | Usuarios, perfiles (ADMIN / SOLICITANTE), login y logout (JWT). |
| `inventory-service` | 8082 | Catálogo de equipos, stock, estado y ubicación. |
| `request-service` | 8083 | Solicitudes de préstamo, regla de 3 horas y flujo de estados. |
| `audit-service` | 8084 | Bitácora de trazabilidad de cada solicitud. |
| `notification-service` | 8085 | Recordatorios internos de devolución (simulados). |

Los servicios internos no publican puertos: solo se llega a ellos a través del gateway.

## Configuración

1. Copiar `.env.example` como `.env` y completar los datos de Supabase y el `JWT_SECRET`.
2. El archivo `.env` está en `.gitignore`. **Nunca subir credenciales a GitHub.**
3. En Supabase usar la cadena de conexión **Session pooler** (puerto 5432). Cada servicio crea su propio
   esquema (`panol_auth`, `panol_inventario`, `panol_solicitudes`, `panol_auditoria`, `panol_notificaciones`)
   y sus tablas al iniciar.

## Ejecución

```bash
docker compose up -d --build
```

Para probar sin Supabase (Postgres local en un contenedor), usar en `.env` los valores de la sección
"Desarrollo local" y ejecutar:

```bash
docker compose --profile local up -d --build
```

## Pruebas unitarias (JUnit)

Cada servicio incluye pruebas JUnit 5 + Mockito. Sin instalar Maven, usando Docker:

```bash
docker run --rm -v "$PWD/auth-service":/app -w /app maven:3.9-eclipse-temurin-21 mvn -B test
```
