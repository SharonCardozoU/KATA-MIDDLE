package com.kata.evaluacion.motor;

import com.kata.evaluacion.motor.modelo.ResultadoEjecucion;
import com.kata.evaluacion.motor.modelo.SolicitudEjecucion;

/**
 * Strategy. Cada lenguaje soportado aporta una implementacion.
 *
 * El lenguaje se identifica con un String y no con un enum a proposito: agregar
 * COBOL debe ser una clase nueva sin modificar ningun tipo existente, que es la
 * medida declarada en EC02.
 */
public interface Ejecutor {

    String lenguaje();

    ResultadoEjecucion ejecutar(SolicitudEjecucion solicitud);
}
