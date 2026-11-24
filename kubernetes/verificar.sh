#!/usr/bin/env bash
# =====================================================================
# EP3 - Verificación post-despliegue ("smoke tests")
# Comprueba que el microservicio funciona Y que la observabilidad quedó
# operativa: métricas en Prometheus, logs en Loki y trazas en Jaeger.
# Si algo falla, termina con código 1 y el pipeline se detiene.
#
# Uso: ./kubernetes/verificar.sh   (con el entorno ya desplegado)
# =====================================================================
set -uo pipefail

# Túneles temporales hacia los servicios internos del cluster
kubectl -n biblioteca port-forward svc/ms-categoria 18082:8082 >/dev/null 2>&1 &
kubectl -n monitoring port-forward svc/prometheus   19090:9090 >/dev/null 2>&1 &
kubectl -n monitoring port-forward svc/loki         13100:3100 >/dev/null 2>&1 &
kubectl -n monitoring port-forward svc/jaeger       26686:16686 >/dev/null 2>&1 &
trap 'kill $(jobs -p) 2>/dev/null || true' EXIT
sleep 5
# Si un puerto ya está ocupado (por ejemplo, paneles.sh abierto), kubectl termina al tiro
if [ "$(jobs -rp | wc -l)" -lt 4 ]; then
  echo "ERROR: no se pudieron abrir los túneles (¿puertos 18082/19090/13100/26686 ocupados?)."
  echo "Cierra paneles.sh u otros 'kubectl port-forward' y vuelve a intentar."
  exit 1
fi

APP=http://localhost:18082
PROM=http://localhost:19090
FALLOS=0

# check "descripción" comando...  -> imprime OK/FALLA y cuenta los fallos
check() {
  local desc="$1"; shift
  if "$@" >/dev/null 2>&1; then echo "  [OK]    $desc"; else echo "  [FALLA] $desc"; FALLOS=$((FALLOS + 1)); fi
}
# reintenta un comando hasta N veces (cada 5 s): la observabilidad tarda unos segundos en "ver" los datos
reintentar() {
  local n="$1"; shift
  for _ in $(seq 1 "$n"); do "$@" >/dev/null 2>&1 && return 0; sleep 5; done
  return 1
}
codigo() { curl -s -o /dev/null -w "%{http_code}" "$@"; }
prom_vale_1() { curl -sfG "$PROM/api/v1/query" --data-urlencode "query=$1" | grep -q '"1"\]'; }

echo "==> Microservicio"
check "health = UP"                          reintentar 12 sh -c "curl -sf $APP/actuator/health | grep -q '\"status\":\"UP\"'"
check "readiness = UP (app + base de datos)" sh -c "curl -sf $APP/actuator/health/readiness | grep -q UP"
check "GET /api/category responde 200"       test "$(codigo $APP/api/category)" = "200"
check "POST inválido responde 400"           test "$(codigo -X POST -H 'Content-Type: application/json' -d '{}' $APP/api/category)" = "400"
check "GET de un id inexistente responde 404" test "$(codigo $APP/api/category/999999)" = "404"
check "/actuator/info informa la versión"    sh -c "curl -sf $APP/actuator/info | grep -q version"
check "/actuator/prometheus expone métricas"  sh -c "curl -sf $APP/actuator/prometheus | grep -q http_server_requests_seconds_count"

echo "==> Observabilidad"
check "Prometheus: ms-categoria UP"          reintentar 18 prom_vale_1 'up{app="ms-categoria"}'
check "Prometheus: MySQL UP (exporter)"      reintentar 18 prom_vale_1 'mysql_up'
check "Prometheus: reglas de alerta cargadas" sh -c "curl -sf $PROM/api/v1/rules | grep -q MicroservicioCaido"
check "Loki: recibe logs del microservicio"  reintentar 18 sh -c "curl -sfG http://localhost:13100/loki/api/v1/query_range --data-urlencode 'query={app=\"ms-categoria\"}' --data-urlencode limit=1 | grep -q '\"values\":\[\['"
check "Jaeger: recibe trazas de 'categorias'" reintentar 18 sh -c "curl -sf http://localhost:26686/api/services | grep -q categorias"

if [ "$FALLOS" -gt 0 ]; then
  echo "Resultado: ${FALLOS} verificación(es) fallaron"
  exit 1
fi
echo "Resultado: todas las verificaciones pasaron"
