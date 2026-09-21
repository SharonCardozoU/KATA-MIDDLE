#!/bin/sh
# Comprueba que el camino navegador -> servidor de desarrollo -> backend funciona,
# que es el mismo que usa la aplicacion Angular en localhost:4200.
echo "== Lenguajes vistos por el frontend =="
curl -s http://localhost:4200/api/lenguajes; echo

echo ""
echo "== Envio completo atravesando el proxy =="
curl -s -X POST http://localhost:4200/api/ejecuciones -H 'Content-Type: application/json' -d '{
  "lenguaje": "python",
  "codigoFuente": "valor = int(input())\nprint(valor * 2)\n",
  "puntajeMaximo": 10,
  "casos": [
    {"entrada": "42", "salidaEsperada": "84"},
    {"entrada": "7",  "salidaEsperada": "14"}
  ]
}'; echo
