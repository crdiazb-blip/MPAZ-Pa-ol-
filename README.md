# MPAZ Pañol

App móvil Android (MVP académico) para consultar el catálogo de equipos tecnológicos de la Escuela Escritora Marcela Paz, armar un carrito y enviar solicitudes de reserva. Asignatura DSY1105 - Desarrollo de Aplicaciones Móviles (Evaluación Parcial 2).

**Integrantes:** _(completar nombres de la pareja)_

## Alcance de esta versión

- Un solo perfil de usuario: **Solicitante** (docente). No hay registro ni base de datos de usuarios.
  Credenciales de demostración: `docente@mpaz.cl` / `1234`.
- Todos los datos son ficticios.

## Funcionalidades

- Login con validación por campo (íconos y mensajes de error).
- Catálogo con filtros por categoría y "solo disponibles".
- Carrito de solicitudes (sumar, restar, quitar).
- Formulario de solicitud: sala, fecha, hora de inicio y hora de devolución. Valida formato, rango horario y la regla de **3 horas de anticipación**.
- Seguimiento de solicitudes con estado y justificación de rechazo.
- Persistencia local con Room (SQLite): equipos y solicitudes.
- Recursos nativos: **vibración** (confirmaciones y errores) y **nivel de batería**.
- Animaciones: cambio de color por estado, expansión de tarjetas, aparición de mensajes de error.

## Arquitectura (MVVM)

```
com.example.mpazpaolr
├── data
│   ├── database/   AppDatabase, Converters
│   ├── local/      dao/PanolDao, entity/*
│   ├── repository/ PanolRepository, AuthRepository
│   └── device/     Vibracion, Bateria (recursos nativos)
├── model/          Enums, estados de formulario y Validaciones (lógica pura)
├── ui
│   ├── components/ Header, Footer, CampoFormulario
│   ├── navigation/ AppNavigation
│   ├── screen/     Login, Principal, Carrito, Solicitud, MisSolicitudes
│   ├── theme/
│   └── viewmodel/  LoginViewModel, PanolViewModel, SolicitudViewModel
└── MainActivity.kt
```

Las validaciones viven en `model/Validaciones.kt` y se prueban con tests unitarios (`ValidacionesTest`).

## Cómo ejecutar

1. Abrir la carpeta en Android Studio (JDK 17 o superior).
2. Sincronizar Gradle y ejecutar en un emulador o dispositivo Android (minSdk 23).
3. Tests: `./gradlew testDebugUnitTest`

## Stack

Kotlin, Jetpack Compose, Material Design 3, MVVM, Room, Navigation Compose.
