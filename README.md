"Parcial n° 1 DevOps"
Integrantes : Manuel Mora y Abel Aylas

Microservicio : Ms_categorias.

Estrategia de ramificacion en este proyecto: Este microservicio "Ms_categoria" usa GitFlow como estrategia de ramificacion, por lo cual esta compuesta por ramas lo mostramos a continuacion:

Rama Main/Master : Contiene el codigo funcional (Produccion) , solo va recibir cambios via PULL Requests, desde la rama develop(releases) y en ramas llamadas Hotfix (que permite hacer cambios con urgencia)

Rama develop: Rama de integracion donde se convinan las nuevas funcionalidades (Feature) antes de pasar a produccion (Main/Master)

Rama Feature: es una rama temporal creadas desde la rama develop, para desarrollar una nueva funcionalidad requerida. por ejemplo (feature/validar-datos) (feature/actualizacion-edpoint). Donde es una rama temporal se puede eliminar.

Rama hotfix : es una rama temporal creada desde main/master con el proposito de corregir un bug urgente de codigo de produccion.

El flujo que usamos:
1) Se crea la rama feature/validar-datos u feature/actualizacion-edpoint desde develop
2) Al finalizar con la funcionalidad,se habre un pull request hacia la rama develop
3) Un compañero compañero abel o manuel, depende quien hizo la nueva validacion revisa y aprueba antes de realizar el merge
4) Cuando develop acumula cambios aprobados, se hace un pull requets desde  develop -> main/master (release)
5) Si en un momento ocurre un error en produccion (main/master) se hace una rama hotfix proveniente de main/master.
se corrige el error y se fuciona con main/master y develop para mantener un registro.


Justificación de GitFlow:

como equipo de trabajo decidimos elegir GitFlow como estrategia de ramificación para nuestro microservicio(ms_categoria), este flujo de trabajo se ajusta a nuestras necesidades ya que es un proyecto altamente colaborativo con ciclos de desarrollos altamente planificados con un largo tiempo de desarrollo.

unas de las razones de porque optamos a este flujo de tabajo:

1) La rama main/master se mantiene siempre limpia y lista para producción, mientras que la rama de develop concentra la nuevas funcionalidades que desarrollaremos mediante las ramas feature.

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

// Hotfix

git checkout main // Nos posicionamos en la rama main
git checkout -b hotfix/arreglos-null // Creamos hotfix con el nombre del error a solucionar

git add service.java //Añadimos los cambios de mejora en el código (service)
git commit -m "fix: Se arregla problema nullPoinerException" // Le asignamos un mensaje descriptivo 
git push -u origin  hotfix/arreglos-null // subimos los cambios 

base: main/master <- compare:  hotfix/arreglos-null (revisamos y mergeamos hacia main)