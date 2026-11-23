#!/usr/bin/env bash
# =====================================================================
# EP3 - Despliegue en el PC (Kubernetes de Docker Desktop)
#   1. Construye la imagen etiquetada con el SHA del commit actual
#   2. La carga en el nodo de Kubernetes (Docker Desktop usa "kind",
#      que tiene su propio almacén de imágenes)
#   3. Ejecuta desplegar.sh
#
# Uso (desde la raíz del repo, en Git Bash):  ./kubernetes/local.sh
# Luego, para abrir los paneles:              ./kubernetes/paneles.sh
# =====================================================================
set -euo pipefail

RAIZ="$(cd "$(dirname "$0")/.." && pwd)"
SHA="$(git -C "$RAIZ" rev-parse --short HEAD)"
IMAGEN="ms-categoria:${SHA}"

echo "==> Construyendo ${IMAGEN}"
docker build --build-arg APP_VERSION="${SHA}" -t "${IMAGEN}" "$RAIZ/ms_categoria"

echo "==> Cargando la imagen en el nodo de Kubernetes"
NODO="$(kubectl get nodes -o jsonpath='{.items[0].metadata.name}')"
if docker inspect "$NODO" >/dev/null 2>&1; then
  # Nodo kind (contenedor): se importa directo en su containerd
  docker save "${IMAGEN}" | docker exec -i "$NODO" ctr -n k8s.io images import --digests -
else
  echo "   (el nodo comparte las imágenes de Docker, no hace falta importarla)"
fi

"$RAIZ/kubernetes/desplegar.sh" "docker.io/library/${IMAGEN}"
