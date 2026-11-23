#!/usr/bin/env python3
# =====================================================================
# EP3 / IE5 - Script personalizado de auditoría de cumplimiento
#
# Revisa automáticamente el Dockerfile, los manifiestos de Kubernetes y
# la configuración de Spring contra un conjunto de políticas. Cada
# política indica la norma o buena práctica de la que proviene.
# Si alguna política FALLA, el script termina con código 1 y el pipeline
# se detiene (nada llega a desplegarse).
#
# Uso: python3 scripts/auditoria_cumplimiento.py   (desde la raíz del repo)
# Requiere: PyYAML
# =====================================================================
import glob
import os
import re
import subprocess
import sys

import yaml

RAIZ = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
DOCKERFILE = os.path.join(RAIZ, "ms_categoria", "Dockerfile")
MANIFIESTOS = sorted(glob.glob(os.path.join(RAIZ, "kubernetes", "**", "*.yaml"), recursive=True))
APP_YML = glob.glob(os.path.join(RAIZ, "ms_categoria", "src", "main", "resources", "application*.yml"))

resultados = []  # (id, norma, descripción, ok, detalle)


def politica(pid, norma, descripcion, problemas):
    """Registra el resultado de una política: pasa si la lista de problemas está vacía."""
    resultados.append((pid, norma, descripcion, not problemas, "; ".join(problemas[:5])))


def cargar_objetos():
    """Lee todos los objetos YAML de la carpeta kubernetes/."""
    objetos = []
    for ruta in MANIFIESTOS:
        with open(ruta, encoding="utf-8") as f:
            for doc in yaml.safe_load_all(f):
                if isinstance(doc, dict):
                    objetos.append((os.path.relpath(ruta, RAIZ), doc))
    return objetos


def contenedores(objetos):
    """Devuelve (archivo, workload, pod_spec, contenedor) de cada Deployment."""
    for ruta, obj in objetos:
        if obj.get("kind") in ("Deployment", "StatefulSet", "DaemonSet"):
            spec = obj["spec"]["template"]["spec"]
            for c in spec.get("containers", []):
                yield ruta, obj["metadata"]["name"], spec, c


# ---------------------------------------------------------------- Dockerfile
with open(DOCKERFILE, encoding="utf-8") as f:
    lineas = [l.strip() for l in f if l.strip() and not l.strip().startswith("#")]

etapa_final = []
for l in lineas:
    if l.upper().startswith("FROM "):
        etapa_final = []
    etapa_final.append(l)
usuarios = [l.split(None, 1)[1] for l in etapa_final if l.upper().startswith("USER ")]
politica("DOCKER-01", "CIS Docker Benchmark 4.1",
         "La imagen final corre con un usuario distinto de root",
         [] if usuarios and usuarios[-1].split(":")[0] not in ("root", "0") else ["no hay USER no-root en la etapa final"])

froms = [l.split()[1] for l in lineas if l.upper().startswith("FROM ")]
politica("DOCKER-02", "CIS Docker Benchmark 4.2 / supply chain",
         "Las imágenes base tienen versión fija (sin :latest)",
         [f for f in froms if ":" not in f or f.endswith(":latest")])

# ---------------------------------------------------------------- Kubernetes
objetos = cargar_objetos()
lista = list(contenedores(objetos))

politica("K8S-01", "CIS Kubernetes 5.7 / buenas prácticas",
         "Ninguna imagen usa la etiqueta :latest",
         [f"{w}/{c['name']}" for _, w, _, c in lista if c["image"].endswith(":latest") or ":" not in c["image"]])

politica("K8S-02", "CIS Kubernetes / NSA-CISA Hardening Guide",
         "Todo contenedor declara límites de CPU y memoria",
         [f"{w}/{c['name']}" for _, w, _, c in lista
          if not (c.get("resources", {}).get("limits", {}).get("memory")
                  and c.get("resources", {}).get("limits", {}).get("cpu"))])

politica("K8S-03", "CIS Kubernetes 5.2.5",
         "Ningún contenedor permite escalar privilegios",
         [f"{w}/{c['name']}" for _, w, _, c in lista
          if c.get("securityContext", {}).get("allowPrivilegeEscalation") is not False])

politica("K8S-10", "CIS Kubernetes 5.2.6 / Pod Security restricted",
         "Todos los pods declaran que corren sin root (runAsNonRoot)",
         [f"{w}" for _, w, spec, c in lista
          if not (spec.get("securityContext", {}).get("runAsNonRoot")
                  or c.get("securityContext", {}).get("runAsNonRoot"))])

