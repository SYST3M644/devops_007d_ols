#!/usr/bin/env bash
# =====================================================================
# EP3 - Abre túneles (port-forward) hacia los paneles del cluster.
# Los servicios son ClusterIP (no se exponen a internet); este script
# los hace accesibles solo desde tu PC mientras está corriendo.
# Ctrl+C para cerrar todo.
# =====================================================================
set -uo pipefail

kubectl -n biblioteca port-forward svc/ms-categoria 8082:8082 >/dev/null 2>&1 &
kubectl -n monitoring port-forward svc/grafana     3000:3000  >/dev/null 2>&1 &
kubectl -n monitoring port-forward svc/prometheus  9090:9090  >/dev/null 2>&1 &
kubectl -n monitoring port-forward svc/jaeger      16686:16686 >/dev/null 2>&1 &
trap 'kill $(jobs -p) 2>/dev/null; echo; echo "Túneles cerrados."' EXIT

USUARIO=$(kubectl -n monitoring get secret grafana-admin -o jsonpath='{.data.admin-user}' | base64 -d)
CLAVE=$(kubectl -n monitoring get secret grafana-admin -o jsonpath='{.data.admin-password}' | base64 -d)

cat <<EOF

  Microservicio  http://localhost:8082/api/category
  Swagger        http://localhost:8082/swagger-ui.html
  Health         http://localhost:8082/actuator/health
  Métricas       http://localhost:8082/actuator/prometheus
  Grafana        http://localhost:3000   (usuario: ${USUARIO}  clave: ${CLAVE})
  Prometheus     http://localhost:9090   (Status > Targets  /  Alerts)
  Jaeger         http://localhost:16686  (servicio: categorias)

  Ctrl+C para cerrar los túneles.
EOF
wait
