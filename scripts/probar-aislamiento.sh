#!/bin/sh
# Evidencia de EC01 y EC03 sin necesidad del backend: ejercita los mismos flags
# de docker run que usa SandboxDocker y verifica que la frontera aguante.

USUARIO="$(id -u):$(id -g)"
ESPACIO="$(mktemp -d /tmp/kata-prueba-XXXXXX)"
trap 'rm -rf "$ESPACIO"' EXIT

aislado() {
  docker run --rm \
    --network none \
    --read-only \
    --user "$USUARIO" \
    --cap-drop ALL \
    --security-opt no-new-privileges \
    --tmpfs /tmp \
    --memory 256m --memory-swap 256m \
    --cpus 1.0 \
    --pids-limit 64 \
    -v "$ESPACIO:/work" \
    -w /work \
    python:3.12-alpine "$@"
}

echo "== 1. Caso normal: debe imprimir 84 =="
printf 'valor = int(input())\nprint(valor * 2)\n' > "$ESPACIO/solucion.py"
printf '42\n' > "$ESPACIO/caso_0.in"
cat > "$ESPACIO/ejecutar.sh" <<'GUION'
#!/bin/sh
cd /work
i=0
while [ $i -lt 1 ]; do
  timeout 5 python3 solucion.py < caso_$i.in > caso_$i.out 2> caso_$i.err
  echo $? > caso_$i.code
  i=$((i+1))
done
exit 0
GUION
aislado sh ejecutar.sh
echo "salida=$(cat "$ESPACIO/caso_0.out") codigo=$(cat "$ESPACIO/caso_0.code")"

echo ""
echo "== 2. Red: debe fallar el acceso a internet =="
if aislado python3 -c "import urllib.request; urllib.request.urlopen('http://example.com', timeout=5)" 2>/dev/null; then
  echo "PROBLEMA: el contenedor alcanzo la red"
else
  echo "OK: sin salida a la red"
fi

echo ""
echo "== 3. Escritura fuera del directorio de trabajo: debe fallar =="
if aislado sh -c "echo intruso > /etc/kata_intruso" 2>/dev/null; then
  echo "PROBLEMA: escribio en /etc"
else
  echo "OK: sistema de archivos de solo lectura"
fi

echo ""
echo "== 4. Bucle infinito: debe terminar con codigo 124 =="
printf 'while True:\n    pass\n' > "$ESPACIO/solucion.py"
aislado sh -c "timeout 5 python3 solucion.py; echo codigo=\$?"

echo ""
echo "== 5. Privilegios: el usuario no debe ser root =="
echo "uid dentro del contenedor = $(aislado id -u)"