politica("K8S-04", "CIS Kubernetes 5.2.1",
         "Ningún contenedor es privilegiado",
         [f"{w}/{c['name']}" for _, w, _, c in lista if c.get("securityContext", {}).get("privileged")])

politica("K8S-05", "Disponibilidad (observabilidad)",
         "Los contenedores de la aplicación tienen readiness y liveness probe",
         [f"{w}/{c['name']}" for r, w, _, c in lista
          if "app" + os.sep in r and c["name"] != "mysqld-exporter"
          and not (c.get("readinessProbe") and c.get("livenessProbe"))])

politica("K8S-06", "OWASP Top 10 A07 / CWE-798 (credenciales en el código)",
         "No hay objetos Secret versionados en el repositorio",
         [f"{r}: {o['metadata']['name']}" for r, o in objetos if o.get("kind") == "Secret"])

politica("K8S-07", "OWASP Top 10 A07 / CWE-798",
         "Las variables con contraseñas se leen desde un Secret (no en texto plano)",
         [f"{w}/{c['name']}:{e['name']}" for _, w, _, c in lista for e in c.get("env", [])
          if re.search(r"PASSWORD|SECRET|TOKEN", e["name"]) and "value" in e])

politica("K8S-08", "Kubernetes Pod Security Standards",
         "Los namespaces aplican una política Pod Security (enforce)",
         [o["metadata"]["name"] for _, o in objetos if o.get("kind") == "Namespace"
          and "pod-security.kubernetes.io/enforce" not in o["metadata"].get("labels", {})])

anotaciones_ms = [o["spec"]["template"]["metadata"].get("annotations", {}) for _, o in objetos
                  if o.get("kind") == "Deployment" and o["metadata"]["name"] == "ms-categoria"]
politica("K8S-09", "Observabilidad",
         "El microservicio publica sus métricas para Prometheus",
         [] if anotaciones_ms and anotaciones_ms[0].get("prometheus.io/scrape") == "true"
         else ["ms-categoria sin anotación prometheus.io/scrape"])

# ---------------------------------------------------------------- Spring / repositorio
problemas_pwd, problemas_act = [], []
for ruta in APP_YML:
    with open(ruta, encoding="utf-8") as f:
        cfg = yaml.safe_load(f) or {}
    pwd = (cfg.get("spring", {}).get("datasource", {}) or {}).get("password")
    if pwd is not None and not str(pwd).startswith("${"):
        problemas_pwd.append(os.path.basename(ruta))
    exp = (((cfg.get("management", {}) or {}).get("endpoints", {}) or {}).get("web", {}) or {}).get("exposure", {}) or {}
    if "*" in str(exp.get("include", "")):
        problemas_act.append(os.path.basename(ruta))
politica("APP-01", "OWASP Top 10 A07 / CWE-798",
         "La contraseña de la base de datos viene de una variable de entorno", problemas_pwd)
politica("APP-02", "OWASP Top 10 A05 (configuración insegura)",
         "Actuator no expone todos sus endpoints ('*')", problemas_act)

try:
    versionados = subprocess.run(["git", "ls-files"], cwd=RAIZ, capture_output=True, text=True, check=True).stdout.split()
except (OSError, subprocess.CalledProcessError):
    versionados = []
politica("REPO-01", "OWASP Top 10 A07 / CWE-798",
         "No hay archivos .env versionados en git",
         [f for f in versionados if os.path.basename(f) == ".env"])

# ---------------------------------------------------------------- Reporte
fallas = [r for r in resultados if not r[3]]
tabla = ["| Resultado | ID | Norma / referencia | Política | Detalle |", "|---|---|---|---|---|"]
for pid, norma, desc, ok, det in resultados:
    tabla.append(f"| {'✅ PASA' if ok else '❌ FALLA'} | {pid} | {norma} | {desc} | {det} |")
reporte = "\n".join(["### Auditoría de cumplimiento", "",
                     f"**{len(resultados) - len(fallas)} de {len(resultados)} políticas cumplidas**", ""] + tabla)
print(reporte)
if os.environ.get("GITHUB_STEP_SUMMARY"):
    with open(os.environ["GITHUB_STEP_SUMMARY"], "a", encoding="utf-8") as f:
        f.write(reporte + "\n")

sys.exit(1 if fallas else 0)
