# Diagrama de arquitectura del proyecto Android

MPAZ Pañol usa el patrón **MVVM** con una capa de datos de dos fuentes: la base de datos local (Room / SQLite) y los
microservicios (Retrofit). La interfaz siempre lee desde Room, por lo que la app sigue mostrando datos sin conexión.

```mermaid
flowchart TB
    subgraph VIEW["View · Jetpack Compose + Material Design 3"]
        L["Login"]
        subgraph SOL["Perfil Solicitante"]
            P["Principal (catálogo)"]
            C["Carrito"]
            S["Solicitud"]
            M["MisSolicitudes"]
        end
        subgraph ADM["Perfil Administrador"]
            AH["AdminHome"]
            AI["AdminInventario"]
            AF["AdminEquipoForm"]
            AS["AdminSolicitudes"]
        end
        NAV["AppNavigation<br/>(rutas protegidas por rol)"]
    end

    subgraph VM["ViewModel · estado con State / StateFlow"]
        LVM["LoginViewModel"]
        PVM["PanolViewModel"]
        SVM["SolicitudViewModel"]
        IVM["InventarioViewModel"]
        GVM["GestionSolicitudesViewModel"]
    end

    subgraph REPO["Repository · única fuente de datos"]
        AR["AuthRepository"]
        PR["PanolRepository"]
        IR["InventarioRepository"]
        GR["GestionSolicitudesRepository"]
    end

    subgraph LOCAL["Datos locales · Room (SQLite)"]
        DB[("AppDatabase<br/>equipos · solicitudes · items<br/>usuarios_locales · sesion")]
    end

    subgraph REMOTE["Datos remotos · Retrofit"]
        RC["RetrofitClient<br/>+ interceptor de token"]
    end

    API["api-gateway<br/>(microservicios Spring Boot)"]
    DEV["Recursos nativos<br/>vibración · batería · linterna · GPS"]
    MODEL["Model<br/>enums · Rol · Perfil · Validaciones"]

    L --> LVM
    P --> PVM
    C --> PVM
    M --> PVM
    S --> SVM
    AI --> IVM
    AF --> IVM
    AS --> GVM

    LVM --> AR
    PVM --> PR
    SVM --> PR
    IVM --> IR
    GVM --> GR
    GR --> PR

    AR --> DB
    PR --> DB
    IR --> DB
    AR --> RC
    PR --> RC
    IR --> RC
    GR --> RC
    RC --> API

    VM -.usa.-> MODEL
    P --> DEV
    AH --> DEV
    NAV --- VIEW
```

## Responsabilidades

| Capa | Paquete | Responsabilidad |
|---|---|---|
| Model | `model/` | Enums (`Categoria`, `EstadoEquipo`, `EstadoSolicitud`, `Rol`), estados de formularios y `Validaciones` (lógica pura, con pruebas unitarias). |
| View | `ui/screen`, `ui/components` | Composables sin lógica de negocio: muestran el estado y notifican eventos al ViewModel. |
| ViewModel | `ui/viewmodel` | Estado de cada pantalla (`mutableStateOf`, `StateFlow`), validación de formularios y llamadas al repositorio. |
| Repository | `data/repository` | Decide de dónde salen los datos: descarga del servidor y actualiza Room; la UI observa Room. |
| Local | `data/local`, `data/database` | Entidades, DAO y base de datos Room. Guarda el catálogo, las solicitudes, el usuario y la sesión. |
| Remote | `data/remote` | Interfaces Retrofit, DTOs, `ApiResult` (éxito, error o sin conexión) y mapeo a entidades. |
| Device | `data/device` | Recursos nativos: vibración y batería (Solicitante); linterna y ubicación GPS (Administrador). |

## Navegación por perfil

```mermaid
flowchart LR
    ST["Cargando<br/>(restaura la sesión de Room)"] -->|sin sesión| LG["Login"]
    ST -->|rol SOLICITANTE| P["Catálogo"]
    ST -->|rol ADMIN| AH["Panel administrador"]
    LG -->|rol SOLICITANTE| P
    LG -->|rol ADMIN| AH

    P --> C["Carrito"] --> S["Nueva solicitud"] --> M["Mis solicitudes"]
    P --> M

    AH --> AI["Inventario"] --> AF["Formulario de equipo"]
    AH --> AS["Solicitudes (consola)"]

    P -->|Salir| LG
    AH -->|Salir| LG
```

Cada pantalla se envuelve en `SoloPara(rol)`: si la sesión activa no tiene el rol requerido, se vuelve al login.
