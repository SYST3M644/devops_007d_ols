"Parcial n° 1 DevOps"
Integrantes : Manuel Mora y Abel Aylas

Microservicio : 

Qué es: microservicio REST que administra el catálogo de categorías de libros. Se registra en Eureka para que los demás microservicios lo descubran.
Stack: Java 17 · Spring Boot 4.0.6 · Spring Data JPA · MySQL · Eureka · Swagger · Maven · Docker · Puerto 8082
Arquitectura: por capas — Controller → Service → Repository → MySQL, con un manejador global de errores que unifica las respuestas de fallo.
Ejecución: ./mvnw spring-boot:run → http://localhost:8082
Documentación de la API: http://localhost:8082/swagger-ui.html
Pruebas: ./mvnw test (11 pruebas de repositorio, servicio y controlador)
Extras: al arrancar carga cuatro categorías por defecto (Nuevo, Usado, Reparado, Digital) si la base está vacía.


Estrategia de ramificacion en este proyecto: Este microservicio "Ms_categoria" usa GitFlow como estrategia de ramificacion, por lo cual esta compuesta por ramas lo mostramos a continuacion:

Rama Main : Contiene el codigo funcional (Produccion) , solo va recibir cambios via PULL Requests, desde la rama develop(releases) y en ramas llamadas Hotfix (que permite hacer cambios con urgencia)

Rama develop: Rama de integracion donde se convinan las nuevas funcionalidades (Feature) antes de pasar a produccion (Main)

Rama Feature: es una rama temporal creadas desde la rama develop, para desarrollar una nueva funcionalidad requerida. por ejemplo (feature/validar-datos) (feature/actualizacion-edpoint). Donde es una rama temporal se puede eliminar.

Rama hotfix : es una rama temporal creada desde main con el proposito de corregir un bug urgente de codigo de produccion.

El flujo que usamos:
1) Se crea la rama feature/validar-datos u feature/actualizacion-edpoint desde develop
2) Al finalizar con la funcionalidad,se habre un pull request hacia la rama develop
3) Un compañero compañero abel o manuel, depende quien hizo la nueva validacion revisa y aprueba antes de realizar el merge
4) Cuando develop acumula cambios aprobados, se hace un pull requets desde  develop -> main (release)
5) Si en un momento ocurre un error en produccion (main) se hace una rama hotfix proveniente de main.
se corrige el error y se fuciona con main y develop para mantener un registro.


Justificación de GitFlow:

como equipo de trabajo decidimos elegir GitFlow como estrategia de ramificación para nuestro microservicio(ms_categoria), este flujo de trabajo se ajusta a nuestras necesidades ya que es un proyecto altamente colaborativo con ciclos de desarrollos altamente planificados con un largo tiempo de desarrollo.

unas de las razones de porque optamos a este flujo de tabajo:

1) La rama main se mantiene siempre limpia y lista para producción, mientras que la rama de develop concentra la nuevas funcionalidades que desarrollaremos mediante las ramas feature.

2) Al usar las ramas feature/**** idependientes, ambos integrantes pudimos registrar desarrollos de funcionalidades distintas : feature/actualizacion-edpoint(Abel aylas) y feature/validacion-de-datos(Manuel Mora) sin inteferir en el trabajo del otro, evitando problemas.

3) Manejos de errores, este flujo de trabajo nos permite resolver errores de código urgentemente. La rama hotfix/**** permite corregir fallos críticos directamente desde main.

4) al hacer pull request y revisión entre el equipo, se reduce el riesgo de errores.

Comandos usados (mas relevantes a explicar)

git Flow init // iniciamos GitFLow aceptando los nombres de las ramas

//Rama develop -> feature
git checkout develop // nos posicionamos en la rama develop

git checkout -b feature/validacion-datos // creamos la rama feature con un nombre descriptivo de una nueva funcionalidad este caso validamos datos

git add service.java // guardamos los cambios esta caso de un service donde se concentra la lógica de negocio

git commit -m "feat: Se agrega validación de datos, dando una mejora visual a los desarrolladores" // confirmamos el cambio con un mensaje

git push -u origin feature/validacion-datos  // empujamos los nuevos cambios listos para hacer un pull request

// luego de validar los nuevos cambios en ambos repositorios locales (nuestros pc) ingresamos:
git checkout develop

git pull origin develop // actualizara los cambios desde el repositorio en git hub en la rama develop 

(hicimos 2 feature por lo cual son los mismos comandos solo cambia el nombre de la rama feature y su funcionalidad)

//Rama main -> hotfix

git checkout main // Nos posicionamos en la rama main

git checkout -b hotfix/arreglos-null // Creamos hotfix con el nombre del error a solucionar

git add service.java //Añadimos los cambios de mejora en el código (service)

git commit -m "fix: Se arregla problema nullPoinerException" // Le asignamos un mensaje descriptivo 

git push -u origin  hotfix/arreglos-null // subimos los cambios 

<<<<<<< HEAD
base: main <- compare:  hotfix/arreglos-null (revisamos y mergeamos hacia main)

**Papeline CI (Git actions):
Se configuro un workflow en .github/workflows que tiene cuyo objetivo de optimizar las validaciones y integraciones del microservicio ms_categorias.

** Triggers : 
push ['develop'] se ejecuta cada vez que se agrega una nueva funcionalidad (feature) hacia la rama develop

pull_request['Main'] se ejecutara vea un PR de release o hotfix hacia main (produccion), validando antes de fusionar.

**Jobs:
Cargando el microservicio con sus dependecias,pom.xml, ademas se configura Java con su JDK 17 (este caso).
Se verifica todo el contenido del microservicio , mostrando lo que compone su carpeta ordenadamente.
Lo mas importante del papeline implementado, permite ejecutar todos los test unitarios del microservicio, copilando el proyecto, validando que no ocurra errores facilitando un future release hacia produccion.

***En nuestro proceso se encontro un error*** -> cuando se ejecuto el papeline ocurrio un error de copilacion por ende sus pruebas unitarias no corrieron, cuyo error fue una falla sintaxis.
se soluciono implementando una rama feature (nueva mejora) llamada: 
fix/conflicto-merge-categoriaservice cuya mejora implementada , se encargo de solucionar el error, dando exitosamente el funcionamiento del papeline.

Importante : el papeline no se hizo sobre main, porque primero como equipo validamos que todo codigo, sea mejora (nueva funcionalidad) sea validada antes de un release hacia main (produccion)
=======
base: main/master <- compare:  hotfix/arreglos-null (revisamos y mergeamos hacia main)

//Tag
Ocupamos tag para el control de versiones en el repositorio empezamos con la version v1.0 subiendo el proyecto base 
y actualizamos el tag a la v1.1 cuando hicimos un hotfix de bug critico en el codigo

//Comandos de uso en Tag:
git checkout develop
git pull origin develop
git checkout -b docs/documentacion-readme
git add .
git commit -m "docs: Se agrega documentacion del microservicio ms_categoria"
git push -u origin docs/documentacion-readme
>>>>>>> 2fa73571ede2eb672a53182dd4aba94151d74c4a
