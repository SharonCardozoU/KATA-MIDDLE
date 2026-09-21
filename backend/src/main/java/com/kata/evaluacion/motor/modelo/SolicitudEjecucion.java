package com.kata.evaluacion.motor.modelo;

import java.util.List;

public record SolicitudEjecucion(String lenguaje, String codigoFuente, List<CasoPrueba> casos) {
}
