# MPAZ Pañol

Sistema móvil de inventario, reserva y préstamo de equipos tecnológicos de la Escuela Escritora Marcela Paz.
Asignatura DSY1105 - Desarrollo de Aplicaciones Móviles (Experiencia 3).

- **App Android** (esta carpeta): Kotlin, Jetpack Compose, Material Design 3, MVVM, Room, Retrofit.
- **Backend** ([`backend/`](backend/README.md)): 6 microservicios Spring Boot, PostgreSQL en Supabase, un contenedor Docker por servicio.

**Integrantes:** crdiazb-blip y francisscaorregoo-gif _(completar nombres completos)_

Todos los datos son ficticios (Ley 19.628 y Ley 21.719).

## Perfiles de usuario

| Perfil | Cuenta de prueba | Qué puede hacer |
|---|---|---|
| **Solicitante** (Docente, Inspector, UTP, Directivo) | `12.345.678-5` / `1234` | Ver el catálogo con filtros, armar el carrito, enviar solicitudes (regla de 3 horas de anticipación), seguir su estado, leer justificaciones de rechazo y notificaciones. |
| **Administrador** (Encargado TI) | `11.111.111-1` / `admin123` | Gestionar el inventario (alta, edición, estado, stock, ubicación, baja), aceptar o rechazar solicitudes con justificación obligatoria, registrar entrega y devolución, ver la bitácora. |

Cada perfil tiene su propia navegación: si una pantalla no corresponde al perfil de la sesión, se vuelve al login.

## Funcionalidades

- **Login y logout** contra el microservicio de autenticación (JWT). La sesión y el usuario se guardan en SQLite (Room): al reabrir la app la sesión se restaura, y quien ya inició sesión puede volver a entrar sin conexión (la clave se guarda solo como hash con sal).
- Formularios validados por campo, con ícono y mensaje de error (login, solicitud, alta y edición de equipos, rechazo).
- Catálogo y solicitudes guardados en Room: la app sigue mostrando los últimos datos descargados sin conexión.
- Recursos nativos por perfil: **Solicitante** → vibración y nivel de batería. **Administrador** → linterna y ubicación GPS.
- Animaciones: cambio de color por estado, expansión de tarjetas, aparición de mensajes de error, indicadores de carga.
- Pruebas unitarias: JUnit en la app (validaciones, hash, mapeos) y JUnit + Mockito en cada microservicio.

## Arquitectura

```
App Android (MVVM) ──Retrofit──▶ api-gateway :8080 ──▶ auth-service         (usuarios, login/logout, JWT)
                                                   ├─▶ inventory-service    (equipos, stock, estado)
                                                   ├─▶ request-service      (solicitudes, regla de 3 horas)
                                                   ├─▶ audit-service        (bitácora de trazabilidad)
                                                   └─▶ notification-service (recordatorios de devolución)
                                                        │
                                                        └──▶ PostgreSQL (Supabase)
```

Estructura de la app:

```
com.example.mpazpaolr
├── data
│   ├── database/    AppDatabase, Converters (Room)
│   ├── local/       dao/ (PanolDao, AuthDao), entity/ (equipos, solicitudes, usuarios, sesión)
│   ├── remote/      api/ (interfaces Retrofit), dto/, RetrofitClient, ApiResult, Mappers
│   ├── repository/  Auth, Panol, Inventario y GestionSolicitudes (única fuente de datos)
│   ├── security/    PasswordHasher
│   └── device/      Vibracion, Bateria, Linterna, Ubicacion (recursos nativos)
├── model/           Enums, Rol, Perfil, estados de formularios y Validaciones (lógica pura)
├── ui
│   ├── components/  Header, Footer, CampoFormulario, selectores
│   ├── navigation/  AppNavigation (rutas por perfil)
│   ├── screen/      Login, Principal, Carrito, Solicitud, MisSolicitudes, Admin*
│   ├── theme/
│   └── viewmodel/   Login, Panol, Solicitud, Inventario y GestionSolicitudes
└── MainActivity.kt
```

## Cómo ejecutar

### 1. Backend

```bash
cd backend
cp .env.example .env        # completar con los datos de Supabase y el JWT_SECRET
docker compose up -d --build
```

Detalles de Supabase, endpoints y pruebas en [`backend/README.md`](backend/README.md).

### 2. App Android

1. Abrir la carpeta en Android Studio (JDK 17 o superior).
2. En el emulador, la app usa `http://10.0.2.2:8080/` (el `localhost` de tu PC). Para un teléfono físico o un
   servidor en la nube, compilar con otra URL: `./gradlew assembleDebug -PAPI_BASE_URL=http://IP-DEL-SERVIDOR:8080/`
3. Ejecutar en un emulador o dispositivo Android (minSdk 23).
4. Tests: `./gradlew testDebugUnitTest`

> El tráfico HTTP sin cifrar (`usesCleartextTraffic`) está habilitado solo para desarrollo.
> En producción el gateway debe publicarse con HTTPS.

## Reparto del trabajo

El historial de Git muestra los aportes de cada integrante. Resumen:

| Integrante | Backend | Android |
|---|---|---|
| crdiazb-blip | estructura y Docker, inventory-service, request-service, api-gateway | autenticación con Room, navegación por rol, gestión de inventario |
| francisscaorregoo-gif | auth-service, audit-service, notification-service | capa Retrofit, sincronización con Room, consola de solicitudes del administrador |
