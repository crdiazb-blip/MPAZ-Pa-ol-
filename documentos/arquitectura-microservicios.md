# Diagrama de arquitectura de microservicios

MPAZ Pañol: la app Android consume una sola puerta de entrada (`api-gateway`). Cada microservicio es un proyecto
Spring Boot independiente, con su propio `Dockerfile` y su propio esquema en la base de datos PostgreSQL de Supabase.

```mermaid
flowchart LR
    APP["App Android<br/>(Retrofit)"]

    subgraph DOCKER["Docker Compose (un contenedor por servicio)"]
        GW["api-gateway<br/>:8080 (único puerto público)"]
        AUTH["auth-service<br/>:8081"]
        INV["inventory-service<br/>:8082"]
        REQ["request-service<br/>:8083"]
        AUD["audit-service<br/>:8084"]
        NOT["notification-service<br/>:8085"]
    end

    subgraph SUPA["Supabase (PostgreSQL en la nube)"]
        S1[("panol_auth")]
        S2[("panol_inventario")]
        S3[("panol_solicitudes")]
        S4[("panol_auditoria")]
        S5[("panol_notificaciones")]
    end

    APP -- "HTTP + token JWT" --> GW
    GW -- "valida el token" --> AUTH
    GW --> INV
    GW --> REQ
    GW --> AUD
    GW --> NOT

    REQ -- "reserva / libera stock" --> INV
    REQ -- "registra hitos" --> AUD
    REQ -- "crea notificaciones" --> NOT

    AUTH --- S1
    INV --- S2
    REQ --- S3
    AUD --- S4
    NOT --- S5
```

## Responsabilidad de cada servicio

| Servicio | Puerto | Responsabilidad | Esquema |
|---|---|---|---|
| `api-gateway` | 8080 | Punto de entrada. Valida el token en cada petición, agrega la identidad del usuario (`X-User-*`) y enruta. Nunca expone `/internal`. | (sin base de datos) |
| `auth-service` | 8081 | Usuarios, perfiles ADMIN / SOLICITANTE, login, logout (tokens revocados) y emisión de JWT. | `panol_auth` |
| `inventory-service` | 8082 | Catálogo, stock total y disponible, estado y ubicación de los equipos; reserva y libera stock con bloqueo para evitar sobreventa. | `panol_inventario` |
| `request-service` | 8083 | Solicitudes de préstamo, regla de 3 horas de anticipación, flujo de estados y justificación de rechazos. | `panol_solicitudes` |
| `audit-service` | 8084 | Bitácora de trazabilidad: Solicitado → Aprobado/Rechazado → Entregado → Devuelto. | `panol_auditoria` |
| `notification-service` | 8085 | Notificaciones internas (simuladas) y recordatorios programados de devolución. | `panol_notificaciones` |

## Autenticación y cierre de sesión

```mermaid
sequenceDiagram
    participant A as App Android
    participant G as api-gateway
    participant U as auth-service
    participant S as Otro microservicio

    A->>G: POST /api/auth/login (RUT, contraseña)
    G->>U: POST /auth/login
    U-->>A: token JWT + perfil (rol)
    Note over A: Guarda sesión y usuario en Room (SQLite)

    A->>G: GET /api/equipos (Authorization: Bearer token)
    G->>U: GET /auth/validate
    U-->>G: usuario y rol
    G->>S: GET /equipos (X-User-Rut, X-User-Rol, ...)
    S-->>A: respuesta

    A->>G: POST /api/auth/logout
    G->>U: POST /auth/logout (el token queda revocado)
    A->>G: GET /api/equipos (mismo token)
    G->>U: GET /auth/validate
    U-->>A: 401 sesión cerrada
```

## Flujo de una solicitud de préstamo

```mermaid
stateDiagram-v2
    [*] --> PENDIENTE: Solicitante envía (≥ 3 horas de anticipación)
    PENDIENTE --> ACEPTADA: Admin acepta (descuenta stock)
    PENDIENTE --> RECHAZADA: Admin rechaza (justificación obligatoria)
    ACEPTADA --> ENTREGADA: Admin registra la entrega
    ENTREGADA --> DEVUELTA: Admin registra la devolución (repone stock)
    DEVUELTA --> FINALIZADA: Admin cierra el ticket
    RECHAZADA --> [*]
    FINALIZADA --> [*]
```

Cada cambio de estado deja un registro en `audit-service` y genera una notificación para el solicitante.
