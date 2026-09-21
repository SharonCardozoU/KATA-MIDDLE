#!/bin/sh
# Verifica si las tres imagenes base soportan 'date +%s%N', que es lo que
# necesita el guion para medir el tiempo real de cada caso de prueba.
for imagen in python:3.12-alpine node:20-alpine eclipse-temurin:21-jdk; do
  printf '%-28s ' "$imagen"
  docker run --rm "$imagen" sh -c 'date +%s%N' 2>&1 | head -1
done
