# ms_categoria

Microservicio REST para la gestión de categorías de libros.
Forma parte del proyecto DevOps DSY1103-005D.

## Tecnologías

- Java 17
- Spring Boot 4.0.6
- Spring Data JPA
- MySQL (H2 para pruebas)
- Eureka Client (Spring Cloud)
- Swagger / OpenAPI
- Maven

## Estructura del proyecto

```
src/main/java/cl/syst3m64/categorias/
├── CategoriasApplication.java     # Clase principal
├── config/                        # Configuración (Swagger y carga inicial de datos)
├── controller/                    # Endpoints REST
├── exception/                     # Manejo de errores
├── model/                         # Entidad Categoria
├── repository/                    # Acceso a la base de datos
└── service/                       # Lógica de negocio
```

## Modelo Categoria

| Campo | Tipo | Descripción |
|---|---|---|
| id | Long | Se genera automáticamente |
| nombre | String | Obligatorio, único, máximo 100 caracteres |
| descripcion | String | Opcional, máximo 300 caracteres |

## Endpoints

Ruta base: `/api/category`

| Método | Ruta | Descripción |
|---|---|---|
| GET | `/api/category` | Lista todas las categorías |
| GET | `/api/category/{id}` | Busca una categoría por su ID |
| GET | `/api/category/nombres?nombre=Nuevo` | Busca categorías por nombre |
| POST | `/api/category` | Crea una nueva categoría |
| PUT | `/api/category/{id}` | Actualiza una categoría existente |
| DELETE | `/api/category/{id}` | Elimina una categoría |

### Ejemplo de body (POST / PUT)

```json
{
  "nombre": "Ciencia Ficción",
  "descripcion": "Libros de ciencia ficción y tecnología"
}
```

## Cómo ejecutarlo

```bash
./mvnw spring-boot:run
```

El servicio queda disponible en `http://localhost:8082`.

Con Docker:

```bash
docker build -t ms_categoria .
docker run -p 8082:8082 ms_categoria
```

## Documentación de la API

Con el servicio en ejecución:

- Swagger UI: http://localhost:8082/swagger-ui.html
- OpenAPI JSON: http://localhost:8082/v3/api-docs

## Pruebas

```bash
./mvnw test
```

Incluye pruebas del repositorio, del servicio y del controlador.
El reporte de cobertura (JaCoCo) queda en `target/site/jacoco/index.html`.

## Datos iniciales

Al arrancar, si la tabla está vacía se cargan cuatro categorías por defecto:
Nuevo, Usado, Reparado y Digital.

## Configuración

La configuración está en `src/main/resources/application.yml`:

- Puerto: `8082`
- Base de datos: MySQL (`bd_categorias`)
- Eureka: `http://eureka-server:8761/eureka/`
