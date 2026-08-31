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