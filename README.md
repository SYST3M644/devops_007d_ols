# ms_categoria · Microservicio de Categorías

Microservicio REST desarrollado para la asignatura de DevOps. Administra el catálogo de categorías de libros de la Biblioteca Online y se registra en Eureka para que los demás microservicios del sistema lo puedan descubrir.

Este README está dividido en dos partes: la [Evaluación Parcial 1](#evaluación-parcial-1) (microservicio, GitFlow y pipeline de integración continua básico) y la [Evaluación Parcial 2](#evaluación-parcial-2) (contenedores, orquestación, pruebas con reportes, análisis de seguridad y publicación de la imagen).

## Integrantes

- Abel Aylas
- Manuel Mora

## Contenido

**Evaluación Parcial 1**

- [De qué trata](#de-qué-trata)
- [Arquitectura](#arquitectura)
- [Cómo levantar el proyecto](#cómo-levantar-el-proyecto)
- [Cómo usar la API](#cómo-usar-la-api)
- [Documentación Swagger](#documentación-swagger)
- [Pruebas](#pruebas)
- [Pipeline CI/CD](#pipeline-cicd)
- [Estrategia de ramificación (GitFlow)](#estrategia-de-ramificación-gitflow)
- [Versionado con tags](#versionado-con-tags)
- [Tecnologías usadas](#tecnologías-usadas)
- [Uso de inteligencia artificial](#uso-de-inteligencia-artificial)

**Evaluación Parcial 2**


- [Contenedores: Dockerfile](#contenedores-dockerfile)
- [Orquestación con Docker Compose](#orquestación-con-docker-compose)
- [Pruebas automatizadas y reportes](#pruebas-automatizadas-y-reportes)
- [Pipeline CI/CD actualizado](#pipeline-cicd-actualizado)
- [Seguridad: SCA y SAST](#seguridad-sca-y-sast)
- [Publicación de la imagen](#publicación-de-la-imagen)
- [Trazabilidad y calidad](#trazabilidad-y-calidad)
- [Limitaciones y mejoras](#limitaciones-y-mejoras)
- [Tecnologías agregadas](#tecnologías-agregadas)
- [Uso de IA en esta evaluación](#uso-de-ia-en-esta-evaluación)
- [Conclusiones](#conclusiones)

---

# Evaluación Parcial 1

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

### Sin Docker

Requiere Java 17 y un MySQL local con la base `bd_categorias` creada.

```bash
cd ms_categoria
export DB_USER=root DB_PASSWORD=tu_password EUREKA_CLIENT_ENABLED=false
./mvnw spring-boot:run
```

`DB_HOST`, `DB_PORT` y `DB_NAME` son opcionales y por defecto apuntan a `localhost:3306/bd_categorias`.

Para levantarlo con Docker Compose, ver [Orquestación con Docker Compose](#orquestación-con-docker-compose) en la Evaluación Parcial 2.

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

En esta evaluación el proyecto contaba con **11 pruebas unitarias** escritas con JUnit 5 y Mockito. Las pruebas usan el perfil `test` con H2 en memoria, así que no necesitan MySQL.

```bash
cd ms_categoria
./mvnw test
```

| Clase | Qué prueba | Tests |
|---|---|---|
| `CategoriaControllerTest` | Endpoints REST (`@WebMvcTest`) | 2 |
| `CategoriaServiceTest` | Lógica de negocio con el repositorio simulado | 3 |
| `CategoriaRepositoryTest` | Consultas JPA contra H2 (`@DataJpaTest`) | 5 |
| `EspeciesApplicationTests` | Carga del contexto de Spring | 1 |
| **Total** | | **11** |


## Pipeline CI/CD

El workflow está en `.github/workflows/maven.yml`.

**Triggers**

| Evento | Rama | Propósito |
|---|---|---|
| `push` | `develop` | Valida cada funcionalidad integrada |
| `pull_request` | `main` | Valida los PR de release o hotfix antes de pasar a producción |

**Pasos**

| # | Paso | Detalle |
|---|---|---|
| 1 | Checkout | Descarga el código del repositorio |
| 2 | Verificación | Muestra la estructura del proyecto con `tree` |
| 3 | JDK 17 | Configura Temurin 17 con caché de Maven |
| 4 | Build y pruebas | `mvn -B package`: compila y ejecuta las pruebas unitarias |
| 5 | Resultado | Mensaje de éxito si el build y las pruebas pasan |

En esta evaluación el pipeline no corría sobre `main` directamente: como equipo primero validábamos cada cambio en `develop` antes de hacer un release a producción. En la Evaluación Parcial 2 se agregó el push a `main` (ver [Pipeline CI/CD actualizado](#pipeline-cicd-actualizado)).

**Incidencias resueltas**

- **Error de compilación:** la primera ejecución falló porque quedó un conflicto de merge sin resolver en `CategoriaService`, y las pruebas no alcanzaron a correr. Se corrigió en la rama `fix/conflicto-merge-categoriaservice`.

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
| Pruebas | JUnit 5, Mockito |
| CI/CD | GitHub Actions |
| Utilidades | Lombok, Maven |

## Uso de inteligencia artificial

Usamos Claude como apoyo durante el desarrollo: para sugerencias de comandos, para entender mejor el flujo de GitFlow, como ayuda en la redacción y para recibir sugerencias y feedback sobre el pipeline a implementar.

------------------------------------------------------------------------------------------------------------------

# Evaluación Parcial 2

Esta parte es la continuación de la Evaluación Parcial 1. Sobre el mismo microservicio ms_categoria automatizamos su ciclo de vida con contenedores, orquestación, pruebas con reportes, análisis de seguridad y publicación de la imagen docker. Seguimos un flujo de lo más básico a lo más complejo: primero el Dockerfile, luego Docker Compose y finalmente el pipeline, al que le fuimos agregando pruebas, reportes, seguridad y la publicación de la imagen.

## Contenedores: Dockerfile

El Dockerfile viene desde la versión base del microservicio. En esta evaluación lo integramos al flujo completo: Docker Compose lo usa para construir el servicio `categorias` y el pipeline lo usa para construir y publicar la imagen.

Usa un build en dos etapas (multi-stage):

| Etapa | Imagen base | Qué hace |
|---|---|---|
| `build` | `eclipse-temurin:17-jdk-alpine` | Instala Maven, descarga las dependencias y compila el JAR |
| runtime | `eclipse-temurin:17-jre-alpine` | Copia solo el JAR desde la etapa `build` y lo ejecuta en el puerto 8082 |

Detalles de la construcción:

- **Imagen final liviana:** la etapa final solo tiene el JRE y el JAR. Maven, el JDK y el código fuente quedan fuera de la imagen.
- **Caché de dependencias:** primero se copia `pom.xml` y se ejecuta `mvn dependency:go-offline`; después se copia `src`. Si solo cambia el código, Docker reutiliza la capa con las dependencias ya descargadas.
- **`-DskipTests`:** las pruebas no se repiten dentro del build de la imagen porque ya se ejecutan en el pipeline antes de construirla.
- **`ENTRYPOINT ["java","-jar","app.jar"]`:** en formato exec, Java recibe directamente las señales de Docker y se detiene limpiamente.

Construir la imagen localmente:

```bash
docker build -t ms-categoria ./ms_categoria
```

## Orquestación con Docker Compose

`ms_categoria/docker-compose.yml` levanta el microservicio junto a su base de datos en una red interna. Los servicios se comunican por nombre: el microservicio se conecta a MySQL usando `DB_HOST: mysql`.

| Servicio | Imagen | Puerto | Detalle |
|---|---|---|---|
| `mysql` | `mysql:8` | 3306 | Healthcheck con `mysqladmin ping` cada 10 s (5 reintentos) y volumen `mysql_datos` para persistir los datos |
| `categorias` | Build local con el Dockerfile | 8082 | Arranca solo cuando MySQL está sano (`depends_on` con `condition: service_healthy`) |

- **Healthcheck:** MySQL se marca como `healthy` solo cuando responde al ping. En el comando se usa `$$MYSQL_ROOT_PASSWORD` para que Compose no reemplace la variable y la expanda el shell dentro del contenedor.
- **Orden de arranque:** `condition: service_healthy` evita que el microservicio intente conectarse antes de que la base de datos esté lista.
- **Persistencia:** el volumen `mysql_datos` conserva los datos aunque se eliminen los contenedores.
- **Eureka:** se desactiva con `EUREKA_CLIENT_ENABLED=false`, porque en este Compose no se levanta un servidor Eureka.

### Variables de entorno

Las credenciales no están escritas en el código ni en el `docker-compose.yml`: se leen desde el archivo `.env`, que está en `.gitignore` y no se sube al repositorio. El archivo `.env.example` sirve de plantilla.

| Variable | Descripción | Valor de ejemplo |
|---|---|---|
| `MYSQL_ROOT_PASSWORD` | Contraseña del usuario root de MySQL | `cambiar_root` |
| `MYSQL_DATABASE` | Nombre de la base de datos | `bd_categorias` |
| `MYSQL_USER` | Usuario con el que se conecta el microservicio | `categorias` |
| `MYSQL_PASSWORD` | Contraseña de ese usuario | `cambiar_password` |
| `EUREKA_CLIENT_ENABLED` | Registro en Eureka (`false` si se levanta solo este servicio) | `false` |

### Cómo levantarlo

Solo se necesita tener Docker Desktop instalado.

```bash
cd devops_007d_ols/ms_categoria
docker compose up --build
```


| Acción | Comando |
|---|---|
| Ver el estado de los servicios | `docker compose ps` |
| Ver los logs del microservicio | `docker compose logs -f categorias` |
| Detener | `docker compose down` |
| Empezar de cero (borra la base de datos) | `docker compose down -v` |


## Pruebas automatizadas y reportes

En esta evaluación ampliamos las pruebas de 11 a **32**, escritas con JUnit 5 y Mockito. Usan el perfil `test`,
cuando teniamos 11 test, teniamos muy poca porcentaje de cobertura, Usamos IA  de forma etica para aumentar la cantidad de test, para aumentar el porcentaje de cobertura, permitiendo asi implementar bien el pipeline con jacoco.

### Reportes

| Reporte | Qué muestra | Comando local | Archivo |
|---|---|---|---|
| Surefire | Resultado de cada prueba: si pasó, falló o se omitió, y su duración | `./mvnw test surefire-report:report-only` | `target/reports/surefire.html` |
| JaCoCo | Cobertura de código: qué líneas y ramas recorren las pruebas | `./mvnw test` | `target/site/jacoco/index.html` |

En el pipeline ambos reportes se publican como artifacts (`Reporte-Pruebas` y `Jacoco-Reporte`). Para descargarlos: pestaña **Actions** → elegir una ejecución → sección **Artifacts** al final de la página (se necesita iniciar sesión en GitHub). Cada ejecución genera sus propios reportes.

### ¿Qué pasa si una prueba falla?

- Las pruebas se ejecutan en el paso `mvn -B package` del pipeline.
- Si una prueba falla, Maven termina con `BUILD FAILURE`  y el job queda en rojo.
- Se saltan los pasos siguientes: el análisis de SonarQube, el login y la publicación de la imagen. **Una versión con pruebas fallidas nunca se publica.**
- Solo se generan y suben los reportes de pruebas (`if: always()`), para revisar qué prueba falló.

## Pipeline CI/CD actualizado

El workflow sigue en `.github/workflows/maven.yml`. Respecto a la Evaluación Parcial 1 se agregaron los reportes, el análisis de SonarQube con su alerta, la construcción y publicación de la imagen docker, los permisos del token y el trigger de push a `main`.

**Triggers**

| Evento | Rama | Qué hace |
|---|---|---|
| `push` | `develop` | Pruebas, reportes y publicación de la imagen |
| `push` | `main` | Pruebas, reportes, análisis SonarQube y publicación de la imagen |
| `pull_request` | `main` | Pruebas, reportes y análisis SonarQube. No publica la imagen |

**Permisos y secretos**

| Elemento | Valor | Uso |
|---|---|---|
| `permissions` | `contents: read`, `packages: write` | Mínimo privilegio: el token solo puede leer el código y publicar en GHCR |
| `SONAR_TOKEN` | Secret del repositorio | Autenticación con SonarQube Cloud |
| `GITHUB_TOKEN` | Generado automáticamente en cada ejecución | Login en `ghcr.io` |

**Pasos**

| # | Paso | Detalle |
|---|---|---|
| 1 | Checkout | Descarga el código con historial completo (`fetch-depth: 0`), necesario para SonarQube |
| 2 | Verificación | Muestra la estructura del proyecto con `tree` |
| 3 | JDK 17 | Configura Temurin 17 con caché de Maven |
| 4 | Build y pruebas | `mvn -B package`: compila, ejecuta las pruebas y genera el reporte JaCoCo |
| 5 | Resultado | Mensaje de éxito |
| 6 | Reporte JaCoCo | Sube el reporte de cobertura como artifact `Jacoco-Reporte` |
| 7 | Reporte de pruebas | Genera el HTML de Surefire aunque alguna prueba falle (`if: always()`) |
| 8 | Subir reporte de pruebas | Sube el reporte como artifact `Reporte-Pruebas` (`if: always()`) |
| 9 | Análisis SonarQube Cloud | Solo en PR o en `main`, nunca en ejecuciones de Dependabot. Espera el Quality Gate (`sonar.qualitygate.wait=true`) y falla si no se aprueba |
| 10 | Alerta si falla SonarQube | Solo si falla el paso 9: deja un resumen en la ejecución y una anotación de error |
| 11 | Login a ghcr.io | Autenticación con `GITHUB_TOKEN` (solo en push) |
| 12 | Nombre de imagen en minúsculas | Convierte el nombre del repositorio a minúsculas, porque ghcr.io no acepta mayúsculas |
| 13 | Build y push | Construye la imagen con el Dockerfile y la publica en `ghcr.io` (solo en push) |


**Incidencias resueltas**

- **Publicación en ghcr.io:** el primer intento falló porque `docker/login-action@v6` no existe (la versión estable es `v4`), el registro estaba escrito como `ghcr:io` y ghcr.io no acepta mayúsculas en el nombre de la imagen. Se corrigió en `feature/pipeline-docker`, usando además `docker/build-push-action@v7` y un paso que convierte el nombre a minúsculas.

## Seguridad: SCA y SAST

El pipeline combina dos tipos de análisis: **SAST** (análisis estático del código propio) con SonarQube Cloud y **SCA** (análisis de las dependencias de terceros) con Dependabot.

### SAST: SonarQube Cloud

| Elemento | Valor |
|---|---|
| Organización | `syst3m644` |
| Proyecto | `SYST3M644_devops_007d_ols` |
| Configuración | Propiedades `sonar.*` en `ms_categoria/pom.xml` |
| Token | Secret `SONAR_TOKEN` (nunca en el código) |
| Cuándo corre | Pull Requests hacia `main` y push a `main` |
| Qué analiza | Bugs, vulnerabilidades, security hotspots, code smells, duplicación y cobertura (desde JaCoCo) |
| Dashboard | https://sonarcloud.io/dashboard?id=SYST3M644_devops_007d_ols |


- **Dependabot:** el análisis se omite en las ejecuciones disparadas por Dependabot, porque GitHub no les entrega los secrets del repositorio y el análisis fallaría sin `SONAR_TOKEN`.

### SCA: Dependabot

Configurado en `.github/dependabot.yml`. Revisa las dependencias cada semana y abre Pull Requests hacia `develop`, respetando GitFlow.


Actualizaciones ya integradas:

| PR | Actualización |
|---|---|
| #24 | `jacoco-maven-plugin` → 0.8.15 |
| #25 | `spring-cloud-dependencies` → 2025.1.4 |
| #26 | `actions/upload-artifact` v6 → v7 |
| #27 | `apache-maven` → 3.10.0 |
| #28 | `springdoc-openapi-starter-webmvc-ui` → 3.1.1 |

### Otros controles de seguridad

- Permisos mínimos para el token del pipeline (`contents: read`, `packages: write`).
- Secretos fuera del código: `SONAR_TOKEN` en GitHub Secrets y credenciales de MySQL en `.env` (ignorado por Git).

### ¿Por qué usamos SCA y SAST?

Principalmente utilizamos SAST en sonarQube principalmente para monitoriar la calidad de nuestro codigo fuente, permitiendo detectar bugs, malas practicas y cobertura junto jacoco

Y por otro lado se opto por utilizar SCA con dependaBot, permitiendo detectar vulnerabilidades como librerias de terceros y generar Pull Request automatico a librerias seguras.

Porque se hizo asi, para viusalizar que nuestro microservicio cuente con la seguridad minima en el desarrollo.

## Publicación de la imagen

En cada push a `develop` o `main`, el pipeline construye la imagen con el Dockerfile y la publica en GitHub Container Registry. Se puede ver en la sección **Packages** del repositorio.

```bash
docker pull ghcr.io/syst3m644/devops_007d_ols:latest
```

- El login se hace con el `GITHUB_TOKEN` de la ejecución, sin credenciales personales.
- La imagen solo se publica si pasaron las pruebas (y el Quality Gate, cuando corre SonarQube).

## Trazabilidad y calidad

### Trazabilidad

Cada cambio se puede seguir desde el commit hasta la imagen publicada:

| Eslabón | Dónde se ve |
|---|---|
| Commit (Conventional Commits) | Historial de commits |
| Rama y Pull Request revisado | Pestaña Pull Requests |
| Ejecución del pipeline (commit, autor y logs de cada paso) | Pestaña Actions |
| Reportes de pruebas y cobertura | Artifacts de cada ejecución |
| Análisis de código | Historial en SonarQube Cloud |
| Imagen publicada | Packages (`ghcr.io`) |
| Versiones liberadas | Tags `v1.0` y `v1.1` |

Historial de cambios de esta evaluación:

| Fecha | Cambio | Rama / PR |
|---|---|---|
| 28-09-2026 | Docker Compose con MySQL | `feature/implementacion-docker-compose` (PR #12) |
| 12-10-2026 | Reporte JaCoCo en el pipeline y nuevas pruebas unitarias | `feature/implementacion-jacoco` |
| 24-10-2026 | Construcción y publicación de la imagen en ghcr.io | `feature/pipeline-docker` (PR #15) |
| 24-10-2026 | Variables de entorno con `.env` | `feature/variables-env` (PR #16) |
| 24-10-2026 | Reporte HTML de pruebas con Surefire | `feature/reporte-pruebas-html` (PR #19) |
| 26-10-2026 | SonarQube Cloud y Dependabot | `feature/sonarqube-cloud` (PR #21) |
| 26-10-2026 | Actualizaciones de dependencias de Dependabot | PRs #24 a #28 |

### Política de calidad

Un cambio solo avanza si cumple estas condiciones:

1. **Las pruebas pasan:** si una falla, el pipeline se detiene y no se publica la imagen.
2. **La cobertura se mide en cada build** con JaCoCo y queda como evidencia descargable.
3. **El Quality Gate de SonarQube se aprueba** en los Pull Requests hacia `main` y en `main`.
4. **Revisión entre pares:** cada Pull Request lo revisa el otro integrante antes del merge.

## Uso de IA en esta evaluación

Usamos Claude como apoyo para revisar el repositorio frente a la pauta de evaluación, recibir feedback sobre el pipeline y la configuración de Docker, preparar la defensa y redactar la documentación de este README, permitiendo mejora ortografica y sintaxis de README, que fue revisada y validada por el equipo. Las decisiones técnicas, las justificaciones y las reflexiones individuales son propias del equipo y se escribieron sin IA y sinceros sobre aprendizaje.


### Reflexión de Manuel Mora

Mi reflexion a esta experiencia de aprendizaje, es que aprendi como construir un docker compose ya sea configurarlo, construir una imagen, checkear el  healthcheck por ejemplo en este caso a mysql (base de datos), realizar volumenes directo en el .yml de compose y depends_on, Que semestre anterior no aprendi muy bien el concepto de docker, ahora a gracias a devops tengo conocimientos mas firmes sobre docker y me siento mas preparado.

Sobre el pipeline, aprendi construir o implementar reportes jacoco desde actions, y empaquetar y publicar una imagen docker. (Yo cause el error de la version v6), pero gracias a mi compañero abel, me ayudo solucionar la version y poder ejecutar correctamente el pipeline, permitiendo visualizar las pruebas unitarias gracias al reporte jacoco, junto el package en github de la imagen docker.

Sobre dependabot y sonarQube. Principalmente no fui directo en codigo, pero pude redactar su teoria, su importacia y como impacta como sus funciones etc. 

De todas formar visualize y apoye a mi compañero abel en el desarrollo de SAST Y SCA, aprendiendo como se construye paso a paso y como visualizar el dashboard que nos genera de nuestro proyecto.

### Reflexión de Abel Aylas

Aprendi a usar jacoco para validar la cobertura del codigo. Teniamos pocos test, por lo cual decidimos agregar mas test usando IA para tener mas cobertura del codigo, en la cual obtuvimos el 100%.

Y tambien aprendi a usar Dependabot como SCA para ver vulnerabilidades de codigo, y tambien SonarQube, que me sirvio para ver la calidad del codigo, en la cual detecto dos fallas o posibles vulneraciones sobre el ID, ya que el ID es un parametro que no puede viajar en JSON, y nos recomienda usar un DTO para que esos datos no viajen en el JSON.

Tambien con el apoyo de mi compañero Manuel, que con el pudimos implementar todo el sistema de Dockerfile. Me costo un poco integrarme a las clases, por eso no pude ver bien el Docker Compose o el empaquetado de imagen o el Pipeline de maven.yml, pero si recuerdo algo del semestre anterior sobre el empaquetado de imagen con Dockerfile. El apoyo de mi compañero Manuel fue fundamental para sacar el proyecto adelante.

