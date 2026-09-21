SELECT p.id, p.nombre, p.descripcion, p.lenguaje, p.puntaje,
       c.orden, c.entrada, c.salida_esperada
FROM pregunta p
JOIN caso_prueba c ON c.pregunta_id = p.id
ORDER BY p.id, c.orden;
