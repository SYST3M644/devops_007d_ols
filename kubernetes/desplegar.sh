#!/usr/bin/env bash
# =====================================================================
# EP3 - Despliegue automatizado del entorno completo en Kubernetes:
# microservicio + MySQL + stack de observabilidad (Prometheus, Loki,
# Alloy, Jaeger y Grafana).
#
# Lo usa el pipeline (cluster kind efímero en GitHub Actions) y también
# se puede correr en el PC (Kubernetes de Docker Desktop).
#
# Uso:  ./kubernetes/desplegar.sh [imagen]
#       (por defecto: ms-categoria:local)
# =====================================================================
set -euo pipefail                      # cualquier error detiene el script (y el pipeline)

DIR="$(cd "$(dirname "$0")" && pwd)"
IMAGEN="${1:-ms-categoria:local}"
INICIO=$(date +%s)

# Genera una contraseña aleatoria de 32 caracteres hexadecimales
aleatorio() { head -c 32 /dev/urandom | od -An -tx1 | tr -d ' \n' | head -c 32; }

# ¿Es un redespliegue? (si el stack ya existía, hay que reiniciarlo para que lea la configuración nueva)
REDESPLIEGUE=no
kubectl get namespace monitoring >/dev/null 2>&1 && REDESPLIEGUE=si

echo "==> 1/6 Namespaces (con políticas Pod Security)"
kubectl apply -f "$DIR/00-namespaces.yaml"

echo "==> 2/6 Secretos (contraseñas aleatorias, nunca se guardan en git)"
if ! kubectl -n biblioteca get secret mysql-credentials >/dev/null 2>&1; then
  kubectl -n biblioteca create secret generic mysql-credentials \
    --from-literal=username=categorias \
    --from-literal=password="$(aleatorio)" \
    --from-literal=root-password="$(aleatorio)" \
    --from-literal=exporter-password="$(aleatorio)"
fi
if ! kubectl -n monitoring get secret grafana-admin >/dev/null 2>&1; then
  kubectl -n monitoring create secret generic grafana-admin \
    --from-literal=admin-user=admin \
    --from-literal=admin-password="$(aleatorio)"
fi

echo "==> 3/6 Stack de observabilidad"
# El dashboard (JSON) se carga como ConfigMap; "apply" lo crea o lo actualiza
kubectl -n monitoring create configmap grafana-dashboards \
  --from-file="$DIR/monitoring/dashboards/" --dry-run=client -o yaml | kubectl apply -f -
kubectl apply -f "$DIR/monitoring/"
if [ "$REDESPLIEGUE" = "si" ]; then
  # Los ConfigMaps no reinician los pods solos: se fuerza para aplicar cambios de configuración
  kubectl -n monitoring rollout restart deployment/prometheus deployment/alloy deployment/grafana
fi

echo "==> 4/6 Base de datos MySQL + exporter de métricas"
kubectl apply -f "$DIR/app/mysql.yaml"
kubectl -n biblioteca rollout status deployment/mysql --timeout=300s

echo "==> 5/6 Microservicio con la imagen: ${IMAGEN}"
# Reemplaza el marcador de imagen por la imagen versionada (SHA del commit)
sed "s|image: ms-categoria:local|image: ${IMAGEN}|" "$DIR/app/ms-categoria.yaml" | kubectl apply -f -
# Trazabilidad del despliegue: queda registrado en "kubectl rollout history"
kubectl -n biblioteca annotate deployment/ms-categoria \
  kubernetes.io/change-cause="despliegue de ${IMAGEN} ($(date -u +%Y-%m-%dT%H:%M:%SZ))" --overwrite

echo "==> 6/6 Esperando que todo quede disponible"
# Si algún componente no queda Ready a tiempo, rollout status falla y el pipeline se detiene
kubectl -n biblioteca rollout status deployment/ms-categoria --timeout=420s
for d in prometheus loki alloy jaeger grafana; do
  kubectl -n monitoring rollout status deployment/"$d" --timeout=300s
done

SEGUNDOS=$(( $(date +%s) - INICIO ))
echo "Despliegue completo en ${SEGUNDOS} s"
# En GitHub Actions deja el tiempo de despliegue como salida del paso
if [ -n "${GITHUB_OUTPUT:-}" ]; then
  echo "segundos=${SEGUNDOS}" >> "$GITHUB_OUTPUT"
fi
