# ms_categoria · Microservicio de Categorías

Microservicio REST desarrollado para la asignatura de DevOps. Administra el catálogo de categorías de libros de la Biblioteca Online y se registra en Eureka para que los demás microservicios del sistema lo puedan descubrir. El proyecto incluye contenedorización con Docker, orquestación con Docker Compose y un pipeline de integración continua en GitHub Actions que compila, prueba y publica la imagen en GitHub Container Registry.

## Integrantes

- Abel Aylas
- Manuel Mora

## Contenido

- [De qué trata](#de-qué-trata)
- [Arquitectura](#arquitectura)
- [Cómo levantar el proyecto](#cómo-levantar-el-proyecto)
- [Cómo usar la API](#cómo-usar-la-api)
- [Documentación Swagger](#documentación-swagger)
- [Pruebas](#pruebas)
- [Docker](#docker)
- [Pipeline CI/CD](#pipeline-cicd)
- [Estrategia de ramificación (GitFlow)](#estrategia-de-ramificación-gitflow)
- [Versionado con tags](#versionado-con-tags)
- [Tecnologías usadas](#tecnologías-usadas)
- [Uso de inteligencia artificial](#uso-de-inteligencia-artificial)

## De qué trata

Cada libro de la biblioteca pertenece a una categoría según su estado o formato. Este microservicio permite crear, consultar, actualizar y eliminar esas categorías. Al arrancar, si la base de datos está vacía, carga cuatro categorías por defecto: **Nuevo**, **Usado**, **Reparado** y **Digital**.

Corre en el puerto **8082** y usa MySQL como base de datos (H2 en memoria para las pruebas).

## Arquitectura

El código sigue una arquitectura por capas: `Controller → Service → Repository → MySQL`, con un manejador global de errores que unifica el formato de las respuestas de fallo.

```
ms_categoria/
├── src/main/java/cl/syst3m64/categorias/
│   ├── CategoriasApplication.java   # Clase principal
│   ├── config/                      # Swagger y carga inicial de datos
│   ├── controller/                  # Endpoints REST
│   ├── exception/                   # Manejo global de errores
│   ├── model/                       # Entidad Categoria
│   ├── repository/                  # Acceso a datos (Spring Data JPA)
│   └── service/                     # Lógica de negocio
├── src/test/                        # Pruebas unitarias
├── Dockerfile
├── docker-compose.yml
└── .env.example                     # Plantilla de variables de entorno
```

## Cómo levantar el proyecto

### Con Docker Compose (recomendado)

Solo se necesita tener Docker Desktop instalado.

```bash
git clone https://github.com/SYST3M644/devops_007d_ols.git
cd devops_007d_ols/ms_categoria
cp .env.example .env
docker compose up --build
```

La primera vez demora unos minutos porque construye la imagen y levanta MySQL. El microservicio espera a que MySQL esté sano antes de arrancar. Cuando termine, la API queda disponible en http://localhost:8082/api/category

Para detenerlo:

```bash
docker compose down
```

Para empezar de cero borrando la base de datos:

```bash
docker compose down -v
```

### Variables de entorno

Las credenciales no están escritas en el código: se leen desde el archivo `.env`, que no se sube al repositorio. El archivo `.env.example` sirve de plantilla.

| Variable | Descripción | Valor de ejemplo |
|---|---|---|
| `MYSQL_ROOT_PASSWORD` | Contraseña del usuario root de MySQL | `cambiar_root` |
| `MYSQL_DATABASE` | Nombre de la base de datos | `bd_categorias` |
| `MYSQL_USER` | Usuario con el que se conecta el microservicio | `categorias` |
| `MYSQL_PASSWORD` | Contraseña de ese usuario | `cambiar_password` |
| `EUREKA_CLIENT_ENABLED` | Registro en Eureka (`false` si se levanta solo este servicio) | `false` |

> Si ya habías levantado el proyecto antes de usar `.env`, ejecuta `docker compose down -v` una vez para que MySQL cree el usuario nuevo.

### Con la imagen publicada

El pipeline publica la imagen en GitHub Container Registry en cada push a `develop`:

```bash
docker pull ghcr.io/syst3m644/devops_007d_ols:latest
```

### Sin Docker

Requiere Java 17 y un MySQL local con la base `bd_categorias` creada.

```bash
cd ms_categoria
export DB_USER=root DB_PASSWORD=tu_password EUREKA_CLIENT_ENABLED=false
./mvnw spring-boot:run
```

`DB_HOST`, `DB_PORT` y `DB_NAME` son opcionales y por defecto apuntan a `localhost:3306/bd_categorias`.

## Cómo usar la API

Ruta base: `http://localhost:8082/api/category`

| Método | Ruta | Descripción | Respuestas |
|---|---|---|---|
| GET | `/api/category` | Lista todas las categorías | 200 |
| GET | `/api/category/{id}` | Busca una categoría por ID | 200, 404 |
| GET | `/api/category/nombres?nombre=Nuevo` | Busca categorías por nombre | 200 |
| POST | `/api/category` | Crea una categoría | 201, 400 |
| PUT | `/api/category/{id}` | Actualiza una categoría | 200, 400, 404 |
| DELETE | `/api/category/{id}` | Elimina una categoría | 200, 404 |

Ejemplo de body para POST y PUT:

```json
{
  "nombre": "Ciencia Ficción",
  "descripcion": "Libros de ciencia ficción y tecnología"
}
```

Reglas de validación:

| Campo | Regla |
|---|---|
| `id` | Lo genera la base de datos. Si se envía en el body, la API responde 400 |
| `nombre` | Obligatorio, único, máximo 100 caracteres |
| `descripcion` | Opcional, máximo 300 caracteres |

Los errores de validación devuelven un JSON con el detalle por campo:

```json
{
  "timestamp": "2026-10-24T12:00:00",
  "status": 400,
  "error": "Validation Failed",
  "errors": { "nombre": "El nombre es obligatorio" }
}
```

## Documentación Swagger

Con el servicio levantado:

- Swagger UI: http://localhost:8082/swagger-ui.html
- OpenAPI JSON: http://localhost:8082/v3/api-docs

## Pruebas

El proyecto tiene **32 pruebas unitarias** escritas con JUnit 5 y Mockito. Las pruebas usan el perfil `test` con H2 en memoria, así que no necesitan MySQL ni el archivo `.env`.

```bash
cd ms_categoria
./mvnw test
```

| Clase | Qué prueba | Tests |
|---|---|---|
| `CategoriaControllerTest` | Endpoints REST y códigos HTTP (`@WebMvcTest`) | 13 |
| `CategoriaServiceTest` | Lógica de negocio con el repositorio simulado | 10 |
| `CategoriaRepositoryTest` | Consultas JPA contra H2 (`@DataJpaTest`) | 5 |
| `DataInitializerTest` | Carga inicial de categorías | 2 |
| `CategoriasApplicationMainTest` | Arranque de la aplicación | 1 |
| `EspeciesApplicationTests` | Carga del contexto de Spring | 1 |
| **Total** | | **32** |

El proyecto genera dos reportes HTML:

| Reporte | Qué muestra | Comando local | Archivo |
|---|---|---|---|
| Surefire | Resultado de cada prueba: si pasó, falló o se omitió, y su duración | `./mvnw test surefire-report:report-only` | `target/reports/surefire.html` |
| JaCoCo | Cobertura de código: qué líneas y ramas recorren las pruebas | `./mvnw test` | `target/site/jacoco/index.html` |

En el pipeline ambos reportes se publican como artifacts descargables (`Reporte-Pruebas` y `Jacoco-Reporte`) en la sección **Artifacts** de cada ejecución de GitHub Actions. El reporte de pruebas se genera aunque alguna prueba falle.

## Docker

### Dockerfile

Usa un build en dos etapas para que la imagen final sea liviana:

1. **build** (`eclipse-temurin:17-jdk-alpine`): descarga las dependencias con Maven y compila el JAR.
2. **runtime** (`eclipse-temurin:17-jre-alpine`): copia solo el JAR y lo ejecuta en el puerto 8082.

### docker-compose.yml

| Servicio | Imagen | Puerto | Detalle |
|---|---|---|---|
| `mysql` | `mysql:8` | 3306 | Healthcheck con `mysqladmin ping` cada 10 s y volumen `mysql_datos` para persistir los datos |
| `categorias` | build local | 8082 | Arranca solo cuando MySQL está sano (`depends_on` con `condition: service_healthy`) |

Ambos servicios leen sus credenciales desde `.env` mediante el bloque `environment:`.

## Pipeline CI/CD

El workflow está en `.github/workflows/maven.yml`.

**Triggers**

| Evento | Rama | Propósito |
|---|---|---|
| `push` | `develop` | Valida cada funcionalidad integrada y publica la imagen |
| `pull_request` | `main` | Valida los PR de release o hotfix antes de pasar a producción |

**Pasos**

| # | Paso | Detalle |
|---|---|---|
| 1 | Checkout | Descarga el código del repositorio |
| 2 | Verificación | Muestra la estructura del proyecto con `tree` |
| 3 | JDK 17 | Configura Temurin 17 con caché de Maven |
| 4 | Build y pruebas | `mvn -B package`: compila, ejecuta las pruebas y genera el reporte JaCoCo |
| 5 | Reporte JaCoCo | Sube el reporte HTML de cobertura como artifact `Jacoco-Reporte` |
| 6 | Reporte de pruebas | Genera el reporte HTML de resultados con Surefire, aunque alguna prueba falle |
| 7 | Subir reporte de pruebas | Sube el reporte como artifact `Reporte-Pruebas` |
| 8 | Login a ghcr.io | Autenticación con `GITHUB_TOKEN` (solo en push) |
| 9 | Build y push | Construye la imagen y la publica como `ghcr.io/syst3m644/devops_007d_ols:latest` (solo en push) |

El pipeline no corre sobre `main` directamente: como equipo primero validamos cada cambio en `develop` antes de hacer un release a producción.

**Incidencias resueltas**

- **Error de compilación:** la primera ejecución falló porque quedó un conflicto de merge sin resolver en `CategoriaService`, y las pruebas no alcanzaron a correr. Se corrigió en la rama `fix/conflicto-merge-categoriaservice`.
- **Publicación en ghcr.io:** el primer intento falló porque `docker/login-action@v6` no existe (la versión estable es `v4`), el registro estaba escrito como `ghcr:io` y ghcr.io no acepta mayúsculas en el nombre de la imagen. Se corrigió en `feature/pipeline-docker`, usando además `docker/build-push-action@v7`.

## Estrategia de ramificación (GitFlow)

Usamos GitFlow porque es un proyecto colaborativo con ciclos de desarrollo planificados.

| Rama | Origen | Propósito |
|---|---|---|
| `main` | — | Código en producción. Solo recibe cambios vía Pull Request desde `develop` (release) o `hotfix/*` |
| `develop` | `main` | Rama de integración donde se combinan las nuevas funcionalidades |
| `feature/*` | `develop` | Desarrollo de una funcionalidad nueva. Temporal, se elimina al fusionar |
| `docs/*` | `develop` | Cambios de documentación |
| `fix/*` | `develop` | Corrección de errores detectados durante la integración |
| `hotfix/*` | `main` | Corrección urgente de un bug en producción. Se fusiona en `main` y `develop` |

### Flujo de trabajo

1. Se crea una rama `feature/*` desde `develop`, por ejemplo `feature/validacion-datos` o `feature/actualizacion-endpoint`.
2. Al terminar la funcionalidad se abre un Pull Request hacia `develop`.
3. El otro integrante revisa y aprueba el PR antes del merge.
4. Cuando `develop` acumula cambios aprobados, se abre un PR `develop → main` (release).
5. Si ocurre un error en producción, se crea una rama `hotfix/*` desde `main`, se corrige y se fusiona con `main` y `develop` para mantener ambas al día.

### Por qué GitFlow

1. **`main` siempre estable:** la rama de producción se mantiene limpia mientras `develop` concentra las funcionalidades nuevas.
2. **Trabajo en paralelo:** con ramas `feature/*` independientes, cada integrante desarrolló su parte sin interferir con el otro: `feature/actualizacion-endpoint` (Abel Aylas) y `feature/validacion-datos` (Manuel Mora).
3. **Corrección rápida de errores:** las ramas `hotfix/*` permiten arreglar fallos críticos directamente desde `main`.
4. **Revisión entre pares:** cada Pull Request es revisado por el otro integrante, lo que reduce el riesgo de errores.

### Comandos principales

Feature (desde `develop`):

```bash
git checkout develop
git pull origin develop
git checkout -b feature/validacion-datos
git add .
git commit -m "feat: Se agrega validación de datos en el service"
git push -u origin feature/validacion-datos
# Se abre el Pull Request feature/validacion-datos → develop
```

Hotfix (desde `main`):

```bash
git checkout main
git pull origin main
git checkout -b hotfix/arreglo-validacion
git add .
git commit -m "fix: Se corrige validación en obtenerPorId"
git push -u origin hotfix/arreglo-validacion
# Se abre el Pull Request hotfix/arreglo-validacion → main
```

Los commits siguen el formato de Conventional Commits (`feat:`, `fix:`, `docs:`, `ci:`).

## Versionado con tags

Usamos tags anotados para marcar las versiones del proyecto:

| Tag | Descripción |
|---|---|
| `v1.0` | Versión base del microservicio |
| `v1.1` | Hotfix de un bug crítico en la validación por ID |

```bash
git checkout main
git pull origin main
git tag -a v1.1 -m "Hotfix de validación por ID"
git push origin v1.1
```

## Tecnologías usadas

| Área | Tecnología |
|---|---|
| Lenguaje y framework | Java 17, Spring Boot 4.0.6 |
| Persistencia | Spring Data JPA, MySQL 8, H2 (pruebas) |
| Microservicios | Spring Cloud Netflix Eureka Client |
| Validación y documentación | Spring Validation, Springdoc OpenAPI (Swagger) |
| Pruebas y reportes | JUnit 5, Mockito, JaCoCo, Maven Surefire Report |
| Contenedores | Docker, Docker Compose, GitHub Container Registry |
| CI/CD | GitHub Actions |
| Utilidades | Lombok, Maven |

## Uso de inteligencia artificial

Usamos Claude como apoyo durante el desarrollo: para sugerencias de comandos, para entender mejor el flujo de GitFlow, como ayuda en la redacción y para recibir sugerencias y feedback sobre el pipeline a implementar.
