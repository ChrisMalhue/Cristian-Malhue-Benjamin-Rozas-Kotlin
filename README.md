# Documentación Técnica: Sistema de Gestión Académica y Reserva de Salas (Android)

Aplicación móvil nativa desarrollada en **Kotlin** para la gestión integral de recursos académicos, reserva de salas de estudio/clases, administración de documentos y control de inspectoría.

---

## 1. Arquitectura y Stack Tecnológico

El proyecto sigue una arquitectura centrada en actividades nativas de Android con comunicación cliente-servidor mediante servicios RESTful.

* **Lenguaje:** Kotlin
* **Interfaz de Usuario:** Jetpack Compose / Material Design (`ui.theme`) y Vistas tradicionales basadas en `Activity`.
* **Sistema de Construcción:** Gradle con Kotlin DSL (`build.gradle.kts` y catálogo de versiones `libs.versions.toml`).
* **Consumo de API / Red:** Retrofit (`RetrofitClient.kt`, `DocumentosApi.kt`, `UsuariosApi.kt`).
* **Pruebas:** JUnit 4 para pruebas unitarias (`RegistroValidacionTest.kt`) y AndroidX Test para pruebas instrumentadas.

---

## 2. Estructura del Proyecto

La base de código principal reside en `app/src/main/java/com/example/cristian_malhue_benjamin_rozas_kotlin/` y se organiza de la siguiente manera:

```text
├── app/
│   ├── release/
│   │   ├── app-release.apk              # Compilación de producción firmada (~45.9 MB)
│   │   └── output-metadata.json         # Metadatos de la compilación release
│   └── src/
│       ├── main/
│       │   ├── AndroidManifest.xml      # Configuración de componentes y permisos
│       │   ├── java/.../
│       │   │   ├── model/               # Entidades y modelos de datos del dominio
│       │   │   ├── network/             # Configuración de clientes HTTP e interfaces API
│       │   │   ├── ui/theme/            # Definición de paleta de colores, tipografía y temas
│       │   │   └── *Activity.kt         # Controladores de pantalla y lógica de presentación
│       │   └── res/                     # Recursos gráficos, iconos mipmap y valores XML
│       ├── test/                        # Pruebas unitarias locales (JVM)
│       └── androidTest/                 # Pruebas instrumentadas en dispositivo/emulador
├── build.gradle.kts                     # Configuración global de Gradle
└── settings.gradle.kts                  # Definición de módulos y repositorios
```

---

## 3. Módulos y Componentes Principales

### 3.1. Capa de Presentación (`Activities`)
Cada pantalla de la aplicación está encapsulada en su propia `Activity` encargada de gestionar el ciclo de vida y la interacción con el usuario:

* **`MainActivity.kt`:** Punto de entrada principal de la aplicación y enrutamiento inicial.
* **`LoginActivity.kt`:** Autenticación de usuarios y validación de credenciales.
* **`RegistroActivity.kt`:** Registro de nuevos usuarios en el sistema con validación de formularios.
* **`InicioActivity.kt`:** Panel principal (Dashboard) de navegación hacia los distintos módulos según el rol del usuario.
* **`ReservarSalaActivity.kt`:** Gestión de disponibilidad y solicitud de reserva de salas por bloque horario.
* **`DocumentosActivity.kt`:** Visualización, descarga o gestión de documentación académica/administrativa.
* **`InspectoriaActivity.kt`:** Módulo administrativo de supervisión, control de estados de reserva y gestión operativa.
* **`EditarPerfilActivity.kt`:** Actualización de datos personales y credenciales de la cuenta activa.

### 3.2. Capa de Modelo (`model/`)
Define las estructuras de datos que representan las entidades del negocio y mapean las respuestas JSON del backend:

* **`Usuario.kt` & `Rol.kt`:** Representan la información del usuario autenticado y sus permisos/roles dentro de la plataforma.
* **`Sala.kt` & `AreaSala.kt`:** Modelan los espacios físicos disponibles y su categorización por área.
* **`Reserva.kt`, `BloqueHorario.kt` & `EstadoReserva.kt`:** Gestionan la lógica temporal de las reservas, franjas horarias y su ciclo de vida (ej. pendiente, aprobada, rechazada).
* **`Documento.kt`:** Estructura los metadatos y enlaces asociados a los archivos gestionados en `DocumentosActivity`.

### 3.3. Capa de Red (`network/`)
Gestiona la comunicación asíncrona con el servidor backend:

* **`RetrofitClient.kt`:** Instancia singleton encargada de configurar la URL base, los conversores de serialización (JSON) y el cliente HTTP.
* **`UsuariosApi.kt`:** Define los endpoints REST para inicio de sesión, registro y edición de perfiles.
* **`DocumentosApi.kt`:** Define los endpoints para la consulta y transferencia de documentos.

---

## 4. Pruebas Automatizadas

El proyecto incluye suites de verificación en dos niveles:

1. **Pruebas Unitarias (`src/test/`):**
   * `RegistroValidacionTest.kt`: Valida las reglas de negocio y formato de entrada durante el proceso de registro de usuarios sin requerir el framework de Android.
   * `ExampleUnitTest.kt`: Plantilla base de pruebas unitarias.
2. **Pruebas Instrumentadas (`src/androidTest/`):**
   * `ExampleInstrumentedTest.kt`: Verifica el contexto de ejecución de la aplicación directamente sobre un entorno Android.

---

## 5. Requisitos y Despliegue

### Requisitos del Entorno de Desarrollo
* **IDE:** Android Studio (Ladybug o superior recomendado).
* **JDK:** Java 17 o superior (compatible con Gradle Daemon configurado en `gradle/gradle-daemon-jvm.properties`).
* **Android SDK:** Configurado según `gradle/libs.versions.toml`.

### Generación de Artefactos de Distribución
* **APK de Producción:** El proyecto ya incluye una versión compilada en `app/release/app-release.apk`.
* **Android App Bundle (`.aab`):** Para generar el paquete de distribución para Google Play Store:
  1. Abrir el proyecto en Android Studio.
  2. Navegar a **Build > Generate Signed Bundle / APK...**
  3. Seleccionar **Android App Bundle**, asociar un archivo Keystore (`.jks`) y compilar la variante `release`.
