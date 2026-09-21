package com.kata.evaluacion.motor.modelo;

import java.util.List;

public record ResultadoEjecucion(
        boolean compilacionExitosa,
        String mensajeCompilacion,
        List<ResultadoCaso> casos) {

    public static ResultadoEjecucion fallaCompilacion(String mensaje) {
        return new ResultadoEjecucion(false, mensaje, List.of());
    }

    public static ResultadoEjecucion compilado(List<ResultadoCaso> casos) {
        return new ResultadoEjecucion(true, "Compilacion exitosa", casos);
    }
}
