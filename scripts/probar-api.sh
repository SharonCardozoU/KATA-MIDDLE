#!/bin/sh
# Ejercita la API con los tres lenguajes y con los dos casos de borde que
# sustentan los escenarios de calidad.
BASE="${1:-http://localhost:8080}"

echo "== Lenguajes soportados (EC02) =="
curl -s "$BASE/api/lenguajes"; echo

echo ""
echo "== Python: dos casos, uno correcto y uno incorrecto =="
curl -s -X POST "$BASE/api/ejecuciones" -H 'Content-Type: application/json' -d '{
  "lenguaje": "python",
  "codigoFuente": "valor = int(input())\nprint(valor * 2)\n",
  "puntajeMaximo": 10,
  "casos": [
    {"entrada": "42", "salidaEsperada": "84"},
    {"entrada": "7",  "salidaEsperada": "99"}
  ]
}'; echo

echo ""
echo "== Java: mismo problema, misma interfaz (EC02) =="
curl -s -X POST "$BASE/api/ejecuciones" -H 'Content-Type: application/json' -d '{
  "lenguaje": "java",
  "codigoFuente": "import java.util.Scanner;\npublic class Solucion { public static void main(String[] a) { Scanner s = new Scanner(System.in); System.out.println(s.nextInt() * 2); } }\n",
  "puntajeMaximo": 10,
  "casos": [ {"entrada": "42", "salidaEsperada": "84"} ]
}'; echo

echo ""
echo "== JavaScript =="
curl -s -X POST "$BASE/api/ejecuciones" -H 'Content-Type: application/json' -d '{
  "lenguaje": "javascript",
  "codigoFuente": "const d=require(\"fs\").readFileSync(0,\"utf8\").trim();console.log(Number(d)*2);\n",
  "puntajeMaximo": 10,
  "casos": [ {"entrada": "42", "salidaEsperada": "84"} ]
}'; echo

echo ""
echo "== TypeScript: compilado y ejecutado con la misma interfaz (EC02) =="
curl -s -X POST "$BASE/api/ejecuciones" -H 'Content-Type: application/json' -d '{
  "lenguaje": "typescript",
  "codigoFuente": "import * as fs from \"fs\";\nconst entrada: string = fs.readFileSync(0, \"utf8\").trim();\nconsole.log(Number(entrada) * 2);\n",
  "puntajeMaximo": 10,
  "casos": [ {"entrada": "42", "salidaEsperada": "84"} ]
}'; echo

echo ""
echo "== COBOL: el plus de EC02, una clase nueva sin tocar el registro =="
curl -s -X POST "$BASE/api/ejecuciones" -H 'Content-Type: application/json' -d '{
  "lenguaje": "cobol",
  "codigoFuente": "IDENTIFICATION DIVISION.\nPROGRAM-ID. SOLUCION.\nDATA DIVISION.\nWORKING-STORAGE SECTION.\n01 N PIC 9(8).\n01 R PIC Z(7)9.\nPROCEDURE DIVISION.\n    ACCEPT N\n    COMPUTE R = N * 2\n    DISPLAY FUNCTION TRIM(R)\n    STOP RUN.\n",
  "puntajeMaximo": 10,
  "casos": [ {"entrada": "42", "salidaEsperada": "84"} ]
}'; echo

echo ""
echo "== Bucle infinito: el motor debe responder TIEMPO_AGOTADO, no colgarse (EC03) =="
curl -s -X POST "$BASE/api/ejecuciones" -H 'Content-Type: application/json' -d '{
  "lenguaje": "python",
  "codigoFuente": "while True:\n    pass\n",
  "puntajeMaximo": 10,
  "casos": [ {"entrada": "1", "salidaEsperada": "1"} ]
}'; echo

echo ""
echo "== Intento de salir a la red: debe fallar el caso, no la plataforma (EC01) =="
curl -s -X POST "$BASE/api/ejecuciones" -H 'Content-Type: application/json' -d '{
  "lenguaje": "python",
  "codigoFuente": "import urllib.request\nprint(urllib.request.urlopen(\"http://example.com\", timeout=3).status)\n",
  "puntajeMaximo": 10,
  "casos": [ {"entrada": "1", "salidaEsperada": "200"} ]
}'; echo

echo ""
echo "== Lenguaje no soportado: debe responder 422 =="
curl -s -o /dev/null -w "codigo http = %{http_code}\n" -X POST "$BASE/api/ejecuciones" \
  -H 'Content-Type: application/json' -d '{
  "lenguaje": "ruby",
  "codigoFuente": "puts 1",
  "puntajeMaximo": 10,
  "casos": [ {"entrada": "1", "salidaEsperada": "1"} ]
}'
