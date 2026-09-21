#!/bin/sh
# Imagenes que usan los ejecutores. Las de TypeScript y COBOL se construyen
# aqui porque llevan el compilador preinstalado: el sandbox corre sin red.
for imagen in python:3.12-alpine node:20-alpine eclipse-temurin:21-jdk; do
  if docker pull "$imagen" >/dev/null 2>&1; then
    echo "listo   $imagen"
  else
    echo "fallo   $imagen"
  fi
done

raiz="$(CDPATH= cd -- "$(dirname "$0")/.." && pwd)"

if docker build -t kata-typescript:20 "$raiz/docker/typescript" >/dev/null; then
  echo "listo   kata-typescript:20"
else
  echo "fallo   kata-typescript:20"
fi

if docker build -t kata-cobol:3 "$raiz/docker/cobol" >/dev/null; then
  echo "listo   kata-cobol:3"
else
  echo "fallo   kata-cobol:3"
fi
