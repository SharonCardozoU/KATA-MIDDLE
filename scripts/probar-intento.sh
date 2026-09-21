#!/bin/sh
echo "== POST intento =="
curl -sS -m 8 -w "\nhttp:%{http_code}\n" \
  -X POST http://localhost:8080/api/evaluaciones/1/intentos \
  -H 'Content-Type: application/json' \
  -d '{}'
echo
echo "== flyway =="
docker exec kata-postgres psql -U kata -d evaluaciones -c "SELECT installed_rank, script, success FROM flyway_schema_history ORDER BY installed_rank;"
echo "== tablas =="
docker exec kata-postgres psql -U kata -d evaluaciones -c "\dt"
