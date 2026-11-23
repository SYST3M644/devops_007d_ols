# ---------- Etapa 1: compilación ----------
FROM eclipse-temurin:17-jdk-alpine AS build
RUN apk add --no-cache maven
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -B -q
COPY src ./src
RUN mvn package -DskipTests -B -q

# ---------- Etapa 2: ejecución ----------
FROM eclipse-temurin:17-jre-alpine

# EP3 - Trazabilidad: el pipeline inyecta el SHA del commit. Queda en la imagen (label),
# en /actuator/info y en cada traza, así se sabe exactamente qué código está corriendo.
ARG APP_VERSION=local
ENV APP_VERSION=${APP_VERSION}
LABEL org.opencontainers.image.revision="${APP_VERSION}" \
      org.opencontainers.image.source="https://github.com/SYST3M644/devops_007d_ols" \
      org.opencontainers.image.title="ms_categoria"

# EP3 - Cumplimiento (CIS Docker Benchmark 4.1): la app corre con un usuario sin privilegios,
# no como root. Si alguien explota la app, no obtiene control del contenedor.
# UID numérico (10001) para que Kubernetes pueda verificar "runAsNonRoot".
RUN addgroup -S -g 10001 app && adduser -S -u 10001 -G app app

WORKDIR /app
COPY --from=build --chown=10001:10001 /app/target/*.jar app.jar
USER 10001:10001

EXPOSE 8082
# La JVM usa como máximo el 60% de la memoria límite del contenedor para el heap;
# el resto queda para metaspace, hilos y código nativo (evita que Kubernetes la mate por OOM).
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=60.0", "-jar", "app.jar"]
