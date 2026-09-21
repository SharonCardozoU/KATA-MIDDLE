#!/bin/sh
# Arranca SonarQube local (puerto 9000) y publica el analisis de backend + frontend.
# No usa 8080. La primera vez descarga la imagen y puede tardar un par de minutos.
set -e
raiz="$(CDPATH= cd -- "$(dirname -- "$0")/.." && pwd)"
cd "$raiz"

if [ -f "$HOME/.local/herramientas/entorno.sh" ]; then
  # shellcheck disable=SC1091
  . "$HOME/.local/herramientas/entorno.sh"
fi

echo "== Levantando SonarQube en :9000 =="
if docker ps -a --format '{{.Names}}' | grep -qx kata-sonar; then
  docker start kata-sonar >/dev/null
else
  docker compose -p kata-sonar -f compose.sonar.yaml up -d
fi

echo "== Esperando a que SonarQube quede UP =="
i=0
status=""
while [ "$i" -lt 90 ]; do
  status="$(curl -s http://localhost:9000/api/system/status 2>/dev/null | sed -n 's/.*"status":"\([^"]*\)".*/\1/p' || true)"
  if [ "$status" = "UP" ]; then
    break
  fi
  i=$((i + 1))
  sleep 4
done
if [ "$status" != "UP" ]; then
  echo "SonarQube no arranco. Mira: docker logs kata-sonar"
  exit 1
fi

# El scanner 8 + Sonar 26 no aceptan usuario/clave: hay que mandar un token.
# Por defecto admin/admin (asi viene el contenedor). Si ya la cambiaste en la UI:
#   SONAR_PASSWORD=tuclave sh scripts/sonar.sh
host="http://localhost:9000"
# Politica de Sonar 26: 12+ caracteres, mayuscula, minuscula, numero y simbolo.
clave_inicial="KataSonar12!"

sonar_ok() {
  curl -s -u "admin:$1" "$host/api/authentication/validate" 2>/dev/null | grep -q '"valid":true'
}

echo "== Autenticando en Sonar =="
clave="${SONAR_PASSWORD:-}"
if [ -n "$clave" ]; then
  if ! sonar_ok "$clave"; then
    echo "No pude autenticar en Sonar. La clave de SONAR_PASSWORD no sirve."
    exit 1
  fi
elif sonar_ok admin; then
  cambio="$(curl -s -u admin:admin -X POST "$host/api/users/change_password" \
    --data-urlencode "login=admin" \
    --data-urlencode "previousPassword=admin" \
    --data-urlencode "password=$clave_inicial" || true)"
  if ! sonar_ok "$clave_inicial"; then
    echo "No pude cambiar la clave por defecto de Sonar."
    printf '%s\n' "$cambio"
    exit 1
  fi
  clave="$clave_inicial"
elif sonar_ok "$clave_inicial"; then
  clave="$clave_inicial"
else
  echo "No pude autenticar en Sonar. Prueba SONAR_PASSWORD=... sh scripts/sonar.sh"
  echo "Usuario admin. Si es la primera vez, la clave queda en $clave_inicial."
  exit 1
fi

curl -s -u "admin:$clave" -X POST "$host/api/user_tokens/revoke" \
  --data-urlencode "name=kata-scanner" >/dev/null 2>&1 || true

respuesta="$(curl -s -u "admin:$clave" -X POST "$host/api/user_tokens/generate" \
  --data-urlencode "name=kata-scanner" \
  --data-urlencode "type=USER_TOKEN" || true)"
token="$(printf '%s' "$respuesta" | sed -n 's/.*"token":"\([^"]*\)".*/\1/p')"
if [ -z "$token" ]; then
  echo "No pude crear el token de Sonar."
  printf '%s\n' "$respuesta"
  exit 1
fi

echo "== Tests y cobertura JaCoCo del backend =="
( cd backend && mvn -q test )

echo "== Publicando analisis =="
docker run --rm \
  --network container:kata-sonar \
  -v "$raiz:/usr/src" \
  -w /usr/src \
  -e SONAR_HOST_URL=http://127.0.0.1:9000 \
  -e SONAR_TOKEN="$token" \
  sonarsource/sonar-scanner-cli

echo ""

echo "== Issues abiertos =="
curl -s -u "admin:$clave" \
  "$host/api/issues/search?componentKeys=kata-evaluacion-tecnica&resolved=false&ps=30" \
  | python3 -c '
import json,sys
data=json.load(sys.stdin)
print("total:", data.get("total"))
for issue in data.get("issues", []):
    print("{:<8} {:<12} {} {}:{}".format(
        issue.get("severity",""),
        issue.get("rule",""),
        issue.get("message",""),
        issue.get("component","").split(":")[-1],
        issue.get("line")))
'

